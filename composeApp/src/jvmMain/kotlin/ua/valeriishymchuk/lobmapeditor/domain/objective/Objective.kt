package ua.valeriishymchuk.lobmapeditor.domain.objective

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference

data class Objective(
    // depends on scenario type can be either player or player team
    val owner: Int?,
    val name: String?,
    val position: Position,
    val type: ObjectiveType,
    val victoryPoints: Int
) {
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