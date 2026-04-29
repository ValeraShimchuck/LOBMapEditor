package ua.valeriishymchuk.lobmapeditor.domain.unit

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameTriggerListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective.ScenarioObjectiveReference
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.NameProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.UnitProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameAction
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.reflect.KClass

data class GameUnit(
    override val name: String?,
    override val owner: Reference<Int, Player>, // AKA player
    override val position: Position,
    override val rotation: Float,
    override val type: GameUnitType,
    override val status: UnitStatus,
    override val formation: UnitFormation?,
    override val health: Int,
    override val organization: Int,
    override val stamina: Int?

): PositionProperty<GameUnit>, NameProperty<GameUnit>, UnitProperty<GameUnit> {

    override val hitboxDimensions: Vector2f get() {
        val formation = formation ?: return UNIT_DIMENSIONS
        return formation.dimensions
    }


    override val identification: String = "${name ?: type.name}. Unit"

    override fun withFormation(formation: UnitFormation?): GameUnit {
        return copy(formation = formation)
    }

    override fun withHealth(health: Int): GameUnit {
        return copy(health = health)
    }

    override fun withOrganization(organization: Int): GameUnit {
        return copy(organization = organization)
    }

    override fun withOwner(owner: Reference<Int, Player>): GameUnit {
        return copy(owner = owner)
    }

    override fun withStamina(stamina: Int?): GameUnit {
        return copy(stamina = stamina)
    }

    override fun withStatus(status: UnitStatus): GameUnit {
        return copy(status = status)
    }

    override fun withType(type: GameUnitType): GameUnit {
        return copy(type = type)
    }

    override fun withName(name: String?): GameUnit {
        return copy(name = name)
    }

    fun serialize(): JsonObject {
        return JsonObject().apply {
            name?.let {
                add("name", JsonPrimitive(it))
            }
            add("player", JsonPrimitive(owner.key + 1))
            add("pos", position.serialize())
            add("rotation", JsonPrimitive(rotation))
            add("type", JsonPrimitive(type.id))
            formation?.let {
                if (it != UnitFormation.MASS) {
                    add("f", JsonPrimitive(it.name.lowercase()))
                }
            }

            if (health != type.defaultHealth) {
                add("hp", JsonPrimitive(health))
            }

            if (organization != type.defaultOrganization) {
                add("org", JsonPrimitive(organization))
            }

            if (stamina != type.defaultStamina) {
                add("st", JsonPrimitive(stamina))
            }

            if (status != UnitStatus.STANDING) {
                add("status", JsonPrimitive(status.id))
            }

        }
    }

    override fun withPosition(pos: Position): GameUnit {
        return copy(position = pos)
    }

    override fun withRotation(rotation: Float): GameUnit {
        return copy(rotation = rotation)
    }

    data class TriggerUnitReference(
        val triggerId: Int,
        val actionId: Int,
        val address: List<Int>
    ): ScenarioReference {

        init {
            if (address.isEmpty()) throw IllegalArgumentException("Address should have at least 1 element")
            if (address.size % 2 == 0) throw IllegalArgumentException("Address should always have an odd size of elements")
        }

        val unitId by lazy {
            address.last()
        }

        fun changeUnitId(newId: Int): TriggerUnitReference {
            val newAddress = address.toMutableList()
            newAddress.removeLast()
            newAddress.add(newId)
            return copy(
                address = newAddress
            )
        }

        private fun traverseAddress(
            triggers: List<GameTrigger>,
            actionHandler: (Int, GameAction.AddTrigger) -> Unit = { _, _ -> },
            unitActionHandler: (GameAction.AddUnit) -> Unit = {}
        ) {
            var action = triggers[triggerId].actions[actionId]
            val addressQueue = ArrayDeque(address)
            var addressId = -1
            while (addressQueue.isNotEmpty()) {
                val currentId = addressQueue.removeFirst() // unit id or trigger id within the action
                addressId++
                if (action is GameAction.AddUnit) {
                    if (addressQueue.isNotEmpty()) throw IllegalStateException("Invalid address, the unit id is not the last in the address")
                    unitActionHandler(action)
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

        private fun <T> updateUnitList(triggers: List<GameTrigger>, updater: (List<GameUnit>) -> Pair<List<GameUnit>, T> ): Pair<List<GameTrigger>, T>  {
            val actionList: MutableList<Pair<Int, GameAction.AddTrigger>> = mutableListOf()
            var addUnitAction: GameAction.AddUnit? = null
            traverseAddress(
                triggers,
                { id, action -> actionList.add(id to action) },
                { addUnitAction = it }
            )
            addUnitAction!!
            val oldUnitList = addUnitAction.gameUnits
            val (newUnitList, value) = updater(oldUnitList)
            val newAddUnitAction = addUnitAction.copy(gameUnits = newUnitList)
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





        override fun dereference(scenario: GameScenario<*>): DomainProperty<*> {
            var unit: GameUnit? = null
            traverseAddress(scenario.triggers, unitActionHandler = {
                unit = it.gameUnits.getOrNull(unitId) ?:
                throw IllegalStateException(
                    "Invalid reference. It points to $address, unit id: $unitId while size of the list is ${it.gameUnits.size}"
                )
            })
            return unit!!
        }

        override fun isValid(scenario: GameScenario<*>): Boolean {
            var unit: GameUnit? = null
            traverseAddress(scenario.triggers, unitActionHandler = {
                unit = it.gameUnits.getOrNull(unitId)
            })
            return unit != null
        }

        override fun <T : DomainProperty<*>> duplicate0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>
        ): Pair<Command<*>, Set<ScenarioReference>> {
            val references = references.map { (it as TriggerUnitReference) }.toSet()
            val oldTriggers = scenario.triggers
            val newReferenceSet: MutableSet<ScenarioReference> = mutableSetOf()
            val newTriggers = references.fold(oldTriggers) { acc, reference ->
                val (updatedTriggers, newReference) = reference.updateUnitList(acc) { units ->
                    val newUnitList = units.toMutableList()
                    newUnitList.add(units[reference.unitId])
                    newUnitList to reference.changeUnitId(newUnitList.lastIndex)
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
            val references = references.map { (it as TriggerUnitReference) }.toSet()
            val oldTriggers = scenario.triggers
            val newTriggers = references.fold(oldTriggers) { acc, reference ->
                val (updatedTriggers, _) = reference.updateUnitList(acc) { units ->
                    val newUnitList = units.toMutableList()
                    newUnitList.removeAt(reference.unitId)
                    newUnitList to Unit
                }
                updatedTriggers
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
            val references = references.map { (it as TriggerUnitReference) }.toSet()
            val oldTriggers = scenario.triggers
            val newTriggers = references.fold(oldTriggers) { acc, reference ->
                val (updatedTriggers, _) = reference.updateUnitList(acc) { units ->
                    val newUnitList = units.mapIndexed { unitId, unit ->
                        if (unitId != reference.unitId) return@mapIndexed unit
                        updater(unit as T) as GameUnit
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

    data class ScenarioUnitReference(
        val listId: Int
    ) : ScenarioReference.Preset {

        override fun isValid(scenario: GameScenario<*>): Boolean {
            return (scenario as? GameScenario.Preset)?.units?.indices?.contains(listId) ?: false
        }


        override fun dereferencePreset(scenario: GameScenario.Preset): DomainProperty<*> {
            return scenario.units[listId]
        }

        override fun <T : DomainProperty<*>> updatePreset(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Preset,
            updater: (T) -> T
        ): Command<*> {
            val ids = references.map { (it as ScenarioUnitReference).listId }.toSet()
            val old = scenario.units
            val new = scenario.units.mapIndexed { id, unit ->
                if (!ids.contains(id)) return@mapIndexed unit
                updater(unit as T) as GameUnit
            }
            return UpdateGameUnitListCommand(
                old,
                new
            )
        }

        override fun <T : DomainProperty<*>> duplicatePreset(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Preset
        ): Pair<Command<*>, Set<ScenarioReference>> {
            val old = scenario.units
            val new = scenario.units.toMutableList().apply {
                addAll(references.map { (it.dereference(scenario) as GameUnit).copy() })
            }

            val oldSize = old.size
            val newSize = new.size
            val references = (oldSize..<newSize).map {
                ScenarioUnitReference(it)
            }.toSet()
            return UpdateGameUnitListCommand(
                old,
                new,
            ) to references
        }

        override fun <T : DomainProperty<*>> deletePreset(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario.Preset
        ): Command<*> {
            val ids = references.map { (it as ScenarioUnitReference).listId }.toSet()
            val old = scenario.units
            val new = scenario.units.filterIndexed { id, _ ->
                !ids.contains(id)
            }
            return UpdateGameUnitListCommand(
                old,
                new
            )
        }


    }

    companion object {
        const val MIN_ORGANIZATION = 0
        const val MIN_HEALTH = 1
        const val MIN_STAMINA = 0
        val UNIT_DIMENSIONS = Vector2f(1f, 2f).mul(16f).mul(0.75f)
        val DEFAULT = GameUnit(
            null,
            Reference(0),
            Position(0f, 0f),
            0f,
            GameUnitType.LINE_INFANTRY,
            UnitStatus.STANDING,
            UnitFormation.LINE,
            GameUnitType.LINE_INFANTRY.defaultHealth,
            GameUnitType.LINE_INFANTRY.defaultOrganization,
            GameUnitType.LINE_INFANTRY.defaultStamina
        )

        fun deserialize(json: JsonObject): GameUnit {
            val name = json.getAsJsonPrimitive("name")?.asString
            val playerKey = json.getAsJsonPrimitive("player").asInt - 1
            val position = Position.deserialize(json.getAsJsonObject("pos"))
            val rotation = json.getAsJsonPrimitive("rotation").asFloat
            val typeId = json.getAsJsonPrimitive("type").asInt
            val unitType = GameUnitType.fromId(typeId)
            val status = json.getAsJsonPrimitive("status")?.asInt
                ?.let(UnitStatus.Companion::fromId) ?: UnitStatus.STANDING
            val formation = if (unitType.texture is UnitTypeTexture.Formation) {
                json.getAsJsonPrimitive("f")?.asString?.let { name ->
                    UnitFormation.valueOf(name.uppercase())
                } ?: UnitFormation.MASS
            } else null
            val health = json.getAsJsonPrimitive("hp")?.asInt ?: unitType.defaultHealth
            val organization = json.getAsJsonPrimitive("org")?.asInt ?: unitType.defaultOrganization
            val stamina = if (unitType.defaultStamina == null) {
                null
            } else json.getAsJsonPrimitive("st")?.asInt ?: unitType.defaultStamina

            return GameUnit(
                name = name,
                owner = Reference(playerKey),
                position = position,
                rotation = rotation,
                type = unitType,
                status = status,
                formation = formation,
                health = health,
                organization = organization,
                stamina = stamina
            )
        }
    }
}