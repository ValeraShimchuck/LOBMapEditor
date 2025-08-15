package ua.valeriishymchuk.lobmapeditor.domain.trigger

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam

sealed interface Condition {
    val key: String
    fun serializeValue(): JsonElement
    fun serialize(): JsonObject {
        return JsonObject().apply {
            add("type", JsonPrimitive(key))
            add("value", serializeValue())
        }
    }

    data class IsTurn(
        val turn: Int
    ): Condition {
        override val key: String = "isTurn"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(turn)
        }
    }

    data class ObjectiveBelongsTo(
        val objectiveName: String,
        val team: PlayerTeam
    ): Condition {
        override val key: String = "objectiveBelongsTo"

        override fun serializeValue(): JsonElement {
            return JsonObject().apply {
                add("name", JsonPrimitive(objectiveName))
                add("team", JsonPrimitive(team.id))
            }
        }
    }

    data class IsTurnMultipleOf(
        val multiple: Int,
        val offset: Int = 0
    ): Condition {
        override val key: String = "isTurnMultipleOf"

        override fun serializeValue(): JsonElement {
            return JsonObject().apply {
                add("multiple", JsonPrimitive(multiple))
                add("offset", JsonPrimitive(offset))
            }
        }

    }

    data class IsTurnGreaterThan(
        val value: Int
    ): Condition {
        override val key: String = "isTurnGreaterThan"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(value)
        }

    }

    data class IsTurnLessThan(
        val value: Int
    ): Condition {
        override val key: String = "isTurnLessThan"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(value)
        }

    }

    data class IsUnitNotAlive(
        val unitName: String
    ): Condition {
        override val key: String = "isUnitNotAlive"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(unitName)
        }

    }

    data class IsUnitRouting(
        val unitName: String
    ): Condition {
        override val key: String = "isUnitRouting"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(unitName)
        }

    }

    data class Chance(
        val chance: Float // from 0 to 100
    ): Condition {
        override val key: String = "chance"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(chance)
        }

    }

}