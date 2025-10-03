package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jogamp.opengl.awt.GLCanvas
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.theme.defaultButtonStyle
import org.joml.Vector2f
import org.joml.Vector4f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.ComposedCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdatePlayerListCommand
import ua.valeriishymchuk.lobmapeditor.commands.WrapCommonToPresetCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.objective.ObjectiveType
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tools.GridTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceObjectiveTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceUnitTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlayerTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.ReferenceOverlayTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.AngleDial
import kotlin.getValue
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.text.ifEmpty

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

        is GridTool -> {
            { GridToolConfig() }
        }

        is ReferenceOverlayTool -> {
            { ReferenceOverlayToolConfig() }
        }

        is PlayerTool -> {
            { PlayerToolConfig() }
        }

        else -> null
    }

    if (content != null) {
        content()
    }
}


@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
private fun PlayerToolConfig() {
    val toolService by rememberInstance<ToolService>()
    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val scenario by editorService.scenario.collectAsState()
    val tool = toolService.playerTool
    val currentPlayerReference by tool.currentPlayer.collectAsState()
    var playerToMoveOwnership by remember(scenario!!, currentPlayerReference) {
        mutableStateOf(Reference<Int, Player>(scenario!!.players.indices.first { currentPlayerReference.key != it }))
    }
    val currentPlayer = currentPlayerReference.getValueOrNull(scenario!!.players::getOrNull) ?: Unit.let {
        tool.currentPlayer.value = Reference(scenario!!.players.mapIndexed { index, _ -> index }.first())
        return
    }

    var showDialog by remember(currentPlayerReference, scenario) { mutableStateOf(false) }

    var ammoTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentPlayer.ammo.toString(),
                selection = TextRange(currentPlayer.ammo.toString().length) // Or calculate appropriate position
            )
        )
    }

    LaunchedEffect(currentPlayerReference) {
//        println("Changed currentPlayerReference to ${currentPlayerReference.key}")
//        val textValue = ammoTextFieldValue.text.toIntOrNull() ?: return@LaunchedEffect
        ammoTextFieldValue = ammoTextFieldValue.copy(text = currentPlayer.ammo.toString())
    }

    val playerPopupManager = remember { PopupManager() }
    val teamPopupManager = remember { PopupManager() }
    val newOwnerPopupManager = remember { PopupManager() }



    Text("Current Player")
    ComboBox(
        labelText = "${currentPlayerReference.key + 1} ${scenario!!.players[currentPlayerReference.key].team}",
        popupManager = playerPopupManager,
        popupContent = {
            VerticallyScrollableContainer {
                Column {
                    scenario!!.players.withIndex().forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                tool.currentPlayer.value = Reference(item.index)
                                playerPopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = "${item.index + 1} ${scenario!!.players[item.index].team}",
                            )
                        }

                    }
                }
            }
        }
    )

    Spacer(Modifier.height(10.dp))

    Text("Team:")
    ComboBox(
        labelText = "${currentPlayerReference.getValue(scenario!!.players::get).team}",
        popupManager = teamPopupManager,
        popupContent = {
            VerticallyScrollableContainer {
                Column {
                    PlayerTeam.entries.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                val oldList = scenario!!.players
                                val newList = scenario!!.players.mapIndexed { id, player ->
                                    if (currentPlayerReference.key != id) return@mapIndexed player
                                    player.copy(team = item)
                                }
                                val command = UpdatePlayerListCommand(
                                    oldList,
                                    newList
                                )
                                editorService.execute(command)
                                teamPopupManager.setPopupVisible(false)
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

    Text("Ammo:")

    TextField(
        value = ammoTextFieldValue,
        onValueChange = { newValue ->
            ammoTextFieldValue = newValue
            ammoTextFieldValue = ammoTextFieldValue.copy(
                text = newValue.text
                    .replace(Regex("[^0-9]"), "").let { str ->
                        val value = str.toFloatOrNull() ?: return@let str
                        val coercedValue = max(value, 0f)
                        if (coercedValue == value) return@let str
                        coercedValue.toString()
                    }
            )


            val finalText: Int = ammoTextFieldValue.text.ifEmpty { "0" }.toIntOrNull() ?: 0

            val oldList = scenario!!.players
            val newList = scenario!!.players.mapIndexed { id, player ->
                if (currentPlayerReference.key != id) return@mapIndexed player
                player.copy(ammo = finalText)
            }
            val command = UpdatePlayerListCommand(
                oldList,
                newList
            )
            editorService.executeCompound(command)
        },
        modifier = Modifier.onFocusChanged { focus ->
            if (!focus.isFocused) {
                editorService.flushCompound()
            }
        },
        leadingIcon = {
            Row {
                Text("Ammo", color = JewelTheme.globalColors.text.info)
                Spacer(Modifier.width(4.dp))
            }
        }
    )


    data class PlayerMapping(
        val player: Player,
        val oldIndex: Int,
        val newIndex: Int
    )


    fun deletePlayer(newOwner: Reference<Int, Player>?) {
        val oldList = scenario!!.players
        val preparedNewList = oldList.mapIndexedNotNull { index, player ->
            if (index == currentPlayerReference.key) return@mapIndexedNotNull null
            player to index
        }.mapIndexed { id, (player, oldIndex) ->
            PlayerMapping(player, oldIndex, id)
        }
        val newList = preparedNewList.map { it.player }



        val oldUnitList = scenario!!.units
        val oldObjectivesList = scenario!!.objectives

        val newUnitList = oldUnitList.mapNotNull { unit ->
            if (unit.owner == currentPlayerReference) {
                if (newOwner == null) return@mapNotNull null
                return@mapNotNull unit.copy(owner = newOwner)
            }
            val oldIndex = unit.owner.key
            val newIndex = preparedNewList.first { it.oldIndex == oldIndex }.newIndex
            return@mapNotNull unit.copy(owner = Reference(newIndex))
        }

        val newObjectivesList = oldObjectivesList.mapNotNull { objective ->
            if (objective.owner == null) return@mapNotNull objective
            if (objective.owner == currentPlayerReference) {
                if (newOwner == null) return@mapNotNull null
                return@mapNotNull objective.copy(owner = newOwner)
            }
            val oldIndex = objective.owner.key
            val newIndex = preparedNewList.first { it.oldIndex == oldIndex }.newIndex
            return@mapNotNull objective.copy(owner = Reference(newIndex))
        }

        editorService.selectedUnits.value = mutableSetOf()
        editorService.selectedObjectives.value = null

        val command = ComposedCommand(
            listOf(
                UpdatePlayerListCommand(
                    oldList,
                    newList
                ),
                UpdateGameUnitListCommand(
                    oldUnitList,
                    newUnitList
                ),
                WrapCommonToPresetCommand(
                    UpdateObjectiveListCommand(
                        oldObjectivesList,
                        newObjectivesList
                    )
                )
            )
        )
        editorService.execute(command)
//        showDialog = false
    }

    Spacer(Modifier.height(10.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
//        Text("Import reference")
//        Spacer(Modifier.width(4.dp))

        DefaultButton(
            onClick = {
                val oldList = scenario!!.players
                val newList = scenario!!.players.toMutableList()
                newList.add(Player(PlayerTeam.RED, 500))
                val command = UpdatePlayerListCommand(
                    oldList,
                    newList
                )
                editorService.execute(command)
                tool.currentPlayer.value = Reference(newList.size - 1)
            },
        ) {
            Text("Add new player")
        }

        OutlinedButton(onClick = {
            val oldList = scenario!!.players
            val newList = scenario!!.players.toMutableList()
            newList.add(currentPlayer)
            val command = UpdatePlayerListCommand(
                oldList,
                newList
            )
            editorService.execute(command)
            tool.currentPlayer.value = Reference(newList.size - 1)
        }) {
            Text("Duplicate player")
        }



        DefaultButton(
            enabled = scenario!!.players.size > 2,
            style = JewelTheme.defaultButtonStyle.let { style ->
                val color = Color(196, 27, 27, 255)
                val color2 = Color(182, 25, 25, 255)
                val color3 = Color(165, 21, 21, 255)
                ButtonStyle(
                    colors = ButtonColors(
//                        style.colors.background,
                        Brush.linearGradient(listOf(color, color)),
                        style.colors.backgroundDisabled,
                        Brush.linearGradient(listOf(color, color)),
                        Brush.linearGradient(listOf(color3, color3)),
                        Brush.linearGradient(listOf(color2, color2)),
                        style.colors.content,
                        style.colors.contentDisabled,
                        style.colors.contentFocused,
                        style.colors.contentPressed,
                        style.colors.contentHovered,
                        style.colors.border,
                        style.colors.borderDisabled,
                        style.colors.borderFocused,
                        style.colors.borderPressed,
                        style.colors.borderHovered
                    ),
                    metrics = style.metrics,
                    focusOutlineAlignment = style.focusOutlineAlignment
                )
            },
            onClick = {
                if (showDialog) showDialog = false
                else {
                    val anyRelatedUnits = scenario!!.units.any { it.owner == currentPlayerReference }
                    val anyRelatedObjectives = scenario!!.objectives.any { it.owner == currentPlayerReference }
                    if (anyRelatedUnits || anyRelatedObjectives) {
                        showDialog = true
                    } else {
                        deletePlayer(null)
                    }

                }
            },
        ) {
            Text(if (showDialog) "Hide dialog" else "Delete player")
        }


    }
    if (showDialog) {
        Spacer(Modifier.height(10.dp))
        Text("You can't delete this player, because there are units or/and objectives related to this player")
        DefaultButton(
            onClick = {
                deletePlayer(playerToMoveOwnership)
            },
        ) {
            Text("Move ownership")
        }
        ComboBox(
            labelText = "${playerToMoveOwnership.key + 1} ${playerToMoveOwnership.getValue(scenario!!.players::get).team}",
            popupManager = newOwnerPopupManager,
            popupContent = {
                VerticallyScrollableContainer {
                    Column {
                        scenario!!.players.indices.filter { currentPlayerReference.key != it }.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                    playerToMoveOwnership = Reference(item)
                                    newOwnerPopupManager.setPopupVisible(false)
                                }) {
                                Text(
                                    text = "${item + 1} ${scenario!!.players[item].team}",
                                )
                            }

                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = {
            deletePlayer(null)
        }) {
            Text("Remove everything related")
        }
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
            value = newValue
        }, valueRange = 0f..7f,
        steps = 0,
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
        labelText = "${playerIndex + 1} ${scenario!!.players[playerIndex].team}",
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
                                text = "${item.index + 1} ${item.value.team}",
                            )
                        }

                    }
                }
            }
        })


    val nameFieldState by remember { mutableStateOf(TextFieldState(currentUnit.name ?: "")) }

    LaunchedEffect(Unit) {
        snapshotFlow { nameFieldState.text.toString() }
            .collect { text ->
                PlaceUnitTool.currentUnit.value = currentUnit.copy(
                    name = text.takeIf { it.isNotBlank() }
                )
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
            angle,
            color = scenario!!.players[playerIndex].team.color,
            modifier = Modifier.size(200.dp)
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
    var playerIndex = currentObjective.owner?.key
    val scenario by editorService.scenario.collectAsState()
    if (playerIndex != null && !scenario!!.players.indices.contains(playerIndex)) playerIndex = null
    val playerTeamPopupManager = remember { PopupManager() }
    val objectiveTypePopupManager = remember { PopupManager() }

    val objectiveNameTextFieldState = rememberTextFieldState("")

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ComboBox(labelText = playerIndex?.let { "${playerIndex + 1} ${scenario!!.players[playerIndex].team}" } ?: "No one",
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
                                    text = "${item.index + 1} ${item.value.team}",
                                )
                            }

                        }
                    }
                }
            })

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
                                        type = item
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

@Composable
@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
private fun GridToolConfig() {
    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val toolService by rememberInstance<ToolService>()
//    val canvas by rememberInstance<GLCanvas>()


    val size by toolService.gridTool.size.collectAsState()
    val offset by toolService.gridTool.offset.collectAsState()
    val thickness by toolService.gridTool.thickness.collectAsState()
    val color by toolService.gridTool.color.collectAsState()

    val sizeXTextFieldState = rememberTextFieldState(size.x.toString())
    val sizeYTextFieldState = rememberTextFieldState(size.y.toString())

    val offsetXTextFieldState = rememberTextFieldState(offset.x.toString())
    val offsetYTextFieldState = rememberTextFieldState(offset.y.toString())

    val enabled by toolService.gridTool.enabled.collectAsState();

    val controller = rememberColorPickerController()


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            enabled,
            onCheckedChange = {
                toolService.gridTool.enabled.value = it
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Show grid")
    }

    Spacer(Modifier.height(4.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            sizeXTextFieldState,
            leadingIcon = {
                Row {
                    Text("X", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        TextField(
            sizeYTextFieldState,
            leadingIcon = {
                Row {
                    Text("Y", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Size")
    }

    Spacer(Modifier.height(4.dp))


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            offsetXTextFieldState,
            leadingIcon = {
                Row {
                    Text("X", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        TextField(
            offsetYTextFieldState,
            leadingIcon = {
                Row {
                    Text("Y", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Offset")
    }

    Spacer(Modifier.height(4.dp))

    Text("Thickness")
    Spacer(Modifier.height(4.dp))
    Slider(
        thickness,
        onValueChange = {
            toolService.gridTool.thickness.value = it
        },
        valueRange = 0f..max(toolService.gridTool.size.value.x, toolService.gridTool.size.value.y)
    )

    Spacer(Modifier.height(4.dp))


    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .padding(10.dp),
        controller = controller,

        initialColor = color.let {
            Color(
                it.x,
                it.y,
                it.z,
                it.w
            )
        },

        onColorChanged = { colorEnvelope: ColorEnvelope ->
            toolService.gridTool.color.value =
                Vector4f(
                    colorEnvelope.color.red,
                    colorEnvelope.color.green,
                    colorEnvelope.color.blue,
                    colorEnvelope.color.alpha
                )


        }
    )

    BrightnessSlider(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        controller = controller,
        initialColor = color.let {
            Color(
                it.x,
                it.y,
                it.z,
                it.w
            )
        }
    )
    Spacer(Modifier.height(10.dp))

    AlphaSlider(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        controller = controller,
        initialColor = color.let {
            Color(
                it.x,
                it.y,
                it.z,
                it.w
            )
        }
    )

    Spacer(Modifier.height(10.dp))


    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        AlphaTile(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(6.dp)),
            controller = controller
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { sizeXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.size.value
                toolService.gridTool.size.value = Vector2f(it, vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { sizeYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.size.value
                toolService.gridTool.size.value = Vector2f(vec.x, it)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.offset.value
                toolService.gridTool.offset.value = Vector2f(it, vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.offset.value
                toolService.gridTool.offset.value = Vector2f(vec.x, it)
            }
    }


}

@Composable
@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
private fun ReferenceOverlayToolConfig() {
    val toolService by rememberInstance<ToolService>()
    val projectService by rememberInstance<ProjectsService>()
    val projectRef by rememberInstance<ProjectRef>()
    val textureStorage by rememberInstance<TextureStorage>()

    val enabled by toolService.refenceOverlayTool.enabled.collectAsState();
    val scale by toolService.refenceOverlayTool.scale.collectAsState()
    val offset by toolService.refenceOverlayTool.offset.collectAsState()
    val transparency by toolService.refenceOverlayTool.transparency.collectAsState()
    val rotation by toolService.refenceOverlayTool.rotation.collectAsState()

    val scaleXTextFieldState = rememberTextFieldState(scale.x.toString())
    val scaleYTextFieldState = rememberTextFieldState(scale.y.toString())

    val offsetXTextFieldState = rememberTextFieldState(offset.x.toString())
    val offsetYTextFieldState = rememberTextFieldState(offset.y.toString())


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            enabled,
            onCheckedChange = {
                toolService.refenceOverlayTool.enabled.value = it
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Show reference")
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scaleXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.scale.value
                toolService.refenceOverlayTool.scale.value = Vector2f(it, vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scaleYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.scale.value
                toolService.refenceOverlayTool.scale.value = Vector2f(vec.x, it)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.offset.value
                toolService.refenceOverlayTool.offset.value = Vector2f(it.coerceIn(-1f..1f), vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.offset.value
                toolService.refenceOverlayTool.offset.value = Vector2f(vec.x, it.coerceIn(-1f..1f))
            }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            scaleXTextFieldState,
            leadingIcon = {
                Row {
                    Text("X", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        TextField(
            scaleYTextFieldState,
            leadingIcon = {
                Row {
                    Text("Y", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Size")
    }

    Spacer(Modifier.height(4.dp))


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            offsetXTextFieldState,
            leadingIcon = {
                Row {
                    Text("X", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        TextField(
            offsetYTextFieldState,
            leadingIcon = {
                Row {
                    Text("Y", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Offset")
    }


    Spacer(Modifier.height(4.dp))
    Text("Transparency")
    Spacer(Modifier.height(4.dp))
    Slider(
        transparency,
        onValueChange = {
            toolService.refenceOverlayTool.transparency.value = it
        },
        valueRange = 0f..1f
    )

    Spacer(Modifier.height(4.dp))
    Text("Rotation")

    Spacer(Modifier.height(4.dp))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AngleDial(
            rotation,
            color = Color.Green,
            modifier = Modifier.size(200.dp)
        )

        Slider(
            value = rotation,
            onValueChange = {
                toolService.refenceOverlayTool.rotation.value = it
            },
            valueRange = 0f..(2 * Math.PI).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(Modifier.height(4.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
//        Text("Import reference")
//        Spacer(Modifier.width(4.dp))

        DefaultButton(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val pickResult = FileKit.openFilePicker(FileKitType.Image) ?: return@launch
                    projectService.importReference(projectRef, pickResult.file)
                    textureStorage.referenceFile = projectRef.referenceFile

                }
            },
        ) {
            Text("Import reference")
        }

        OutlinedButton(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                projectService.clearReference(projectRef)
                textureStorage.referenceFile = null
            }
        }) {
            Text("Clear reference")
        }


    }


}