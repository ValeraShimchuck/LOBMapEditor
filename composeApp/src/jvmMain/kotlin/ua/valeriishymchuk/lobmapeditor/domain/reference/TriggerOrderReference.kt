package ua.valeriishymchuk.lobmapeditor.domain.reference

import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameTriggerListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.address.ObjectAddress
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameAction
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger
import kotlin.reflect.KClass

data class TriggerOrderReference(
    val objectAddress: ObjectAddress
) {

    val triggerId get() = objectAddress.triggerId
    val actionId get() = objectAddress.actionId
    val address get() = objectAddress.address

    val flatAddress by lazy {
        listOf(
            listOf(triggerId, actionId),
            address
        ).flatten()
    }

    val objectId by lazy {
        flatAddress.last()
    }


    private fun traverseAddress(
        triggers: List<GameTrigger>,
        actionHandler: (Int, GameAction.AddTrigger) -> Unit = { _, _ -> },
        associatedActionHandler: (GameAction.OrderUnit) -> Unit = {},
        address: List<Int> = flatAddress,
        addressId: Int = 0
    ) {
        val triggerId = address[0]
        val actionId = address[1]
        val newAddress = address.drop(2)
        if (newAddress.isEmpty()) {
            associatedActionHandler(triggers[triggerId].actions[actionId] as GameAction.OrderUnit)
            return
        }
        val addTriggersAction = triggers[triggerId].actions[actionId] as GameAction.AddTrigger
        actionHandler(addressId, addTriggersAction)
        traverseAddress(addTriggersAction.triggers, actionHandler, associatedActionHandler, newAddress, addressId + 2)

    }

    fun dereference(scenario: GameScenario<*>): GameAction.OrderUnit? {
        var associatedActionObject: GameAction.OrderUnit? = null
        traverseAddress(scenario.triggers, associatedActionHandler = {
            associatedActionObject = it
        })
        return associatedActionObject
    }


    fun <R> traverseModifySelf(
        triggers: List<GameTrigger>,
        modification: (List<GameAction>) -> Pair<List<GameAction>, R>
    ): Pair<List<GameTrigger>, R> {
        val intListAddress = flatAddress
        return traverseModify(triggers, intListAddress, modification)
    }

    fun <R> traverseModify(
        triggers: List<GameTrigger>,
        address: List<Int>,
        modification: (List<GameAction>) -> Pair<List<GameAction>, R>
    ): Pair<List<GameTrigger>, R> {
        val triggerId = address[0]
        val actionId = address[1]
        val newAddress = address.drop(2)
        val isLast = newAddress.isEmpty()
        val trigger = triggers[triggerId]
        if (isLast) {
            val actionList = trigger.actions
            val (newActions, result) = modification(actionList)
            val newTriggerList = triggers.toMutableList()
            newTriggerList[triggerId] = trigger.copy(
                actions = newActions
            )
            return newTriggerList to result
        }
        val actionsCopy = trigger.actions.toMutableList()
        val addTriggersAction = actionsCopy[actionId] as GameAction.AddTrigger
        val (subTriggerList, result) = traverseModify(addTriggersAction.triggers, newAddress, modification)
        val newTriggerList = triggers.toMutableList()
        actionsCopy[actionId] = addTriggersAction.copy(triggers = subTriggerList)
        newTriggerList[triggerId] = trigger.copy(
            actions = actionsCopy
        )
        return newTriggerList to result
    }


    private fun removeItself(triggers: List<GameTrigger>): List<GameTrigger> {
        val intListAddress = flatAddress
        val (newTriggerList, _) = traverseModify(triggers, intListAddress) { actions ->
            val newActions = actions.toMutableList()
            newActions.removeAt(intListAddress.last())
            newActions to Unit
        }

        return newTriggerList
    }

    private fun duplicateItself(triggers: List<GameTrigger>): Pair<List<GameTrigger>, TriggerOrderReference> {
        var associatedAction: GameAction.OrderUnit? = null
        traverseAddress(
            triggers,
            { _, _ -> },
            { associatedAction = it }
        )
        associatedAction!!
        val newCameraMoveAction: GameAction = associatedAction

        val intListAddress = flatAddress
        val (newTriggerList, result) = traverseModify(triggers, intListAddress) { actions ->
            val newActions = actions.toMutableList()
            newActions.add(newCameraMoveAction)
            val index = newActions.lastIndex
            newActions to index
        }

        val newIntListAddress = intListAddress.toMutableList()
        newIntListAddress[newIntListAddress.lastIndex] = result
        val objectAddress = ObjectAddress(
            newIntListAddress[0],
            newIntListAddress[1],
            newIntListAddress.drop(2)
        )

        return newTriggerList to TriggerOrderReference(objectAddress)
    }

    fun duplicate(
        references: List<TriggerOrderReference>,
        scenario: GameScenario<*>
    ): Pair<Command<*>, Set<TriggerOrderReference>> {
        val references = references.toSet()
        val oldTriggers = scenario.triggers
        val newReferenceSet = mutableSetOf<TriggerOrderReference>()
        val newTriggers = references.fold(oldTriggers) { acc, reference ->

            val (updatedTriggers, newReference) = reference.duplicateItself(acc)
            newReferenceSet.add(newReference)
            updatedTriggers
        }

        return UpdateGameTriggerListCommand(
            oldTriggers = oldTriggers,
            newTriggers = newTriggers
        ) to newReferenceSet
    }

    fun delete(
        references: List<TriggerOrderReference>,
        scenario: GameScenario<*>
    ): Command<*> {
        val references = references.toSet()
        val oldTriggers = scenario.triggers
        val newTriggers = references.fold(oldTriggers) { acc, reference ->
            reference.removeItself(acc)
        }

        return UpdateGameTriggerListCommand(
            oldTriggers = oldTriggers,
            newTriggers = newTriggers
        )
    }

    fun update(
        references: List<TriggerOrderReference>,
        scenario: GameScenario<*>,
        updater: (GameAction.OrderUnit) -> GameAction.OrderUnit
    ): Command<*> {
        val references = references.toSet()
        val oldTriggers = scenario.triggers
        val newTriggers = references.fold(oldTriggers) { acc, reference ->
            val (updatedTriggers, _) = reference.traverseModifySelf(acc) { actions ->
                val newActions = actions.mapIndexed { objectId, actionObject ->
                    if (objectId != reference.objectId) return@mapIndexed actionObject
                    updater(actionObject as GameAction.OrderUnit)
                }
                newActions to Unit
            }
            updatedTriggers
        }

        return UpdateGameTriggerListCommand(
            oldTriggers = oldTriggers,
            newTriggers = newTriggers
        )
    }


}