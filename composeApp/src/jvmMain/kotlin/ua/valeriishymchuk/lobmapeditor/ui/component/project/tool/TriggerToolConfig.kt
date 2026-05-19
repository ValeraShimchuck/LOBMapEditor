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
import java.lang.IllegalStateException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun TriggerToolConfig() {
    // TODO


    // implement move camera, make it as a reference object

    // then finish the final boss

    // try make other actions other than order
    // think about how to show orders

    // Also
    // Add additional warning messages if objective with certain name wasn't found
    // make actions:
    // show relative coordinates of spawned neutral objectives
    // think of improving actions(adding ability to directly order)
    // show units those might be affected by remove unit action
    // Show move camera action on the map(maybe make it as an object that can be dragged
    // If remove unit doesn't affect anyone then write a warning message
    // If war is not defined anywhere(when polling in condition) - write a warning message



    // At some point add a tool that will convert replay to map
    // reference https://github.com/egueneysaye/Replay_to_Scenario/blob/main/index.html

    val editorService by rememberInstance<EditorService<*>>()
    val scenarioNullable by editorService.scenario.collectAsState()
    val scenario = scenarioNullable ?: return

    TriggerComponent(Unit, editorService.scenario.value!!.triggers, { newTriggerList, flush ->
        val command = UpdateGameTriggerListCommand(
            scenario.triggers,
            newTriggerList
        )
        if (flush) {
            editorService.execute(command)
        } else {
            editorService.executeCompound(command)
        }
    }, { editorService.flushCompound() })

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalJewelApi::class)
@Composable
private fun TriggerComponent(
    rememberKey: Any,
    triggerList0: List<GameTrigger>,
    updateTriggerList: (List<GameTrigger>, Boolean) -> Unit, // newList, flush
    flush: () -> Unit,
) {

    val editorService by rememberInstance<EditorService<*>>()
    var triggerList by remember(rememberKey) { mutableStateOf(triggerList0) }


    var currentTriggerReference: Int? by remember(rememberKey) {
        mutableStateOf(null)
    }

    val currentTrigger: GameTrigger? = currentTriggerReference?.let { ref ->
        triggerList.getOrNull(ref) ?: Unit.let {
            currentTriggerReference = null
            return
        }
    }


    CenteredRow {
        Text("Current Trigger: ")
        DropDownNullable(
            currentTrigger,
            triggerList,
            { idx, value ->
                value.displayText("${idx ?: currentTriggerReference}")
            },
            { idx, _ ->
                currentTriggerReference = idx
            }
        )
    }


    @Composable
    fun AddTriggerButton() {
        DefaultButton(
            onClick = {
                val newList = triggerList.toMutableList()
                newList.add(GameTrigger.DEFAULT)
                val lastIndex = newList.lastIndex
                updateTriggerList(newList, true)
//                editorService.execute(
//                    UpdateGameTriggerListCommand(
//                        oldList,
//                        newList
//                    )
//                )
//                tool.currentTrigger.value = Reference(lastIndex)
                currentTriggerReference = lastIndex
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

    val nonNullReference = currentTriggerReference!!

    fun updateCurrentTrigger(updater: (GameTrigger) -> GameTrigger, flush: Boolean = true) {
        val reference = currentTriggerReference
        println("Current trigger list: ${triggerList}")
        val newList = triggerList.mapIndexed { idx, value ->
            if (idx != reference) return@mapIndexed value
            updater(value)
        }

        triggerList = newList
        updateTriggerList(newList, flush)

//        val command = UpdateGameTriggerListCommand(
//            oldList,
//            newList
//        )
//        if (flush) {
//            editorService.execute(command)
//        } else {
//            editorService.executeCompound(command)
//        }
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
            val newList = triggerList.filterIndexed { idx, _ ->
                currentTriggerReference != idx
            }

            updateTriggerList(newList, true)


//            editorService.execute(
//                UpdateGameTriggerListCommand(
//                    list,
//                    newList
//                )
//            )
//
//            tool.currentTrigger.value = null
            currentTriggerReference = null
        }

    }

    DefaultVSpacer()

    Text("Triggers settings:")
    DefaultVSpacer()
    // event
    CenteredRow {
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
            },
            modifier = Modifier.widthIn(max = 130.dp)
        )

    }

    DefaultVSpacer()

    // Condition Logic
    CenteredRow {
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
            },
            modifier = Modifier.widthIn(max = 170.dp)
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
            },
            modifier = Modifier.widthIn(max = 180.dp)
        )
    }

    DefaultVSpacer()


    @Composable
    fun AddConditionButton() {
        DefaultButton(
            onClick = {
                var newId: Int? = null
                updateCurrentTrigger { trigger ->
                    val newTriggers = trigger.copy(
                        conditions = trigger.conditions.toMutableList().also {
                            it.add(Condition.ConditionEnum.IS_TURN.default)
                        }
                    )
                    newId = newTriggers.conditions.lastIndex
                    newTriggers
                }

                currentConditionReference = newId

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
                },

                modifier = Modifier.widthIn(max = 180.dp)
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
                            flush()
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
                            flush()
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
                                flush()
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
                                flush()
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
                                flush()
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
                                flush()
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
                            flush()
                        }
                    },
                    placeholder = { Text("Empty") }
                )
            }
        }


    } else {
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
            },
            modifier = Modifier.widthIn(max = 180.dp)
        )
    }

    @Composable
    fun AddActionButton() {
        BlueButton("Add Action") {
            var newId: Int? = null
            updateCurrentTrigger { trigger ->
                val newTrigger = trigger.copy(
                    actions = trigger.actions.addImmutably(GameAction.ActionEnum.ADD_UNIT.default)
                )
                newId = newTrigger.actions.lastIndex
                newTrigger
            }

            currentActionReference = newId
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
            updateAction({ actualAction ->
                val castedAction = (actualAction as? T) ?: throw IllegalStateException("Wtf")
                updater(castedAction)
            }, flush)
        }

        fun <T : GameAction> updateActionTyped(action: T, updater: (T) -> T) {
            updateActionTyped(action, updater, flush = true)
        }

        CenteredRow {
            Text("Action Type: ")
            DropDown(
                action.enumRepresentation,
                GameAction.ActionEnum.entries.toList(),
                { _, enum -> enum.name },
                { _, enum ->
                    if (enum == action.enumRepresentation) return@DropDown
                    updateAction {
                        enum.default
                    }
                },
                modifier = Modifier.widthIn(max = 200.dp)

            )
        }

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
                        val unitTeam =
                            (editorService.scenario.value as? GameScenario.Preset)?.players?.get(unit.owner.key)?.team
                        Text(
                            "$unitId ${
                                unit.name ?: let {
                                    if (unitTeam != null) "${unit.type.name} $unitTeam"
                                    else unit.type.name
                                }
                            }"
                        )

                        IconActionButton(AllIconsKeys.Actions.MoveToButton, null, onClick = {

                            editorService.cameraPosition = unit.position.toVector2f()


                        })

                        IconActionButton(AllIconsKeys.General.Delete, null, onClick = {
                            updateUnit(null)
                            flush()
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

            is GameAction.AddTrigger -> {

                TriggerComponent(
                    nonNullReference,
                    action.triggers,
                    { newList, flush ->
                        updateAction({
                            action.copy(triggers = newList)
                        }, flush)
                    },
                    flush
                )
            }

            is GameAction.DefeatPlayer -> {
                Text("Player")
                IntTextField(
                    finalActionReference,
                    {
                        action.player.key
                    },
                    { newId ->
                        updateActionTyped(action, {
                            it.copy(player = Reference(newId))
                        }, false)
                    },
                    flush
                )
                // TODO show if player does not exist(only in preset mode)
                // keep in mind that internal list starts from 0, where as LoB's ids start from 1

            }
            is GameAction.EndGame -> {
                Text("Reason:")
                DropDown(
                    action.reason,
                    GameAction.GameEndReason.entries.toList(),
                    { _, reason -> reason.name },
                    { _, reason ->
                        updateActionTyped(action, {
                            it.copy(
                                reason = reason
                            )
                        })
                    }
                )
            }
            is GameAction.MoveCamera -> TODO()
            is GameAction.OrderUnit -> TODO()
            is GameAction.RemoveUnit -> {
                Text("Units:")
                println("Units to be removed: ${action.units}")
                action.units.forEachIndexed { id, name ->
                    ReactiveTextField(finalActionReference to id, name, { newName ->
                        updateActionTyped(action, { action ->
                            println("${action.units}")
                            println("Got new name: ${newName}")
                            action.copy(
                                units = action.units.toMutableList().also {
                                    it[id] = newName
                                }
                            )
                        }, false)
                        println(action.units)
                    },
                        onFocusLoss = flush)
                }
            }
            is GameAction.SetVar -> {
                // name
                Text("Name:")
                ReactiveTextField(
                    finalActionReference,
                    action.name,
                    { newText ->
                        updateActionTyped(action, {
                            it.copy(
                                name = newText
                            )
                        }, false)
                    },
                    onFocusLoss = flush
                )
                DefaultVSpacer()
                Text("Value:")
                FloatTextField(
                    finalActionReference,
                    { action.value },
                    {  newValue ->
                        updateActionTyped(action, {
                            it.copy(
                                value = newValue
                            )
                        }, false)
                    },
                    flush
                )
                // value
            }
            is GameAction.ShowMessage -> {
                // title
                Text("Title:")
                ReactiveTextField(
                    finalActionReference,
                    action.title,
                    { newText ->
                        updateActionTyped(action, {
                            it.copy(
                                title = newText
                            )
                        }, false)
                    },
                    onFocusLoss = flush
                )
                DefaultVSpacer()
                // message
                Text("Message:")
                ReactiveTextField(
                    finalActionReference,
                    action.message,
                    { newText ->
                        updateActionTyped(action, {
                            it.copy(
                                message = newText
                            )
                        }, false)
                    },
                    onFocusLoss = flush
                )
            }
            is GameAction.SpawnNeutralObjectives -> {
                // per battle amount
                val battleAmount = action.amount

                CenteredRow {
                    Text("Amount of objectives per battle type")
                    Checkbox(battleAmount != null, { newState ->
                        if (newState) {
                            updateActionTyped(action) {
                                it.copy(
                                    amount = (GameAction.ActionEnum.SPAWN_NEUTRAL_OBJECTIVES.default as GameAction.SpawnNeutralObjectives).amount
                                )
                            }
                        } else {
                            updateActionTyped(action) {
                                it.copy(
                                    amount = null
                                )
                            }
                        }
                    })
                }

                if (battleAmount != null) {
                    DefaultVSpacer()
                    GameAction.BattleType.entries.forEach { battleType ->
                        CenteredRow {
                            Text(battleType.displayName)
                            DefaultHSpacer()
                            IntTextField(
                                finalActionReference, {
                                    battleAmount[battleType] ?: 1
                                },
                                {
                                    updateActionTyped(action) { action ->
                                        val newAmount = battleAmount.toMutableMap()
                                        newAmount[battleType] = it
                                        action.copy(
                                            amount = newAmount
                                        )
                                    }
                                }, adjustValue = { max(it, 1) },
                                modifier = Modifier,
                                onFocusLoss = flush
                            )

                        }
                    }

                }

                DefaultVSpacer()


                // min/max positions, they are from 0-1 actually, no idea what is that, probably it is relative to the map

                println("Current action positions: $action")

                val hasPositions = listOf(
                    action.minX,
                    action.maxX,
                    action.minY,
                    action.maxY
                ).all { it != null }
                CenteredRow {
                    Text("Objective Spawn Relative Boundaries")
                    Checkbox(
                        hasPositions,
                        { state ->
                            if (state) {
                                updateActionTyped(action) { typedAction ->
                                    typedAction.copy(
                                        minX = 0.0f,
                                        maxX = 1.0f,
                                        minY = 0.0f,
                                        maxY = 1.0f
                                    )
                                }
                            } else {
                                updateActionTyped(action) { typedAction ->
                                    typedAction.copy(
                                        minX = null,
                                        maxX = null,
                                        minY = null,
                                        maxY = null
                                    )
                                }
                            }
                        })
                }

                if (hasPositions) {
                    DefaultVSpacer()
                    CenteredRow {
                        Text("Start")
                        DefaultHSpacer()
                        Text("X")
                        Slider(
                            action.minX!!, {
                                updateActionTyped(
                                    action,
                                    { actionTyped ->
                                        actionTyped.copy(
                                            minX = it
                                        )
                                    }, false
                                )
                            }, onValueChangeFinished = {
                                flush()
                            }, modifier = Modifier.weight(0.5f)
                        )

                        DefaultHSpacer()
                        Text("Y")
                        Slider(
                            action.minY!!, {
                                updateActionTyped(
                                    action,
                                    { actionTyped ->
                                        actionTyped.copy(
                                            minY = it
                                        )
                                    }, false
                                )
                            }, onValueChangeFinished = {
                                flush()
                            },
                            modifier = Modifier.weight(0.5f)
                        )

                    }

                    CenteredRow {
                        Text("End")
                        DefaultHSpacer()
                        Text("X")
                        Slider(
                            action.maxX!!, {
                                updateActionTyped(
                                    action,
                                    { actionTyped ->
                                        actionTyped.copy(
                                            maxX = it
                                        )
                                    }, false
                                )
                            }, onValueChangeFinished = {
                                flush()
                            },
                            modifier = Modifier.weight(0.5f)
                        )

                        DefaultHSpacer()
                        Text("Y")
                        Slider(
                            action.maxY!!, {
                                updateActionTyped(
                                    action,
                                    { actionTyped ->
                                        actionTyped.copy(
                                            maxY = it
                                        )
                                    },
                                    false,
                                )
                            }, onValueChangeFinished = {
                                flush()
                            },
                            modifier = Modifier.weight(0.5f)
                        )

                    }

                }
                DefaultVSpacer()


                // ojbective spawn orientation
                CenteredRow {
                    Text("Orientation")
                    Checkbox(action.orientation != null, { state ->
                        if (state) {
                            updateActionTyped(action) { action ->
                                action.copy(
                                    orientation = GameAction.ObjectiveSpawnOrientation.entries.first()
                                )
                            }
                        } else {
                            updateActionTyped(action) { action ->
                                action.copy(
                                    orientation = null
                                )
                            }
                        }
                    })


                    val orientation = action.orientation
                    if (orientation != null) {
                        DropDown(orientation, GameAction.ObjectiveSpawnOrientation.entries.toList(), { _, orientation ->
                            orientation.name

                        }, { _, orientation ->
                            updateActionTyped(action) { action ->
                                action.copy(
                                    orientation = orientation
                                )
                            }
                        })
                    }

                }

                // spacing
                CenteredRow {
                    Text("Spacing")
                    val spacing = action.spacing
                    Checkbox(spacing != null, { state ->
                        if (state) {
                            updateActionTyped(action) { action ->
                                action.copy(
                                    spacing = 0.25f
                                )
                            }
                        } else {
                            updateActionTyped(action) { action ->
                                action.copy(
                                    spacing = null
                                )
                            }
                        }
                    })

                    if (spacing != null) {
                        FloatTextField(spacing, { it }, {
                            updateActionTyped(
                                action,
                                { action ->
                                    action.copy(
                                        spacing = it
                                    )
                                },
                                false
                            )
                        }, flush, adjustValue = { number ->
                            max(number, 0.0f)
                        }, modifier = Modifier)
                    }

                }

            }
        }

        DefaultVSpacer()

    } else {
        AddActionButton()
    }

}
