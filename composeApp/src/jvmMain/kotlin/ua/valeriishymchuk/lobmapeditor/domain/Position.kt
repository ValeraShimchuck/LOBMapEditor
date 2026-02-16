package ua.valeriishymchuk.lobmapeditor.domain

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.joml.Vector2f
import org.joml.Vector2i

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

    fun serializeAsArray(): JsonArray {
        return JsonArray().apply {
            add(x)
            add(y)
        }
    }

    companion object {
        fun deserialize(json: JsonObject): Position {
            return Position(
                json.getAsJsonPrimitive("x").asFloat,
                json.getAsJsonPrimitive("y").asFloat
            )
        }

        fun deserializeArray(json: JsonArray): Position {
            return Position(
                json.get(0).asFloat,
                json.get(1).asFloat
            )
        }

    }



}

fun Position.toVector2f() = Vector2f(x, y)
fun Position.toVector2i() = Vector2i(x.toInt(), y.toInt())

fun Vector2f.toPosition() = Position(x, y)
