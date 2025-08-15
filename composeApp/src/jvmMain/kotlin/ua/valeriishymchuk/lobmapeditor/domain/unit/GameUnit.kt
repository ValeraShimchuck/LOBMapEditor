package ua.valeriishymchuk.lobmapeditor.domain.unit

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
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
            add("player", JsonPrimitive(owner.key))
            add("pos", position.serialize())
            add("rotation", JsonPrimitive(rotationRadians))
            add("type", JsonPrimitive(type.id))
        }
    }
}