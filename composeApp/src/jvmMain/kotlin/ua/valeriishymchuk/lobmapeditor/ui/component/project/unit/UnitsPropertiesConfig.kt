package ua.valeriishymchuk.lobmapeditor.ui.component.project.unit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.jogamp.opengl.awt.GLCanvas
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceUnitTool
import kotlin.getValue

@OptIn(ExperimentalJewelApi::class)
@Composable
fun UnitsPropertiesConfig() {

    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val scenario by editorService.scenario.collectAsState()
    val rawSelection by editorService.selectedUnits.collectAsState()
    val canvas by rememberInstance<GLCanvas>()


    if (rawSelection.isEmpty()) return

    val selection by derivedStateOf {
        rawSelection.map { unit -> unit.getValue(scenario!!.units::get) }
    }

    fun updateSelectedUnits(unitMap: (GameUnit) -> GameUnit) {
        val copyList = scenario!!.units.toMutableList()
        val rawSelectionReferences = rawSelection.map { it.key }

        val command = UpdateGameUnitListCommand(
            scenario!!.units,
            copyList.mapIndexed { index, unit ->
                if (!rawSelectionReferences.contains(index)) return@mapIndexed unit
                unitMap(unit)
            }
        )

        if (command.newGameUnits == command.oldGameUnits) return

        editorService.executeCompound(command)

    }

    val isNameMixed by derivedStateOf { selection.map { it.name }.distinct().size > 1 }

    // Use TextFieldValue directly instead of TextFieldState
    var textFieldValue by remember(selection) {
        mutableStateOf(
            Unit.let {
                val currentText = when {
                    selection.isEmpty() -> ""
                    selection.map { it.name }.distinct().size > 1 -> ""
                    else -> selection.first().name ?: ""
                }
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // Or calculate appropriate position
                )
            }
        )
    }

    VerticallyScrollableContainer {
        Column {
            Spacer(Modifier.width(4.dp))
            Text("Name")
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    textFieldValue = newValue

                    val finalText: String? = newValue.text.ifEmpty { null }
                    updateSelectedUnits { it.copy(name = finalText) }
                },
                modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        editorService.flushCompound()
                    }
                },
                placeholder = { Text(if (isNameMixed) "Mixed" else "Empty") }
            )
        }
    }
}