package ua.valeriishymchuk.lobmapeditor.domain.trigger

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference

sealed interface GameAction {

    val key: String
    fun serializeValue(): JsonElement
    fun serialize(): JsonObject {
        return JsonObject().apply {
            add("type", JsonPrimitive(key))
            add("value", serializeValue())
        }
    }

    data class AddUnit(
        val gameUnit: GameUnit
    ): GameAction {
        override val key: String = "addUnit"

        override fun serializeValue(): JsonElement {
            return gameUnit.serialize()
        }

    }

    data class ShowMessage(
        val title: String,
        val message: String
    ): GameAction {
        override val key: String = "showMessage"

        override fun serializeValue(): JsonElement {
            return JsonObject().apply {
                add("title", JsonPrimitive(title))
                add("message", JsonPrimitive(message))
            }
        }
    }

    data class DefeatPlayer(
        val player: Reference<Int, Player>
    ): GameAction {
        override val key: String = "defeatPlayer"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(player.key)
        }
    }

    data class MoveCamera(
        val position: Position,
        val zoom: Float,
        val duration: Float
    ): GameAction {
        override val key: String = "defeatPlayer"

        override fun serializeValue(): JsonElement {
            return position.serialize().apply {
                add("zoom", JsonPrimitive(zoom))
                add("duration", JsonPrimitive(duration))
            }
        }
    }

}