package ua.valeriishymchuk.lobmapeditor.domain.trigger

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

data class GameTrigger(
    val actions: List<GameAction>,
    val conditions: List<Condition>,
    val eventType: EventTriggerType,
    val conditionLogicType: ConditionLogicType,
    val once: Boolean
) {

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