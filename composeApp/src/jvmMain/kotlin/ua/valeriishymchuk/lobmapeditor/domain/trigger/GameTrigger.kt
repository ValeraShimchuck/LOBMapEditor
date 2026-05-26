package ua.valeriishymchuk.lobmapeditor.domain.trigger

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import kotlin.reflect.KClass

data class GameTrigger(
    val actions: List<GameAction>,
    val conditions: List<Condition>,
    val eventType: EventTriggerType,
    val conditionLogicType: ConditionLogicType,
    val once: Boolean
) {

    fun displayText(prepend: String): String {
        val onceText = if (once) "Once" else "Not-Once"
        val eventType = when (eventType) {
            EventTriggerType.ON_TURN_START -> "Turn-Start"
            EventTriggerType.ON_TURN_END -> "Turn-End"
        }
        val conditionLogicType = when (conditionLogicType) {
            ConditionLogicType.OR -> "Logic-Or"
            ConditionLogicType.AND -> "Logic-And"
        }

        val conditions = conditions.size.toString() + "-Cond."
        val actions = actions.size.toString() + "-Act."
        return listOf(
            prepend,
            eventType,
            conditionLogicType,
            onceText,
            conditions,
            actions
        ).joinToString(separator = " ")
    }

    fun serialize(): JsonObject {
        return JsonObject().apply {
            add("actions", JsonArray().apply {
                actions.forEach {
                    add(it.serialize())
                }
            })

            add("conditions", JsonArray().apply {
                conditions.forEach {
                    add(it.serialize())
                }
            })

            add("event", JsonPrimitive(eventType.key))
            add("conditionLogic", JsonPrimitive(conditionLogicType.key))
            add("once", JsonPrimitive(once))
        }
    }

    companion object {

        fun <T: GameAction> List<GameTrigger>.findAllActions(kClass: Class<T>): List<T> {
            return this.flatMap { trigger ->
                val list: MutableList<T> = trigger.actions.filterIsInstance(kClass)
                    .toMutableList()
                val nestedTriggers = trigger.actions.filterIsInstance<GameAction.AddTrigger>().flatMap { it.triggers }
                list.addAll(nestedTriggers.findAllActions(kClass))
                list
            }
        }

        fun List<GameTrigger>.findAllOrders(): List<GameAction.OrderUnit> {
            return findAllActions(GameAction.OrderUnit::class.java)
        }

        fun List<GameTrigger>.findAllUnits(): List<GameUnit> {
            return findAllActions(GameAction.AddUnit::class.java).flatMap { it.gameUnits }
        }

        fun List<GameTrigger>.findAllRemovableUnitsNames(): Set<String> {
            return findAllActions(GameAction.RemoveUnit::class.java)
                .flatMap { it.units}
                .toSet()
        }

        fun List<GameTrigger>.findAllCameraMovements(): List<Position> {
            return findAllActions(GameAction.MoveCamera::class.java).map { it.position }
        }

        val DEFAULT = GameTrigger(
            emptyList(),
            emptyList(),
            EventTriggerType.ON_TURN_START,
            ConditionLogicType.AND,
            true
        )

        fun deserialize(json: JsonObject): GameTrigger {
            val actionsArray = json.getAsJsonArray("actions")
            val actions = actionsArray.map {
                GameAction.deserialize(it.asJsonObject)
            }.toList()

            val conditionsArray = json.getAsJsonArray("conditions")
            val conditions = conditionsArray.map {
                Condition.deserialize(it.asJsonObject)
            }.toList()

            val eventType = EventTriggerType.findByKey(json.getAsJsonPrimitive("event").asString)
            val conditionLogicType = ConditionLogicType.findByKey(json.getAsJsonPrimitive("conditionLogic").asString)
            val once = json.getAsJsonPrimitive("once").asBoolean

            return GameTrigger(
                actions = actions,
                conditions = conditions,
                eventType = eventType,
                conditionLogicType = conditionLogicType,
                once = once
            )
        }
    }

}