package ua.valeriishymchuk.lobmapeditor.domain.objective

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference

data class Objective(
    val owner: Reference<Int, Player>?,
    val name: String?,
    val position: Position,
    val type: ObjectiveType
) {
    fun serialize(): JsonObject {
        return JsonObject().apply {
            name?.let {
                add("name", JsonPrimitive(it))
            }
            owner?.key?.let {
                add("player", JsonPrimitive(it))
            }
            add("pos", position.serialize())
            add("type", JsonPrimitive(type.id))
        }
    }

    companion object {
        fun deserialize(json: JsonObject): Objective {
            val owner: Reference<Int, Player>? = if (json.has("player")) {
                Reference(json.getAsJsonPrimitive("player").asInt)
            } else null
            val name = if (json.has("name"))
                json.getAsJsonPrimitive("name").asString
            else null
            val pos = Position.Companion.deserialize(json.getAsJsonObject("pos"))
            val type = if (json.has("type"))
                json.getAsJsonPrimitive("type").asInt
            else 1
            val objectiveType = ObjectiveType.getTypeById(type)
            return Objective(
                owner,
                name,
                pos,
                objectiveType
            )
        }
    }

}