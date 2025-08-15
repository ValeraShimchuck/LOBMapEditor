package ua.valeriishymchuk.lobmapeditor.domain

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference

data class Objective(
    private val owner: Reference<Int, Player>?,
    private val name: String?,
    private val position: Position
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
            val pos = Position.deserialize(json.getAsJsonObject("pos"))
            return Objective(
                owner,
                name,
                pos
            )
        }
    }

}
