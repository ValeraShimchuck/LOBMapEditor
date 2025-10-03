package ua.valeriishymchuk.lobmapeditor.domain.unit

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference

data class GameUnit(
    val name: String?,
    val owner: Reference<Int, Player>, // AKA player
    val position: Position,
    val rotationRadians: Float,
    val type: GameUnitType
) {
    fun serialize(): JsonObject {
        return JsonObject().apply {
            name?.let {
                add("name", JsonPrimitive(it))
            }
            add("player", JsonPrimitive(owner.key + 1))
            add("pos", position.serialize())
            add("rotation", JsonPrimitive(rotationRadians))
            add("type", JsonPrimitive(type.id))
        }
    }

    companion object {

        val UNIT_DIMENSIONS = Vector2f(1f, 2f).mul(16f).mul(0.75f)

        fun deserialize(json: JsonObject): GameUnit {
            val name = json.getAsJsonPrimitive("name")?.asString
            val playerKey = json.getAsJsonPrimitive("player").asInt - 1
            val position = Position.deserialize(json.getAsJsonObject("pos"))
            val rotation = json.getAsJsonPrimitive("rotation").asFloat
            val typeId = json.getAsJsonPrimitive("type").asInt
            val unitType = GameUnitType.fromId(typeId)

            return GameUnit(
                name = name,
                owner = Reference(playerKey),
                position = position,
                rotationRadians = rotation,
                type = unitType
            )
        }
    }
}