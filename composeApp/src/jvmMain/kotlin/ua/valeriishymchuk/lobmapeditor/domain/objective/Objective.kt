package ua.valeriishymchuk.lobmapeditor.domain.objective

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.NameProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.ObjectiveProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit.ScenarioUnitReference
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import kotlin.reflect.KClass

data class Objective(
    // depends on scenario type can be either player or player team
    override val owner: Int?,
    override val name: String?,
    override val position: Position,
    override val type: ObjectiveType,
    override val victoryPoints: Int
): PositionProperty<Objective>, NameProperty<Objective>, ObjectiveProperty<Objective> {

    override val rotation: Float? = null
    override fun withPosition(pos: Position): Objective {
        return copy(position = pos)
    }

    override fun withOwner(owner: Int?): Objective {
        return copy(owner = owner)
    }

    override fun withType(type: ObjectiveType): Objective {
        return copy(type = type)
    }

    override fun withVictoryPoints(victoryPoints: Int): Objective {
        return copy(victoryPoints = victoryPoints)
    }

    override val identification: String = "${name ?: type.name}. Objective"


    override fun withRotation(rotation: Float): Objective {
        throw IllegalStateException("Rotation is not supported")
    }

    override fun withName(name: String?): Objective {
        return copy(name = name)
    }

    override val hitboxDimensions: Vector2f = Vector2f(
        26f
    )

    fun serialize(isPreset: Boolean): JsonObject {
        return JsonObject().apply {
            name?.let {
                add("name", JsonPrimitive(it))
            }
            if (owner != null) {
                if (isPreset) {
                    add("player", JsonPrimitive(owner + 1))
                } else {
                    add("team", JsonPrimitive(owner + 1))
                }
            }

            add("pos", position.serialize())
            add("type", JsonPrimitive(type.id))
            if (victoryPoints != type.defaultVictoryPoints) {
                add("vp", JsonPrimitive(victoryPoints))
            }
        }
    }

    data class ScenarioObjectiveReference(
        val listId: Int
    ) : ScenarioReference {
        override fun dereference(scenario: GameScenario<*>): DomainProperty<*> {
            return scenario.commonData.objectives[listId]
        }

        override fun <T : DomainProperty<*>> duplicate0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>
        ): Pair<Command<*>, Set<ScenarioReference>> {
            val old = scenario.commonData.objectives
            val new = scenario.commonData.objectives.toMutableList().apply {
                addAll(references.map { (it.dereference(scenario) as Objective).copy() })
            }

            val oldSize = old.size
            val newSize = new.size
            val references = (oldSize..<newSize).map {
                ScenarioObjectiveReference(it)
            }.toSet()
            return UpdateObjectiveListCommand(
                old,
                new,
            ) to references
        }

        override fun <T : DomainProperty<*>> delete0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>
        ): Command<*> {
            val ids = references.map { (it as ScenarioObjectiveReference).listId }.toSet()
            val old = scenario.objectives
            val new = scenario.objectives.filterIndexed { id, _ ->
                !ids.contains(id)
            }
            return UpdateObjectiveListCommand(
                old,
                new
            )
        }

        override fun <T : DomainProperty<*>> update0(
            clazz: KClass<T>,
            references: List<ScenarioReference>,
            scenario: GameScenario<*>,
            updater: (T) -> T
        ): Command<*> {
            val ids = references.map { (it as ScenarioObjectiveReference).listId }.toSet()
            val old = scenario.objectives
            val new = scenario.objectives.mapIndexed { id, unit ->
                if (!ids.contains(id)) return@mapIndexed unit
                updater(unit as T) as Objective
            }
            return UpdateObjectiveListCommand(
                old,
                new
            )
        }


    }

    companion object {
        const val MIN_VICTORY_POINTS = 1

        fun deserialize(json: JsonObject, isPreset: Boolean): Objective {
            val owner: Int? = if (isPreset) {
                if (json.has("player")) {
                    json.getAsJsonPrimitive("player").asInt - 1
                } else null
            } else {
                if (json.has("team")) {
                    json.getAsJsonPrimitive("team").asInt - 1
                } else null
            }
            val name = if (json.has("name"))
                json.getAsJsonPrimitive("name").asString
            else null
            val pos = Position.Companion.deserialize(json.getAsJsonObject("pos"))
            val type = if (json.has("type"))
                json.getAsJsonPrimitive("type").asInt
            else 1
            val objectiveType = ObjectiveType.getTypeById(type)

            val victoryPoints = if (json.has("vp")) {
                json.getAsJsonPrimitive("vp").asInt
            } else objectiveType
                .defaultVictoryPoints
            return Objective(
                owner,
                name,
                pos,
                objectiveType,
                victoryPoints
            )
        }
    }

}