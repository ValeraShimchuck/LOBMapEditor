package ua.valeriishymchuk.lobmapeditor.domain.trigger

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import javax.management.monitor.StringMonitor

sealed interface Condition {
    val key: String
    val enumRepresentation: ConditionEnum

    fun serializeValue(): JsonElement
    fun serialize(): JsonObject {
        return JsonObject().apply {
            add("type", JsonPrimitive(key))
            add("value", serializeValue())
        }
    }

    enum class ConditionEnum(
        private val defaultLazy: () -> Condition,
        val displayName: String
    ) {

        IS_TURN({ IsTurn(2) }, "Is Turn"),
        IS_TURN_MULTIPLE_OF({ IsTurnMultipleOf(2, 0) }, "Is Turn Multiple Of"),
        IS_TURN_GREATER_THAN({ IsTurnGreaterThan(2) }, "Is Turn Greater Than"),
        IS_TURN_LESS_THAN({ IsTurnLessThan(2) }, "Is Turn Less Than"),
        OBJECTIVE_BELONGS_TO({ ObjectiveBelongsTo("OBJECTIVE_NAME", PlayerTeam.BLUE, null) }, "Objective Belongs To"),
        IS_UNIT_NOT_ALIVE({ IsUnitNotAlive("UNIT_NAME") }, "Is Unit Not Alive"),
        IS_UNIT_ROUTING({ IsUnitRouting("UNIT_NAME") }, "Is Unit Routing"),
        UNIT_MOVED_THIS_TURN({ UnitMovedThisTurn("UNIT_NAME") }, "Unit Moved This Turn"),
        CHANCE({ Chance(50f) }, "Chance"),
        IS_VAR({ IsVar("VAR_NAME", 1f, null) }, "Is Var");

        val default by lazy { defaultLazy() }


    }

    data class IsVar(
        val name: String,
        val value: Float,
        val not: Boolean?
    ): Condition {
        override val key: String = "isVar"

        override val enumRepresentation: ConditionEnum = ConditionEnum.IS_VAR

        override fun serializeValue(): JsonElement {
            val obj = JsonObject()
            obj.add("name", JsonPrimitive(name))
            obj.add("value", JsonPrimitive(value))
            not?.let { not ->
                obj.add("not", JsonPrimitive(not))
            }
            return obj
        }

    }

    data class IsTurn(
        val turn: Int
    ): Condition {
        override val key: String = "isTurn"

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(turn)
        }

        override val enumRepresentation: ConditionEnum = ConditionEnum.IS_TURN
    }

    data class UnitMovedThisTurn(
        val name: String,
    ): Condition {

        override val key: String = "unitMovedThisTurn"
        override val enumRepresentation: ConditionEnum = ConditionEnum.UNIT_MOVED_THIS_TURN

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(name)
        }

    }

    data class ObjectiveBelongsTo(
        val objectiveName: String,
        val team: PlayerTeam?,
        val player: Int?
    ): Condition {
        override val key: String = "objectiveBelongsTo"

        override val enumRepresentation: ConditionEnum = ConditionEnum.OBJECTIVE_BELONGS_TO

        override fun serializeValue(): JsonElement {
            return JsonObject().apply {
                add("name", JsonPrimitive(objectiveName))
                team?.let {  team ->
                    add("team", JsonPrimitive(team.id))
                }
                player?.let { player ->
                    add("player", JsonPrimitive(player))
                }
            }
        }
    }

    data class IsTurnMultipleOf(
        val multiple: Int,
        val offset: Int = 0
    ): Condition {
        override val key: String = "isTurnMultipleOf"

        override val enumRepresentation: ConditionEnum = ConditionEnum.IS_TURN_MULTIPLE_OF

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

        override val enumRepresentation: ConditionEnum = ConditionEnum.IS_TURN_GREATER_THAN

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(value)
        }

    }

    data class IsTurnLessThan(
        val value: Int
    ): Condition {
        override val key: String = "isTurnLessThan"

        override val enumRepresentation: ConditionEnum = ConditionEnum.IS_TURN_LESS_THAN

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(value)
        }

    }

    data class IsUnitNotAlive(
        val unitName: String
    ): Condition {
        override val key: String = "isUnitNotAlive"

        override val enumRepresentation: ConditionEnum = ConditionEnum.IS_UNIT_NOT_ALIVE

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(unitName)
        }

    }

    data class IsUnitRouting(
        val unitName: String
    ): Condition {
        override val key: String = "isUnitRouting"

        override val enumRepresentation: ConditionEnum = ConditionEnum.IS_UNIT_ROUTING

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(unitName)
        }

    }

    data class Chance(
        val chance: Float // from 0 to 100
    ): Condition {
        override val key: String = "chance"

        override val enumRepresentation: ConditionEnum = ConditionEnum.CHANCE

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(chance)
        }

    }

    companion object {


        fun deserialize(json: JsonObject): Condition {
            val type = json.getAsJsonPrimitive("type").asString
            val value = json.get("value")

            return when (type) {
                "isTurn" -> IsTurn(value.asJsonPrimitive.asInt)
                "objectiveBelongsTo" -> {
                    val obj = value.asJsonObject
                    val team = if (!obj.has("team")) null
                    else PlayerTeam.fromId(obj.getAsJsonPrimitive("team").asInt)
                    val player = if (!obj.has("player")) null
                    else obj.getAsJsonPrimitive("player").asInt
                    ObjectiveBelongsTo(
                        objectiveName = obj.getAsJsonPrimitive("name").asString,
                        team,
                        player
                    )
                }
                "unitMovedThisTurn" -> {
                    UnitMovedThisTurn(value.asJsonPrimitive.asString)
                }
                "isVar" -> {
                    val obj = value.asJsonObject
                    val isNot = if (obj.has("not")) obj.getAsJsonPrimitive("not").asBoolean
                    else null
                    IsVar(
                        obj.getAsJsonPrimitive("name").asString,
                        obj.getAsJsonPrimitive("value").asFloat,
                        isNot
                    )
                }
                "isTurnMultipleOf" -> {
                    val obj = value.asJsonObject
                    IsTurnMultipleOf(
                        multiple = obj.getAsJsonPrimitive("multiple").asInt,
                        offset = obj.getAsJsonPrimitive("offset").asInt
                    )
                }
                "isTurnGreaterThan" -> IsTurnGreaterThan(value.asJsonPrimitive.asInt)
                "isTurnLessThan" -> IsTurnLessThan(value.asJsonPrimitive.asInt)
                "isUnitNotAlive" -> IsUnitNotAlive(value.asJsonPrimitive.asString)
                "isUnitRouting" -> IsUnitRouting(value.asJsonPrimitive.asString)
                "chance" -> Chance(value.asJsonPrimitive.asFloat)
                else -> throw IllegalArgumentException("Unknown Condition type: $type")
            }
        }
    }

}