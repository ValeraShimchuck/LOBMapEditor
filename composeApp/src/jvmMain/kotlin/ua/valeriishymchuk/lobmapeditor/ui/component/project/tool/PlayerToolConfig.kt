package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.theme.defaultButtonStyle
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.ComposedCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdatePlayerListCommand
import ua.valeriishymchuk.lobmapeditor.commands.WrapCommonToPresetCommand
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.PresetToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.getValue
import kotlin.math.max
import kotlin.text.ifEmpty

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerToolConfig() {
    val diToolService by rememberInstance<ToolService<*>>()
    val toolService = diToolService as? PresetToolService ?: return
    val diEditorService by rememberInstance<EditorService<*>>();
    val editorService = diEditorService as? PresetEditorService ?: return
    val scenario by editorService.scenario.collectAsState()
    scenario ?: return
    val tool = toolService.playerTool
    val currentPlayerReference by tool.currentPlayer.collectAsState()
    val currentPlayer = currentPlayerReference.getValueOrNull(scenario!!.players::getOrNull) ?: Unit.let {
        tool.currentPlayer.value = Reference(scenario!!.players.mapIndexed { index, _ -> index }.first())
        return
    }
    var playerToMoveOwnership by remember(scenario, currentPlayerReference) {
        mutableStateOf(Reference<Int, Player>(scenario!!.players.indices.first { currentPlayerReference.key != it }))
    }

    var showDialog by remember(currentPlayerReference, scenario) { mutableStateOf(false) }

    var ammoTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentPlayer.ammo.toString(),
                selection = TextRange(currentPlayer.ammo.toString().length)
            )
        )
    }



    LaunchedEffect(currentPlayerReference) {
        ammoTextFieldValue = ammoTextFieldValue.copy(text = currentPlayer.ammo.toString())
    }

    var baseAmmoTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentPlayer.baseAmmo.toString(),
                selection = TextRange(currentPlayer.baseAmmo.toString().length)
            )
        )
    }


    LaunchedEffect(currentPlayerReference) {
//        println("Changed currentPlayerReference to ${currentPlayerReference.key}")
//        val textValue = ammoTextFieldValue.text.toIntOrNull() ?: return@LaunchedEffect
        baseAmmoTextFieldValue = baseAmmoTextFieldValue.copy(text = currentPlayer.baseAmmo.toString())
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

    Spacer(Modifier.height(10.dp))

    Text("Base ammo:")

    TextField(
        value = baseAmmoTextFieldValue,
        onValueChange = { newValue ->
            baseAmmoTextFieldValue = newValue
            baseAmmoTextFieldValue = baseAmmoTextFieldValue.copy(
                text = newValue.text
                    .replace(Regex("[^0-9]"), "").let { str ->
                        val value = str.toFloatOrNull() ?: return@let str
                        val coercedValue = max(value, 0f)
                        if (coercedValue == value) return@let str
                        coercedValue.toString()
                    }
            )


            val finalText: Int = baseAmmoTextFieldValue.text.ifEmpty { "0" }.toIntOrNull() ?: 0

            val oldList = scenario!!.players
            val newList = scenario!!.players.mapIndexed { id, player ->
                if (currentPlayerReference.key != id) return@mapIndexed player
                player.copy(baseAmmo = finalText)
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
                Text("Base ammo", color = JewelTheme.globalColors.text.info)
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
            if (objective.owner == currentPlayerReference.key) {
                if (newOwner == null) return@mapNotNull null
                return@mapNotNull objective.copy(owner = newOwner.key)
            }
            val oldIndex = objective.owner
            val newIndex = preparedNewList.first { it.oldIndex == oldIndex }.newIndex
            return@mapNotNull objective.copy(owner = newIndex)
        }

        editorService.selectedObjects.value = setOf()

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
                newList.add(Player(PlayerTeam.RED, 500, 500))
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
                    val anyRelatedObjectives = scenario!!.objectives.any { it.owner == currentPlayerReference.key }
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