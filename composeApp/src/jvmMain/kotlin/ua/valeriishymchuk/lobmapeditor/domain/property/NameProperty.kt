package ua.valeriishymchuk.lobmapeditor.domain.property

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import kotlin.getValue
import kotlin.text.ifEmpty

interface NameProperty<SELF : NameProperty<SELF>> : DomainProperty<SELF> {

    val name: String?

    fun withName(name: String?): SELF

    companion object {
        @OptIn(ExperimentalJewelApi::class)
        @Composable
        fun Component(
            selectedObjectsReferences: Set<ScenarioReference>,
            onUpdate: ((NameProperty<*>) -> NameProperty<*>) -> Unit,
            onFlush: () -> Unit
        ) {

            val editorService by rememberInstance<EditorService<*>>()


            val scenarioNullable by editorService.scenario.collectAsState()
            val scenario = scenarioNullable ?: return

            val selectedObjectsRaw = selectedObjectsReferences.map { it.dereference(scenario) }
            if (!selectedObjectsRaw.all { it is NameProperty }) return

            val selectedObjects = selectedObjectsRaw.map { it as NameProperty }
            if (selectedObjects.isEmpty()) return
            val isNameMixed by derivedStateOf { selectedObjects.map { it.name }.distinct().size > 1 }

            var textFieldValue by remember(selectedObjectsRaw) {
                mutableStateOf(
                    Unit.let {
                        val currentText = when {
                            selectedObjects.isEmpty() -> ""
                            selectedObjects.map { it.name }.distinct().size > 1 -> ""
                            else -> selectedObjects.map { it.name }.distinct().firstOrNull() ?: ""
                        }
                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length) // Or calculate appropriate position
                        )
                    }
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Name:")
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    textFieldValue = newValue

                    val finalText: String? = newValue.text.ifEmpty { null }
                    onUpdate {
                        it.withName(finalText)
                    }
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