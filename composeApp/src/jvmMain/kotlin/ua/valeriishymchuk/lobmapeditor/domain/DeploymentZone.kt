package ua.valeriishymchuk.lobmapeditor.domain

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam

data class DeploymentZone(
    val team: PlayerTeam,
    val position: Position,
    val width: Float,
    val height: Float
) {
    fun serialize(): JsonObject {
        return position.serialize().apply {
            add("team", JsonPrimitive(team.id))
            add("width", JsonPrimitive(width))
            add("height", JsonPrimitive(height))
        }
    }

    companion object {
        fun deserialize(json: JsonObject): DeploymentZone {
            // Deserialize position first
            val position = Position.deserialize(json)

            // Extract team, width, and height
            val teamId = json.getAsJsonPrimitive("team").asInt
            val team = PlayerTeam.fromId(teamId)
            val width = json.getAsJsonPrimitive("width").asFloat
            val height = json.getAsJsonPrimitive("height").asFloat

            return DeploymentZone(
                team = team,
                position = position,
                width = width,
                height = height
            )
        }
    }
}
