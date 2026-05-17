package ua.valeriishymchuk.lobmapeditor.domain.reference

import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameTriggerListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameAction
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger
import kotlin.reflect.KClass


abstract class TriggerScenarioReference<
        SELF : TriggerScenarioReference<SELF, ASSOCIATED_ACTION, ASSOCIATED_ACTION_OBJECT>,
        ASSOCIATED_ACTION : GameAction,
        ASSOCIATED_ACTION_OBJECT: DomainProperty<*>
        >(
    val objectAddress: ObjectAddress
) : ScenarioReference {

    val triggerId get() = objectAddress.triggerId
    val actionId get() = objectAddress.actionId
    val address get() = objectAddress.address

    init {
        if (address.isEmpty()) throw IllegalArgumentException("Address should have at least 1 element")
        if (address.size % 2 == 0) throw IllegalArgumentException("Address should always have an odd size of elements")
    }

    val objectId by lazy {
        address.last()
    }

    abstract fun withObjectAddress(objectAddress: ObjectAddress): SELF

    fun changeObjectId(newId: Int): SELF {
        val newAddress = address.toMutableList()
        newAddress.removeLast()
        newAddress.add(newId)
        return withObjectAddress(
            objectAddress.copy(
                address = newAddress
            )
        )
    }

    protected abstract fun tryCastAction(action: GameAction): ASSOCIATED_ACTION?

    private fun traverseAddress(
        triggers: List<GameTrigger>,
        actionHandler: (Int, GameAction.AddTrigger) -> Unit = { _, _ -> },
        associatedActionHandler: (ASSOCIATED_ACTION) -> Unit = {}
    ) {
        var action = triggers[triggerId].actions[actionId]
        val addressQueue = ArrayDeque(address)
        var addressId = -1
        while (addressQueue.isNotEmpty()) {
            val currentId = addressQueue.removeFirst() // unit id or trigger id within the action
            addressId++
            val associatedAction = tryCastAction(action)
            if (associatedAction != null) {
                if (addressQueue.isNotEmpty()) throw IllegalStateException("Invalid address, the unit id is not the last in the address")
                associatedActionHandler(associatedAction)
                return
            }
            if (action is GameAction.AddTrigger) {
                actionHandler(addressId, action)
                val nextId = addressQueue.removeFirstOrNull() // only action id within trigger list
                    ?: throw IllegalArgumentException(
                        "Invalid address, for AddTrigger action there should be 2 ids in the list per 1 action"
                    )
                addressId++
                action = action.triggers[currentId].actions[nextId]
            } else {
                throw IllegalArgumentException("Invalid address, ids within address can only point to a unit or AddTrigger action")
            }
        }

        throw IllegalStateException("Invalid address, addresses should contain last id that points to a unit within an AddUnit action")
    }

    protected abstract fun retrieveObjectsFromAction(action: ASSOCIATED_ACTION): List<ASSOCIATED_ACTION_OBJECT>

    protected abstract fun createNewActionFromObjects(objects: List<ASSOCIATED_ACTION_OBJECT>): ASSOCIATED_ACTION

    private fun <R> updateObjectList(
        triggers: List<GameTrigger>,
        updater: (List<ASSOCIATED_ACTION_OBJECT>) -> Pair<List<ASSOCIATED_ACTION_OBJECT>, R>
    ): Pair<List<GameTrigger>, R> {
        val actionList: MutableList<Pair<Int, GameAction.AddTrigger>> = mutableListOf()
        var associatedAction: ASSOCIATED_ACTION? = null
        traverseAddress(
            triggers,
            { id, action -> actionList.add(id to action) },
            { associatedAction = it }
        )
        associatedAction!!
        val oldAssociatedObjectList = retrieveObjectsFromAction(associatedAction)
        val (newAssociatedObjectList, value) = updater(oldAssociatedObjectList)
        val newAddUnitAction = createNewActionFromObjects(newAssociatedObjectList)
        val newAction = actionList.foldRight(newAddUnitAction as GameAction) { el, acc ->
            val triggerId = address[el.first]
            val actionId = address[el.first + 1]
            val action = el.second
            val oldList = action.triggers
            val newList: List<GameTrigger> = oldList.mapIndexed { id, trigger ->
                if (id != triggerId) return@mapIndexed trigger
                val oldActionList = trigger.actions
                val newActionList = oldActionList.mapIndexed { currentActionId, currentAction ->
                    if (currentActionId != actionId) return@mapIndexed currentAction
                    acc
                }
                trigger.copy(actions = newActionList)
            }
            action.copy(triggers = newList)
        }

        val newTriggerList = triggers.mapIndexed { id, trigger ->
            if (id != triggerId) return@mapIndexed trigger
            trigger.copy(actions = trigger.actions.mapIndexed { currentActionId, currentAction ->
                if (actionId != currentActionId) return@mapIndexed currentAction
                newAction
            })
        }
        return newTriggerList to value
    }

    data class ObjectAddress(
        val triggerId: Int, val actionId: Int, val address: List<Int>
    )

    override fun dereference(scenario: GameScenario<*>): DomainProperty<*> {
        var associatedActionObject: ASSOCIATED_ACTION_OBJECT? = null
        traverseAddress(scenario.triggers, associatedActionHandler = {
            associatedActionObject = retrieveObjectsFromAction(it).getOrNull(objectId) ?:
                    throw IllegalStateException(
                        "Invalid reference. It points to $address, Object id: $objectId while size of the list is ${retrieveObjectsFromAction(it).size}"
                    )
        })
        return associatedActionObject!!
    }

    override fun isValid(scenario: GameScenario<*>): Boolean {
        var associatedObject: ASSOCIATED_ACTION_OBJECT? = null
        traverseAddress(scenario.triggers, associatedActionHandler = {
            associatedObject = retrieveObjectsFromAction(it).getOrNull(objectId)
        })
        return associatedObject != null
    }

    protected abstract fun tryCastSelf(scenarioReference: ScenarioReference): SELF?

    override fun <T : DomainProperty<*>> duplicate0(
        clazz: KClass<T>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>
    ): Pair<Command<*>, Set<ScenarioReference>> {
        val references = references.mapNotNull { tryCastSelf(it) }.toSet()
        val oldTriggers = scenario.triggers
        val newReferenceSet: MutableSet<ScenarioReference> = mutableSetOf()
        val newTriggers = references.fold(oldTriggers) { acc, reference ->
            val (updatedTriggers, newReference) = reference.updateObjectList(acc) { objects ->
                val newObjectList = objects.toMutableList()
                newObjectList.add(objects[reference.objectId])
                newObjectList to reference.changeObjectId(newObjectList.lastIndex)
            }
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
        val references = references.mapNotNull { tryCastSelf(it) }.toSet()
        val oldTriggers = scenario.triggers
        val newTriggers = references.fold(oldTriggers) { acc, reference ->
            val (updatedTriggers, _) = reference.updateObjectList(acc) { objects ->
                val newObjectList = objects.toMutableList()
                newObjectList.removeAt(reference.objectId)
                newObjectList to Unit
            }
            updatedTriggers
        }

        return UpdateGameTriggerListCommand(
            oldTriggers = oldTriggers,
            newTriggers = newTriggers
        )
    }

    protected abstract fun castToAssociatedObject(obj: Any): ASSOCIATED_ACTION_OBJECT

    override fun <R : DomainProperty<*>> update0(
        clazz: KClass<R>,
        references: List<ScenarioReference>,
        scenario: GameScenario<*>,
        updater: (R) -> R
    ): Command<*> {
        val references = references.mapNotNull { tryCastSelf(it) }.toSet()
        val oldTriggers = scenario.triggers
        val newTriggers = references.fold(oldTriggers) { acc, reference ->
            val (updatedTriggers, _) = reference.updateObjectList(acc) { objects ->
                val newUnitList = objects.mapIndexed { objectId, actionObject ->
                    if (objectId != reference.objectId) return@mapIndexed actionObject
                    castToAssociatedObject(updater(actionObject as R))
                }
                newUnitList to Unit
            }
            updatedTriggers
        }

        return UpdateGameTriggerListCommand(
            oldTriggers = oldTriggers,
            newTriggers = newTriggers
        )
    }

}