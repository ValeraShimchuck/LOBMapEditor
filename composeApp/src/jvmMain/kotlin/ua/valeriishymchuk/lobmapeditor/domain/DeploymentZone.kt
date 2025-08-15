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
}
