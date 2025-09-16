package ua.valeriishymchuk.lobmapeditor.domain

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

    companion object {
        fun deserialize(json: JsonObject): Position {
            return Position(
                json.getAsJsonPrimitive("x").asFloat,
                json.getAsJsonPrimitive("y").asFloat
            )
        }
    }



}

fun Position.toVector2f() = Vector2f(x, y)
fun Position.toVector2i() = Vector2i(x.toInt(), y.toInt())

fun Vector2f.toPosition() = Position(x, y)
