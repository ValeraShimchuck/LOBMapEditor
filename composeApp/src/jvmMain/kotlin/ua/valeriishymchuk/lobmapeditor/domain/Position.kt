package ua.valeriishymchuk.lobmapeditor.domain

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

data class Position(
    val x: Float,
    val y: Float
) {
    fun serialize(): JsonObject {
        return JsonObject().apply {
            add("x", JsonPrimitive(x))
            add("y", JsonPrimitive(y))
        }
    }

    companion object {
        fun deserialize(json: JsonObject): Position {
            return Position(
                json.getAsJsonPrimitive("x").asFloat,
                json.getAsJsonPrimitive("y").asFloat
            )
        }
    }
}
