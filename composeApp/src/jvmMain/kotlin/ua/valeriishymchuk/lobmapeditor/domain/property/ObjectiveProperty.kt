package ua.valeriishymchuk.lobmapeditor.domain.property

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective.Companion.MIN_VICTORY_POINTS
import ua.valeriishymchuk.lobmapeditor.domain.objective.ObjectiveType
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import kotlin.getValue
import kotlin.math.max
import kotlin.text.ifEmpty

interface ObjectiveProperty<SELF : ObjectiveProperty<SELF>> : DomainProperty<SELF> {

    // depends on scenario type can be either player or player team
    val owner: Int?

    val type: ObjectiveType
    val victoryPoints: Int

    fun withType(type: ObjectiveType): SELF
    fun withVictoryPoints(victoryPoints: Int): SELF
    fun withOwner(owner: Int?): SELF


    companion object {
        @OptIn(ExperimentalJewelApi::class,ExperimentalFoundationApi::class)
        @Composable
        fun Component(
            selectedObjectsReferences: Set<ScenarioReference>,
            onUpdate: ((ObjectiveProperty<*>) -> ObjectiveProperty<*>) -> Unit,
            onFlush: () -> Unit
        ) {
            val editorService by rememberInstance<EditorService<*>>()
            val scenarioNullable by editorService.scenario.collectAsState()
            val scenario = scenarioNullable ?: return

            val presetScenario: GameScenario.Preset? = scenario as? GameScenario.Preset

            val selectedObjectsRaw = selectedObjectsReferences.map { it.dereference(scenario) }
            if (!selectedObjectsRaw.all { it is ObjectiveProperty }) return

            val selectedObjects = selectedObjectsRaw.map { it as ObjectiveProperty }
            if (selectedObjects.isEmpty()) return

            val typePopupManager = remember { PopupManager() }
            val ownerPopupManager = remember { PopupManager() }


            val isOwnerMixed by derivedStateOf { selectedObjects.map { it.owner }.distinct().size > 1 }
            val isTypeMixed by derivedStateOf { selectedObjects.map { it.type }.distinct().size > 1 }
            val isVictoryPointsMixed by derivedStateOf { selectedObjects.map { it.victoryPoints }.distinct().size > 1 }

            var victoryPointsTextFieldValue by remember {
                mutableStateOf(
                    Unit.let {
                        val currentText = when {
                            selectedObjects.isEmpty() -> ""
                            isVictoryPointsMixed -> ""
                            else -> selectedObjects.map { it.victoryPoints }.distinct().firstOrNull()?.toString() ?: ""
                        }
                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length)
                        )
                    }
                )
            }

            // VP Selection handler
            LaunchedEffect(selectedObjectsRaw) {

                val textValue = victoryPointsTextFieldValue.text.toIntOrNull()
                val value = selectedObjects.map { it.victoryPoints }.distinct().firstOrNull()
                if (textValue != value || (textValue != null && isVictoryPointsMixed)) {
                    val finalValue: String = if (value != null && !isVictoryPointsMixed) value.toString()
                    else ""
                    victoryPointsTextFieldValue = victoryPointsTextFieldValue.copy(text = finalValue)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Victory Points:")
            TextField(
                value = victoryPointsTextFieldValue,
                onValueChange = { newValue ->
                    victoryPointsTextFieldValue = newValue
                    victoryPointsTextFieldValue = victoryPointsTextFieldValue.copy(
                        text = newValue.text
                            .replace(Regex("[^0-9.]"), "").let { str ->
                                val value = str.toIntOrNull() ?: return@let str
                                val coercedValue = max(value, MIN_VICTORY_POINTS)
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )


                    val finalText: Int = victoryPointsTextFieldValue.text.ifEmpty {
                        MIN_VICTORY_POINTS.toString()
                    }.toIntOrNull() ?: MIN_VICTORY_POINTS
                    onUpdate {
                        it.withVictoryPoints(finalText)
                    }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        onFlush()
                    }
                },
                placeholder = { Text(if (isVictoryPointsMixed) "Mixed" else "0") }
            )

            Spacer(Modifier.height(10.dp))

            // Objective type
            Text("Type:")
            ComboBox(
                labelText = if (isTypeMixed) "Mixed" else let {
                    val type = selectedObjects.map { it.type }.distinct().firstOrNull() ?: return@let ""
                    "$type"
                },
                popupManager = typePopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            ObjectiveType.entries.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        onUpdate {
                                            it.withType(item)
                                        }
                                        onFlush()
                                        typePopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "$item",
                                    )
                                }
                            }
                        }
                    }
                }
            )


            Spacer(Modifier.height(10.dp))

            // Objective owner
            Text("Owner:")
            ComboBox(
                labelText = if (isOwnerMixed) "Mixed" else let {
                    val selection = selectedObjects.first()
                    val ownerLabel: String = if (selection.owner == null) {
                        "No one"
                    }
                    else {
                        "${selection.owner!! + 1} ${
                            if (presetScenario != null) {
                                presetScenario.players[selection.owner!!].team
                            } else PlayerTeam.entries[selection.owner!!]
                        }"
                    }
                    ownerLabel
                },
                popupManager = ownerPopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                    onUpdate {
                                        it.withOwner(null)
                                    }
                                    onFlush()
                                    ownerPopupManager.setPopupVisible(false)
                                }) {
                                Text(
                                    text = "No one",
                                )
                            }
                            if (presetScenario != null) {
                                presetScenario.players.withIndex().sortedByDescending {
                                    it.index
                                }.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                            onUpdate {
                                                it.withOwner(item.index)
                                            }
                                            onFlush()
                                            ownerPopupManager.setPopupVisible(false)
                                        }) {
                                        Text(
                                            text = "${item.index + 1} ${item.value.team}",
                                        )
                                    }

                                }
                            } else {
                                PlayerTeam.entries.withIndex().sortedByDescending { it.index }.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                            onUpdate {
                                                it.withOwner(item.index)
                                            }
                                            onFlush()
                                            ownerPopupManager.setPopupVisible(false)
                                        }) {
                                        Text(
                                            text = "${item.index + 1} ${item.value}",
                                        )
                                    }

                                }
                            }


                        }
                    }
                }
            )

        }
    }

}