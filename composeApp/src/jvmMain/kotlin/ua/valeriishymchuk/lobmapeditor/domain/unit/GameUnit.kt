package ua.valeriishymchuk.lobmapeditor.domain.unit

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.NameProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.UnitProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.reference.TriggerScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameAction
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

) : PositionProperty<GameUnit>, NameProperty<GameUnit>, UnitProperty<GameUnit> {

    override val hitboxDimensions: Vector2f
        get() {
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

    class TriggerUnitReference(
        objectAddress: ObjectAddress
    ) : TriggerScenarioReference<TriggerUnitReference, GameAction.AddUnit, GameUnit>(objectAddress) {

        override fun withObjectAddress(objectAddress: ObjectAddress): TriggerUnitReference {
            return TriggerUnitReference(objectAddress)
        }

        override fun tryCastAction(action: GameAction): GameAction.AddUnit? {
            return action as? GameAction.AddUnit
        }

        override fun retrieveObjectsFromAction(action: GameAction.AddUnit): List<GameUnit> {
            return action.gameUnits
        }

        override fun createNewActionFromObjects(objects: List<GameUnit>): GameAction.AddUnit {
            return GameAction.AddUnit(objects)
        }

        override fun tryCastSelf(scenarioReference: ScenarioReference): TriggerUnitReference? {
            return scenarioReference as? TriggerUnitReference
        }

        override fun castToAssociatedObject(obj: Any): GameUnit {
            return obj as GameUnit
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
            clazz: KClass<T>, references: List<ScenarioReference>, scenario: GameScenario.Preset, updater: (T) -> T
        ): Command<*> {
            val ids = references.map { (it as ScenarioUnitReference).listId }.toSet()
            val old = scenario.units
            val new = scenario.units.mapIndexed { id, unit ->
                if (!ids.contains(id)) return@mapIndexed unit
                updater(unit as T) as GameUnit
            }
            return UpdateGameUnitListCommand(
                old, new
            )
        }

        override fun <T : DomainProperty<*>> duplicatePreset(
            clazz: KClass<T>, references: List<ScenarioReference>, scenario: GameScenario.Preset
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
            clazz: KClass<T>, references: List<ScenarioReference>, scenario: GameScenario.Preset
        ): Command<*> {
            val ids = references.map { (it as ScenarioUnitReference).listId }.toSet()
            val old = scenario.units
            val new = scenario.units.filterIndexed { id, _ ->
                !ids.contains(id)
            }
            return UpdateGameUnitListCommand(
                old, new
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
            val status =
                json.getAsJsonPrimitive("status")?.asInt?.let(UnitStatus.Companion::fromId) ?: UnitStatus.STANDING
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