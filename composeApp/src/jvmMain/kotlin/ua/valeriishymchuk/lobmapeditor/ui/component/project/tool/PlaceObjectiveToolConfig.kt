package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.objective.ObjectiveType
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.PlaceObjectiveTool
import kotlin.getValue
import kotlin.math.max
import kotlin.text.ifEmpty

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlaceObjectiveToolConfig() {
    val editorService by rememberInstance<EditorService<*>>()
    val presetEditorService = editorService as? PresetEditorService

    val currentObjective by PlaceObjectiveTool.currentObjective.collectAsState()
    var playerIndex = currentObjective.owner
    val scenario by editorService.scenario.collectAsState()
    if (playerIndex != null) {
        if (presetEditorService != null) {
            if (!presetEditorService.scenario.value!!.players.indices.contains(playerIndex)) {
                playerIndex = null
            }
        } else {
            if (!PlayerTeam.entries.indices.contains(playerIndex)) playerIndex = null
        }
    }
    val playerTeamPopupManager = remember { PopupManager() }
    val objectiveTypePopupManager = remember { PopupManager() }

    val objectiveNameTextFieldState = rememberTextFieldState("")

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val ownerTeam: PlayerTeam? = playerIndex?.let { ownerIndex ->
            if (presetEditorService != null) {
                presetEditorService.scenario.value!!.players[ownerIndex].team
            } else PlayerTeam.entries.get(ownerIndex)
        }
        // owner
        ComboBox(labelText = playerIndex?.let { "${playerIndex + 1} $ownerTeam" }
            ?: "No one",
            popupManager = playerTeamPopupManager,
            popupContent = {
                VerticallyScrollableContainer {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                                    owner = null
                                )
                                playerTeamPopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = "No one",
                            )
                        }

                        if (presetEditorService != null) {
                            presetEditorService.scenario.value!!.players.withIndex().sortedByDescending {
                                it.index
                            }.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

                                        PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                                            owner = item.index
                                        )
                                        playerTeamPopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "${item.index + 1} ${item.value.team}",
                                    )
                                }

                            }
                        } else {
                            PlayerTeam.entries.withIndex().sortedByDescending {
                                it.index
                            }.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

                                        PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                                            owner = item.index
                                        )
                                        playerTeamPopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "${item.index + 1} ${item.value}",
                                    )
                                }

                            }
                        }


                    }
                }
            })

        // type
        ComboBox(
            labelText = "${currentObjective.type}",
            popupManager = objectiveTypePopupManager,
            popupContent = {
                VerticallyScrollableContainer {
                    Column {

                        ObjectiveType.entries.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                    PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                                        type = item,
                                        victoryPoints = item.defaultVictoryPoints
                                    )
                                    objectiveTypePopupManager.setPopupVisible(false)
                                }) {
                                Text(
                                    text = "$item",
                                )
                            }

                        }
                    }
                }
            })

        // name
        TextField(
            objectiveNameTextFieldState,
            Modifier.fillMaxWidth(),
            placeholder = { Text("Objective name... (Blank - default name)") }
        )

        // Victory Points
        var victoryPointsTextValue by remember(currentObjective) {
            mutableStateOf(
                TextFieldValue(
                    text = currentObjective.victoryPoints.toString(),
                    selection = TextRange(currentObjective.victoryPoints.toString().length) // Or calculate appropriate position
                )
            )
        }
        TextField(
            value = victoryPointsTextValue,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { newValue ->
                victoryPointsTextValue = newValue
                victoryPointsTextValue = victoryPointsTextValue.copy(
                    text = newValue.text
                        .replace(Regex("[^0-9]"), "").let { str ->
                            val value = str.toIntOrNull() ?: return@let str
                            val coercedValue = max(value, Objective.MIN_VICTORY_POINTS)
                            if (coercedValue == value) return@let str
                            coercedValue.toString()
                        }
                )

                val finalText: Int =
                    victoryPointsTextValue.text.ifEmpty { Objective.MIN_VICTORY_POINTS.toString() }.toIntOrNull()
                        ?: Objective.MIN_VICTORY_POINTS

                PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                    victoryPoints = finalText
                )

            },

            leadingIcon = {
                Row {
                    Text("Victory Points", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { objectiveNameTextFieldState.text.toString() }
            .collect { text ->
                PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                    name = text.takeIf { it.isNotBlank() }
                )
            }
    }

}