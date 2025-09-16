package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.*
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceObjectiveTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceUnitTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.AngleDial
import kotlin.math.roundToInt

@Composable
fun ToolConfig(modifier: Modifier = Modifier) {
    val toolService by rememberInstance<ToolService>()
    val currentTool by toolService.currentTool.collectAsState()


    val content: @Composable (() -> Unit)? = when (currentTool) {
        is TerrainTool -> {
            { TerrainToolConfig() }
        }

        is HeightTool -> {
            { HeightToolConfig() }
        }

        is PlaceUnitTool -> {
            { PlaceUnitToolConfig() }
        }

        is PlaceObjectiveTool -> {
            { PlaceObjectiveToolConfig() }
        }

        else -> null
    }

    if (content != null) {
        content()
    }
}

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
private fun TerrainToolConfig() {

    val currentTerrain by TerrainTool.terrain.collectAsState()

    val popupManager = remember { PopupManager() }

    BrushToolConfig(TerrainTool)

    ComboBox(
        labelText = currentTerrain.name, popupManager = popupManager, popupContent = {
            VerticallyScrollableContainer {
                Column {
                    TerrainType.entries.sortedByDescending {
                        it.dominance
                    }.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                TerrainTool.terrain.value = item
                                popupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = item.name,
                            )
                        }

                    }
                }
            }
        })

}

@Composable
private fun HeightToolConfig() {
    val currentHeight by HeightTool.height.collectAsState()

    var value by remember { mutableStateOf(currentHeight.toFloat()) }

    LaunchedEffect(value) {
        HeightTool.height.value = value.roundToInt()
    }


    BrushToolConfig(HeightTool)

    Text("Height: $currentHeight")
    Slider(
        value = value, // Float
        onValueChange = { newValue ->
            value = newValue // просто оновлюємо
        }, valueRange = 0f..10f, // будь-яке значення між 1 та 10
        steps = 0, // без кроків
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalJewelApi::class)
@Composable
private fun PlaceUnitToolConfig() {
    val currentUnit by PlaceUnitTool.currentUnit.collectAsState()

    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val scenario by editorService.scenario.collectAsState()


    val playerIndex = currentUnit.owner.key
    val teamPopupManager = remember { PopupManager() }
    val unitPopupManager = remember { PopupManager() }



    ComboBox(
        labelText = "$playerIndex ${scenario!!.players[playerIndex].team}",
        popupManager = teamPopupManager,
        popupContent = {
            VerticallyScrollableContainer {
                Column {
                    scenario!!.players.withIndex().sortedByDescending {
                        it.index
                    }.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

                                PlaceUnitTool.currentUnit.value = currentUnit.copy(
                                    owner = Reference(item.index)
                                )
                                teamPopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = "${item.index} ${item.value.team}",
                            )
                        }

                    }
                }
            }
        })


    val nameFieldState by remember { mutableStateOf(TextFieldState(currentUnit.name ?: "")) }

    LaunchedEffect(nameFieldState) {
        PlaceUnitTool.currentUnit.value = currentUnit.copy(
            name = nameFieldState.text.toString().takeIf { it.isNotBlank() })

        println(nameFieldState.text.toString().takeIf { it.isNotBlank() })
    }

    LaunchedEffect(Unit) {
        snapshotFlow { nameFieldState.text.toString() }
            .collect { text ->
                PlaceUnitTool.currentUnit.value = currentUnit.copy(
                    name = text.takeIf { it.isNotBlank() }
                )
                println(text.takeIf { it.isNotBlank() })
            }
    }


    Spacer(Modifier.height(4.dp))

    TextField(
        nameFieldState, Modifier.fillMaxWidth(), placeholder = { Text("Unit name... (Blank - default name)") })

    Spacer(Modifier.height(4.dp))


    ComboBox(
        labelText = currentUnit.type.name, popupManager = unitPopupManager, popupContent = {
            VerticallyScrollableContainer {
                Column {
                    GameUnitType.entries.sortedByDescending {
                        it.ordinal
                    }.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                PlaceUnitTool.currentUnit.value = PlaceUnitTool.currentUnit.value.copy(
                                    type = item
                                )
                                unitPopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = item.name,
                            )
                        }

                    }
                }
            }
        })

    var angle by remember { mutableStateOf(0f) }

    LaunchedEffect(angle) {
        PlaceUnitTool.currentUnit.value = currentUnit.copy(
            rotationRadians = angle
        )
    }

    Spacer(Modifier.height(4.dp))


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AngleDial(
            angle, color = scenario!!.players[playerIndex].team.color, modifier = Modifier.size(200.dp)
        )

        Slider(
            value = angle,
            onValueChange = { angle = it },
            valueRange = 0f..(2 * Math.PI).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }

}

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
private fun PlaceObjectiveToolConfig() {
    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()


    val currentObjective by PlaceObjectiveTool.currentObjective.collectAsState()
    val playerIndex = currentObjective.owner?.key
    val scenario by editorService.scenario.collectAsState()

    val playerTeamPopupManager = remember { PopupManager() }

    val objectiveNameTextFieldState = rememberTextFieldState("")

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ComboBox(labelText = playerIndex?.let { "$playerIndex ${scenario!!.players[playerIndex].team}" } ?: "No one",
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

                        scenario!!.players.withIndex().sortedByDescending {
                            it.index
                        }.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

                                    PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                                        owner = Reference(item.index)
                                    )
                                    playerTeamPopupManager.setPopupVisible(false)
                                }) {
                                Text(
                                    text = "${item.index} ${item.value.team}",
                                )
                            }

                        }
                    }
                }
            })


        TextField(
            objectiveNameTextFieldState,
            Modifier.fillMaxWidth(),
            placeholder = { Text("Objective name... (Blank - default name)") }
        )
    }

    LaunchedEffect(objectiveNameTextFieldState) {
        PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
            name = objectiveNameTextFieldState.takeIf { it.text.isNotBlank() }?.text?.toString()
        )
    }


}