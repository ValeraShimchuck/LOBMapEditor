package ua.valeriishymchuk.lobmapeditor.domain.reference

import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameTriggerListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.address.ObjectAddress
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameAction
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger
import kotlin.reflect.KClass

data class MoveCameraScenarioReference(
    val objectAddress: ObjectAddress
) : ScenarioReference {

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
        associatedActionHandler: (GameAction.MoveCamera) -> Unit = {},
        address: List<Int> = flatAddress,
        addressId: Int = 0
    ) {
        val triggerId = address[0]
        val actionId = address[1]
        val newAddress = address.drop(2)
        if (newAddress.isEmpty()) {
            associatedActionHandler(triggers[triggerId].actions[actionId] as GameAction.MoveCamera)
            return
        }
        val addTriggersAction = triggers[triggerId].actions[actionId] as GameAction.AddTrigger
        actionHandler(addressId, addTriggersAction)
        traverseAddress(addTriggersAction.triggers, actionHandler, associatedActionHandler, newAddress, addressId + 2)

    }
//        var action = triggers[triggerId].actions[actionId]
//        val addressQueue = ArrayDeque(address)
//        var addressId = -1
//        while (addressQueue.isNotEmpty()) {
//            val currentId = addressQueue.removeFirst() // unit id or trigger id within the action
//            addressId++
//            val associatedAction = action as? GameAction.MoveCamera
//            if (associatedAction != null) {
//                if (addressQueue.isNotEmpty()) throw IllegalStateException("Invalid address, the unit id is not the last in the address")
//                associatedActionHandler(associatedAction)
//                return
//            }
//            if (action is GameAction.AddTrigger) {
//                actionHandler(addressId, action)
//                val nextId = addressQueue.removeFirstOrNull() // only action id within trigger list
//                    ?: throw IllegalArgumentException(
//                        "Invalid address, for AddTrigger action there should be 2 ids in the list per 1 action"
//                    )
//                addressId++
//                action = action.triggers[currentId].actions[nextId]
//            } else {
//                throw IllegalArgumentException("Invalid address, ids within address can only point to a unit or AddTrigger action")
//            }
//        }
//
//        throw IllegalStateException("Invalid address, addresses should contain last id that points to a unit within an AddUnit action")
//    }

    override fun dereference(scenario: GameScenario<*>): DomainProperty<*> {
        var associatedActionObject: GameAction.MoveCamera? = null
        traverseAddress(scenario.triggers, associatedActionHandler = {
            associatedActionObject = it
        })
        return associatedActionObject!!
    }

    override fun isValid(scenario: GameScenario<*>): Boolean {
        var associatedActionObject: GameAction.MoveCamera? = null
        traverseAddress(scenario.triggers, associatedActionHandler = {
            associatedActionObject = it
        })
        return associatedActionObject != null
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

    private fun duplicateItself(triggers: List<GameTrigger>): Pair<List<GameTrigger>, MoveCameraScenarioReference> {
        var associatedAction: GameAction.MoveCamera? = null
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

        return newTriggerList to MoveCameraScenarioReference(objectAddress)
    }

    override fun <T : DomainProperty<*>> duplicate0(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ): Pair<Command<*>, Set<ScenarioReference>> {
        val references = references.mapNotNull { it as MoveCameraScenarioReference }.toSet()
        val oldTriggers = scenario.triggers
        val newReferenceSet = mutableSetOf<ScenarioReference>()
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

    override fun <T : DomainProperty<*>> delete0(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ): Command<*> {
        val references = references.mapNotNull { it as MoveCameraScenarioReference }.toSet()
        val oldTriggers = scenario.triggers
        val newTriggers = references.fold(oldTriggers) { acc, reference ->
            reference.removeItself(acc)
        }

        return UpdateGameTriggerListCommand(
            oldTriggers = oldTriggers,
            newTriggers = newTriggers
        )
    }

    override fun <T : DomainProperty<*>> update0(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>,
        updater: (T) -> T
    ): Command<*> {
        val references = references.mapNotNull { it as MoveCameraScenarioReference }.toSet()
        val oldTriggers = scenario.triggers
        val newTriggers = references.fold(oldTriggers) { acc, reference ->
            val (updatedTriggers, _) = reference.traverseModifySelf(acc) { actions ->
                val newActions = actions.mapIndexed { objectId, actionObject ->
                    if (objectId != reference.objectId) return@mapIndexed actionObject
                    updater(actionObject as T) as GameAction
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