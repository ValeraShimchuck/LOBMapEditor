package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameTriggerListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.toVector2f
import ua.valeriishymchuk.lobmapeditor.domain.trigger.*
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.shared.utils.addImmutably
import ua.valeriishymchuk.lobmapeditor.ui.component.common.*
import kotlin.math.roundToInt

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun TriggerToolConfig() {
    // TODO
    // make dropdown for actions, conditions and units within AddUnit
    // test how nested trigger units work
    // test ctrl+z with trigger and nested trigger units
    // remove properties for units, only show name(or team and type) and add 'move to' button

    // add AddObjective action
    // think about how to show orders

    // Also
    // Add additional warning messages if objective with certain name wasn't found
    // make actions:
    // make objects created by actions visible in editor and be treated as units(make visual difference)
    // think of improving actions(adding ability to directly order)

    val toolService by rememberInstance<ToolService<*>>()
    val editorService by rememberInstance<EditorService<*>>()
    val scenarioNullable by editorService.scenario.collectAsState()
    val scenario = scenarioNullable ?: return
    val tool = toolService.triggerTool
    val currentTriggerReference by tool.currentTrigger.collectAsState()
    val currentTrigger: GameTrigger? = currentTriggerReference?.let { triggerReference ->
        val trigger = triggerReference.getValueOrNull(scenario.commonData.triggers::getOrNull)
        if (trigger == null) {
            tool.currentTrigger.value = null
            return
        }
        trigger
    }

    Text("Current Trigger:")
    DefaultVSpacer()
    DropDownNullable(
        currentTrigger,
        scenario.commonData.triggers,
        { idx, value ->
            value.displayText("${idx ?: currentTriggerReference!!.key}")
        },
        { idx, value ->
            tool.currentTrigger.value = value?.let { Reference(idx) }
        }
    )

    @Composable
    fun AddTriggerButton() {
        DefaultButton(
            onClick = {
                val oldList = scenario.triggers
                val newList = oldList.toMutableList()
                newList.add(GameTrigger.DEFAULT)
                val lastIndex = newList.lastIndex
                editorService.execute(
                    UpdateGameTriggerListCommand(
                        oldList,
                        newList
                    )
                )
                tool.currentTrigger.value = Reference(lastIndex)
            },
        ) {
            Text("Add new Trigger")
        }
    }

    if (currentTrigger == null) {
        DefaultVSpacer()
        AddTriggerButton()
        return
    }

    fun updateCurrentTrigger(updater: (GameTrigger) -> GameTrigger, flush: Boolean = true) {
        val reference = currentTriggerReference!!
        val oldList = scenario.triggers
        val newList = scenario.triggers.mapIndexed { idx, value ->
            if (idx != reference.key) return@mapIndexed value
            updater(value)
        }
        val command = UpdateGameTriggerListCommand(
            oldList,
            newList
        )
        if (flush) {
            editorService.execute(command)
        } else {
            editorService.executeCompound(command)
        }
    }

    fun updateCurrentTrigger(updater: (GameTrigger) -> GameTrigger) {
        updateCurrentTrigger(updater, flush = true)
    }

    DefaultVSpacer()

    CenteredRow {

        AddTriggerButton()

        LongHSpacer()

        RedButton(
            "Delete Trigger"
        ) {
            val list = scenario.triggers
            val newList = list.filterIndexed { idx, _ ->
                currentTriggerReference!!.key != idx
            }

            editorService.execute(
                UpdateGameTriggerListCommand(
                    list,
                    newList
                )
            )

            tool.currentTrigger.value = null
        }

    }

    DefaultVSpacer()

    Text("Triggers settings:")
    DefaultVSpacer()
    // event
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Event: ")

        DropDown(
            currentTrigger.eventType,
            EventTriggerType.entries,
            { _, value ->
                value.displayName
            },
            { _, value ->
                updateCurrentTrigger { trigger ->
                    trigger.copy(
                        eventType = value
                    )
                }
            }
        )
    }

    DefaultVSpacer()

    // Condition Logic
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Condition Logic: ")

        DropDown(
            currentTrigger.conditionLogicType,
            ConditionLogicType.entries,
            { _, value ->
                value.displayName
            },
            { _, value ->
                updateCurrentTrigger { trigger ->
                    trigger.copy(
                        conditionLogicType = value
                    )
                }
            }
        )
    }

    SimpleDivider()


    var currentConditionReference: Int? by remember(currentTriggerReference) {
        mutableStateOf(currentTrigger.conditions.indices.firstOrNull())
    }

    CenteredRow {
        Text("Current Condition: ")
        DropDownNullable(
            currentConditionReference,
            currentTrigger.conditions.indices.toList(),
            { _, id ->
                "$id ${currentTrigger.conditions[id].key}"
            },
            { _, selection ->
                currentConditionReference = selection
            }
        )
    }

    DefaultVSpacer()


    @Composable
    fun AddConditionButton() {
        DefaultButton(
            onClick = {
                updateCurrentTrigger { trigger ->
                    trigger.copy(
                        conditions = trigger.conditions.toMutableList().also {
                            it.add(Condition.ConditionEnum.IS_TURN.default)
                        }
                    )
                }
            },
        ) {
            Text("Add new condition")
        }
    }

    val finalConditionReference = currentConditionReference
    if (finalConditionReference != null) {
        CenteredRow {
            AddConditionButton()

            LongHSpacer()

            RedButton("Delete Condition") {
                updateCurrentTrigger { trigger ->
                    trigger.copy(conditions = trigger.conditions.filterIndexed { idx, _ ->
                        finalConditionReference != idx
                    })
                }
            }
        }

        val condition: Condition = currentTrigger.conditions[finalConditionReference]

        fun updateCondition(updater: (Condition) -> Condition) {
            updateCurrentTrigger { trigger ->
                trigger.copy(
                    conditions = trigger.conditions.mapIndexed { idx2, condition2 ->
                        if (idx2 != finalConditionReference) return@mapIndexed condition2
                        updater(condition2)
                    }
                )
            }
        }

        fun <T : Condition> updatedConditionTyped(condition: T, updater: (T) -> T) {
            updateCondition { _ ->
                updater(condition)
            }
        }

        DefaultVSpacer()


        CenteredRow {
            Text("Condition Type: ")
            DropDown(
                condition.enumRepresentation,
                Condition.ConditionEnum.entries,
                { _, value ->
                    println("Value: ${value.displayName}")
                    value.displayName
                },
                { _, value ->
                    updateCondition { _ ->
                        value.default
                    }
                }
            )

        }

        DefaultVSpacer()


        when (condition) {
            is Condition.Chance -> {
                val chance = condition.chance
                var value by remember { mutableStateOf(chance) }

                LaunchedEffect(value) {
                    updatedConditionTyped(condition) {
                        it.copy(chance = value)
                    }
                }

                Text("Chance: %.2f".format(chance))

                Slider(
                    value = value, // Float
                    onValueChange = { newValue ->
                        value = newValue
                    }, valueRange = 0f..100f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth()
                )


            }

            is Condition.IsTurn -> {
                val turn = condition.turn
                var value by remember { mutableStateOf(turn.toFloat()) }

                LaunchedEffect(value) {
                    updatedConditionTyped(condition) {
                        it.copy(turn = value.roundToInt())
                    }
                }

                Text("Turn: $turn")

                Slider(
                    value = value, // Float
                    onValueChange = { newValue ->
                        value = newValue
                    }, valueRange = 1f..60f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is Condition.IsTurnGreaterThan -> {
                val turn = condition.value
                var value by remember { mutableStateOf(turn.toFloat()) }

                LaunchedEffect(value) {
                    updatedConditionTyped(condition) {
                        it.copy(value = value.roundToInt())
                    }
                }

                Text("Turn: $turn")

                Slider(
                    value = value, // Float
                    onValueChange = { newValue ->
                        value = newValue
                    }, valueRange = 1f..60f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is Condition.IsTurnLessThan -> {
                val turn = condition.value
                var value by remember { mutableStateOf(turn.toFloat()) }

                LaunchedEffect(value) {
                    updatedConditionTyped(condition) {
                        it.copy(value = value.roundToInt())
                    }
                }

                Text("Turn: $turn")

                Slider(
                    value = value, // Float
                    onValueChange = { newValue ->
                        value = newValue
                    }, valueRange = 1f..60f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is Condition.IsTurnMultipleOf -> {
                val multiple = condition.multiple
                val offset = condition.offset
                var value by remember { mutableStateOf(multiple.toFloat()) }

                LaunchedEffect(value) {
                    updatedConditionTyped(condition) {
                        it.copy(multiple = value.roundToInt())
                    }
                }

                Text("Multiple: $multiple")

                Slider(
                    value = value, // Float
                    onValueChange = { newValue ->
                        value = newValue
                    }, valueRange = 1f..60f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth()
                )

                var value2 by remember { mutableStateOf(offset.toFloat()) }

                LaunchedEffect(value2) {
                    updatedConditionTyped(condition) {
                        it.copy(offset = value.roundToInt())
                    }
                }

                Text("Offset: $multiple")

                Slider(
                    value = value2, // Float
                    onValueChange = { newValue ->
                        value2 = newValue
                    }, valueRange = 1f..60f,
                    steps = 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is Condition.IsUnitNotAlive -> {
                var textFieldValue by remember(currentTrigger) {
                    mutableStateOf(
                        TextFieldValue(
                            text = condition.unitName,
                            selection = TextRange(condition.unitName.length)
                        )
                    )
                }



                Spacer(Modifier.height(4.dp))

                Text("Unit:")
                TextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue

                        val finalText: String = newValue.text
                        updatedConditionTyped(condition) { condition ->
                            condition.copy(
                                unitName = finalText
                            )

                        }
                    },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    placeholder = { Text("Empty") }
                )
            }

            is Condition.IsUnitRouting -> {
                var textFieldValue by remember(currentTrigger) {
                    mutableStateOf(
                        TextFieldValue(
                            text = condition.unitName,
                            selection = TextRange(condition.unitName.length)
                        )
                    )
                }



                Spacer(Modifier.height(4.dp))

                Text("Unit:")
                TextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue

                        val finalText: String = newValue.text
                        updatedConditionTyped(condition) { condition ->
                            condition.copy(
                                unitName = finalText
                            )

                        }
                    },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    placeholder = { Text("Empty") }
                )
            }

            is Condition.IsVar -> {
                var textFieldValue by remember(currentTrigger) {
                    mutableStateOf(
                        TextFieldValue(
                            text = condition.name,
                            selection = TextRange(condition.name.length)
                        )
                    )
                }



                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Var:")
                    TextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue

                            val finalText: String = newValue.text
                            updatedConditionTyped(condition) { condition ->
                                condition.copy(
                                    name = finalText
                                )

                            }
                        },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                            if (!focus.isFocused) {
                                editorService.flushCompound()
                            }
                        },
                        placeholder = { Text("Empty") }
                    )
                }


                var value by remember {
                    val text = condition.value.toString()
                    mutableStateOf(
                        TextFieldValue(
                            text = text,
                            selection = TextRange(text.length)
                        )
                    )
                }

                LaunchedEffect(currentTrigger) {
                    value = value.copy(text = condition.value.toString())
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Value:")
                    TextField(
                        value = value,
                        onValueChange = { newValue ->
                            value = newValue
                            value = value.copy(
                                text = newValue.text
                                    .replace(Regex("[^0-9.-]"), "").let { str ->
                                        val value = str.toFloatOrNull() ?: return@let str
                                        value.toString()
                                    }
                            )


                            val finalText: Float = value.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f

                            updatedConditionTyped(condition) { condition ->
                                condition.copy(value = finalText)
                            }
                        },
                        modifier = Modifier.onFocusChanged { focus ->
                            if (!focus.isFocused) {
                                editorService.flushCompound()
                            }
                        },
                        leadingIcon = {
                            Row {
                                Text("Value", color = JewelTheme.globalColors.text.info)
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                    )
                }

                val isHidden = condition.not == true

                Spacer(Modifier.height(4.dp)) // hide checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text("Not Equals(Not):")
                    Spacer(Modifier.width(4.dp))
                    Checkbox(isHidden, onCheckedChange = {
                        updatedConditionTyped(condition) { condition ->
                            if (it) {
                                condition.copy(not = true)
                            } else {
                                condition.copy(not = null)
                            }


                        }
                    })
                }

            }

            is Condition.ObjectiveBelongsTo -> {
                var objectiveValue by remember(currentTrigger) {
                    mutableStateOf(
                        TextFieldValue(
                            text = condition.objectiveName,
                            selection = TextRange(condition.objectiveName.length)
                        )
                    )
                }



                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Objective:")
                    TextField(
                        value = objectiveValue,
                        onValueChange = { newValue ->
                            objectiveValue = newValue

                            val finalText: String = newValue.text
                            updatedConditionTyped(condition) { condition ->
                                condition.copy(
                                    objectiveName = finalText
                                )

                            }
                        },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                            if (!focus.isFocused) {
                                editorService.flushCompound()
                            }
                        },
                        placeholder = { Text("Empty") }
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Team:")
                    DropDownNullable(
                        condition.team,
                        PlayerTeam.entries,
                        { _, team ->
                            team.displayName
                        },
                        { _, team ->
                            updatedConditionTyped(condition) { condition ->
                                condition.copy(team = team)
                            }
                        }
                    )
                }

                var playerValue by remember {
                    val text = (condition.player ?: 0).toString()
                    mutableStateOf(
                        TextFieldValue(
                            text = text,
                            selection = TextRange(text.length)
                        )
                    )
                }

                LaunchedEffect(currentTrigger) {
                    playerValue = playerValue.copy(text = (condition.player ?: 0).toString())
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Player:")
                    TextField(
                        value = playerValue,
                        onValueChange = { newValue ->
                            playerValue = newValue
                            playerValue = playerValue.copy(
                                text = newValue.text
                                    .replace(Regex("[^0-9]"), "").let { str ->
                                        val value = str.toIntOrNull() ?: return@let str
                                        value.toString()
                                    }
                            )


                            val finalText: Int = playerValue.text.ifEmpty { "0" }.toIntOrNull() ?: 0

                            updatedConditionTyped(condition) { condition ->
                                if (finalText <= 0) {
                                    condition.copy(player = null)
                                } else {
                                    condition.copy(player = finalText)
                                }

                            }
                        },
                        modifier = Modifier.onFocusChanged { focus ->
                            if (!focus.isFocused) {
                                editorService.flushCompound()
                            }
                        },
                        leadingIcon = {
                            Row {
                                Text("Value", color = JewelTheme.globalColors.text.info)
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                    )
                }


            }

            is Condition.UnitMovedThisTurn -> {
                var textFieldValue by remember(currentTrigger) {
                    mutableStateOf(
                        TextFieldValue(
                            text = condition.name,
                            selection = TextRange(condition.name.length)
                        )
                    )
                }



                Spacer(Modifier.height(4.dp))

                Text("Unit:")
                TextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue

                        val finalText: String = newValue.text
                        updatedConditionTyped(condition) { condition ->
                            condition.copy(
                                name = finalText
                            )

                        }
                    },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    placeholder = { Text("Empty") }
                )
            }
        }



    }
    else {
        AddConditionButton()
    }

    SimpleDivider()

    var currentActionReference: Int? by remember(currentTriggerReference) {
        mutableStateOf(currentTrigger.actions.indices.firstOrNull())
    }


    CenteredRow {
        Text("Current Action: ")

        DropDownNullable(
            currentActionReference,
            currentTrigger.actions.indices.toList(),
            { _, id ->
                "$id ${currentTrigger.actions[id].enumRepresentation.key}"
            },
            { _, selection ->
                currentActionReference = selection
            }
        )
    }

    @Composable
    fun AddActionButton() {
        BlueButton("Add Action") {
            updateCurrentTrigger { trigger ->
                trigger.copy(actions = trigger.actions.addImmutably(GameAction.ActionEnum.ADD_UNIT.default))
            }
        }
    }

    DefaultVSpacer()


    val finalActionReference = currentActionReference
    if (finalActionReference != null) {

        CenteredRow {
            AddActionButton()

            LongHSpacer()

            RedButton("Delete Action") {
                updateCurrentTrigger { trigger ->
                    trigger.copy(actions = trigger.actions.filterIndexed { idx, _ ->
                        finalActionReference != idx
                    })
                }
            }

        }

        val action = currentTrigger.actions[finalActionReference]
        fun updateAction(updater: (GameAction) -> GameAction, flush: Boolean = true) {
            updateCurrentTrigger({ trigger ->
                trigger.copy(
                    actions = trigger.actions.mapIndexed { idx2, condition2 ->
                        if (idx2 != finalActionReference) return@mapIndexed condition2
                        updater(condition2)
                    }
                )
            }, flush)
        }

        fun updateAction(updater: (GameAction) -> GameAction) {
            updateAction(updater, flush = true)
        }

        fun <T : GameAction> updateActionTyped(action: T, updater: (T) -> T, flush: Boolean = true) {
            updateAction({ _ ->
                updater(action)
            }, flush)
        }

        fun <T : GameAction> updateActionTyped(action: T, updater: (T) -> T) {
            updateActionTyped(action, updater, flush = true)
        }

        Text("Action Type:")
        DropDown(
            action.enumRepresentation,
            GameAction.ActionEnum.entries.toList(),
            { _, enum -> enum.name },
            { _, enum ->
                if (enum == action.enumRepresentation) return@DropDown
                updateAction {
                    enum.default
                }
            }

        )

        DefaultVSpacer()

        when (action) {
            is GameAction.AddUnit -> {
                Text("Units")
                DefaultVSpacer()
                action.gameUnits.forEachIndexed { unitId, unit ->
                    fun updateUnit(unit: GameUnit?) {
                        updateActionTyped(
                            action,
                            { action ->
                                action.copy(gameUnits = action.gameUnits.mapIndexedNotNull { id2, mapUnit ->
                                    if (unitId != id2) return@mapIndexedNotNull mapUnit
                                    unit
                                })
                            },
                            flush = false
                        )
                    }
                    CenteredRow {
                        val unitTeam = (scenario as? GameScenario.Preset)?.players?.get(unit.owner.key)?.team
                        Text("$unitId ${unit.name ?: let {
                            if (unitTeam != null) "${unit.type.name} $unitTeam"
                            else unit.type.name
                        }}")

                        IconActionButton(AllIconsKeys.Actions.MoveToButton, null, onClick = {

                            editorService.cameraPosition = unit.position.toVector2f()


                        })

                        IconActionButton(AllIconsKeys.General.Delete, null, onClick = {
                            updateUnit(null)
                            editorService.flushCompound()
                        })

                    }

                }

                DefaultVSpacer()

                BlueButton("Add Unit") {
                    updateActionTyped(action) { action ->
                        action.copy(gameUnits = action.gameUnits.addImmutably(GameUnit.DEFAULT))
                    }
                }

            }

            is GameAction.AddTrigger -> TODO()
            is GameAction.DefeatPlayer -> TODO()
            is GameAction.EndGame -> TODO()
            is GameAction.MoveCamera -> TODO()
            is GameAction.OrderUnit -> TODO()
            is GameAction.RemoveUnit -> TODO()
            is GameAction.SetVar -> TODO()
            is GameAction.ShowMessage -> TODO()
            is GameAction.SpawnNeutralObjectives -> TODO()
        }

        DefaultVSpacer()

    } else {
        AddActionButton()
    }

}