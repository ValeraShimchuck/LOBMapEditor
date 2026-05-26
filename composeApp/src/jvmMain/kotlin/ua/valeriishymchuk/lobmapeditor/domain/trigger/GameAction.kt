package ua.valeriishymchuk.lobmapeditor.domain.trigger

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.joml.Vector2f
import org.joml.Vector3f
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.property.CameraProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitStatus
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.shared.utils.getOrNull

sealed interface GameAction {

    val enumRepresentation: ActionEnum
    fun serializeValue(): JsonElement
    fun serialize(): JsonObject {
        return JsonObject().apply {
            add("type", JsonPrimitive(enumRepresentation.key))
            add("value", serializeValue())
        }
    }

    enum class ActionEnum(
        val key: String,
        defaultGetter: () -> GameAction
    ) {
        ADD_UNIT("addUnit", {
            AddUnit(
                listOf(
                    GameUnit.DEFAULT
                )
            )
        }),
        REMOVE_UNIT("removeUnit", {
            RemoveUnit(
                listOf("UNIT_NAME1", "UNIT_NAME2")
            )
        }),
        ADD_TRIGGER("addTrigger", { AddTrigger(emptyList()) }),
        SHOW_MESSAGE("showMessage", { ShowMessage("TITLE", "MESSAGE") }),
        DEFEAT_PLAYER("defeatPlayer", { DefeatPlayer(Reference(1)) }),
        MOVE_CAMERA("moveCamera", { MoveCamera(Position(0f, 0f), 1f, 2f) }),
        SPAWN_NEUTRAL_OBJECTIVES("spawnNeutralObjectives", {
            SpawnNeutralObjectives(
                0.25f,
                mapOf(
                    BattleType.CLASH to 1,
                    BattleType.COMBAT to 2,
                    BattleType.BATTLE to 3,
                    BattleType.GRAND_BATTLE to 3
                ),
                0.1f,
                0.9f,
                0.1f,
                0.9f,
                null
            )
        }),
        SET_VAR("setVar", { SetVar("VAR_NAME", 1f) }),
        END_GAME("endGame", { EndGame(GameEndReason.VICTORY) }),
        ORDER_UNIT("orderUnit", {
            OrderUnit(
                OrderType.WALK,
                "UNIT_NAME",
                "TARGET_UNIT_NAME",
                null,
                null,
                null
            )
        });

        val default by lazy(defaultGetter)

        companion object {
            fun fromKey(str: String): ActionEnum {
                return entries.firstOrNull { it.key == str } ?: throw IllegalArgumentException("Can't find $str")
            }
        }

    }


    data class AddUnit(
        val gameUnits: List<GameUnit>
    ): GameAction {
        override val enumRepresentation: ActionEnum = ActionEnum.ADD_UNIT

        override fun serializeValue(): JsonElement {
            val jsonArray = JsonArray()
            gameUnits.forEach {
                jsonArray.add(it.serialize())
            }
            return jsonArray
        }

    }

    data class ShowMessage(
        val title: String,
        val message: String
    ): GameAction {
        override val enumRepresentation: ActionEnum = ActionEnum.SHOW_MESSAGE

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
        override val enumRepresentation: ActionEnum = ActionEnum.DEFEAT_PLAYER

        override fun serializeValue(): JsonElement {
            return JsonPrimitive(player.key)
        }
    }

    data class MoveCamera(
        override val position: Position,
        override val zoom: Float?,
        override val duration: Float
    ): GameAction, PositionProperty<MoveCamera>, CameraProperty<MoveCamera> {
        override val enumRepresentation: ActionEnum = ActionEnum.MOVE_CAMERA

        override fun serializeValue(): JsonElement {
            return position.serialize().apply {
                zoom?.let { zoom ->
                    add("zoom", JsonPrimitive(zoom))
                }
                add("duration", JsonPrimitive(duration))
            }
        }

        override val hitboxDimensions: Vector2f = Vector2f(26f)
        override val rotation: Float? = null

        override fun withPosition(pos: Position): MoveCamera {
            return copy(position = pos)
        }

        override fun withRotation(rotation: Float): MoveCamera {
            throw IllegalStateException("Rotation is not supported")
        }

        override val identification: String = "$position. Camera Movement"

        override fun withZoom(zoom: Float?): MoveCamera {
            return copy(zoom = zoom)
        }

        override fun withDuration(duration: Float): MoveCamera {
            return copy(duration = duration)
        }
    }

    data class AddTrigger(
        val triggers: List<GameTrigger>
    ): GameAction {
        override val enumRepresentation: ActionEnum = ActionEnum.ADD_TRIGGER

        override fun serializeValue(): JsonElement {
            val array = JsonArray()

            triggers.forEach { trigger ->
                array.add(trigger.serialize())
            }
            return array
        }

    }

    enum class GameEndReason{
        VICTORY,
        MAX_TURN,
        CANCELLED,
        DRAW_BY_AGREEMENT;
        val key get() = name.lowercase()
    }

    data class SetVar(
        val name: String,
        val value: Float
    ): GameAction {

        override val enumRepresentation: ActionEnum = ActionEnum.SET_VAR

        override fun serializeValue(): JsonElement {
            val obj = JsonObject()
            obj.add("name", JsonPrimitive(name))
            obj.add("value", JsonPrimitive(value))
            return obj
        }
    }

    data class EndGame(
        val reason: GameEndReason
    ): GameAction {

        override val enumRepresentation: ActionEnum = ActionEnum.END_GAME

        override fun serializeValue(): JsonElement {
            val obj = JsonObject()
            obj.add("reason", JsonPrimitive(reason.key))
            return obj
        }

    }

    // reference https://github.com/sophie-games/lob-sdk/blob/main/src/game-data/eras/napoleonic/battle-types.json
    enum class BattleType(
        val displayName: String
    ) {
        MICRO("Micro"),
        CLASH("Clash"),
        COMBAT("Combat"),
        BATTLE("Battle"),
        GRAND_BATTLE("Grand Battle");
        val key: String get() = name.lowercase()
    }

    enum class ObjectiveSpawnOrientation {
        PERPENDICULAR,
        PARALLEL,
        CIRCLE;
        val key: String get() = name.lowercase()
    }

    data class SpawnNeutralObjectives(
        val spacing: Float?,
        val amount: Map<BattleType, Int>?,
        val minX: Float?, // 0-1
        val maxX: Float?, // 0-1
        val minY: Float?, // 0-1
        val maxY: Float?, // 0-1
        val orientation: ObjectiveSpawnOrientation?
    ): GameAction {

        override val enumRepresentation: ActionEnum = ActionEnum.SPAWN_NEUTRAL_OBJECTIVES

        override fun serializeValue(): JsonElement {
            val obj = JsonObject()
            spacing?.let { spacing ->
                obj.add("spacing", JsonPrimitive(spacing))
            }
            minX?.let { minX ->
                obj.add("minX", JsonPrimitive(minX))
            }
            maxX?.let { maxX ->
                obj.add("maxX", JsonPrimitive(maxX))
            }

            minY?.let { minY ->
                obj.add("minY", JsonPrimitive(minY))
            }
            maxY?.let { maxY ->
                obj.add("maxY", JsonPrimitive(maxY))
            }
            orientation?.let { orientation ->
                obj.add("orientation", JsonPrimitive(orientation.key))
            }

            amount?.let { amount ->
                val amountObj = JsonObject()
                amount.forEach { (size, amount) ->
                    amountObj.add(size.key, JsonPrimitive(amount))
                }
                obj.add("amount", amountObj)
            }

            return obj
        }

    }

    enum class OrderType(
        val color: Vector3f,
    ) {
        WALK(Vector3f(85f, 85f, 255f).div(255f)),
        RUN(Vector3f(255f, 85f, 85f).div(255f)),
        SHOOT(Vector3f(255f, 255f, 85f).div(255f)),
        FIRE_AND_ADVANCE(Vector3f(255f, 165f, 0f).div(255f)),
        PLACE_ENTITY(Vector3f(0f, 165f, 255f).div(255f)),
        FALLBACK(Vector3f(204f).div(255f)),
        ROTATE(Vector3f(0f, 255f, 255f).div(255f));
        val id: Int get() = ordinal + 1
    }

    data class RemoveUnit(
        val units: List<String>
    ): GameAction {

        override val enumRepresentation: ActionEnum = ActionEnum.REMOVE_UNIT

        override fun serializeValue(): JsonElement {
            return JsonArray().apply {
                units.forEach { unitName ->
                    add(unitName)
                }
            }
        }

    }

    data class OrderUnit(
        val type: OrderType?, // null will be serialized as -1
        val unitName: String,
        // either targetName, path or pos should be used(exclusively)
        val targetName: String?, // For orders that require a target (follow, shoot, etc.).
        val path: List<Position>?, // positions will be stored as array of 2 numbers. For orders with path (movement, etc.).
        val pos: Position?, // For orders with position (shoot at location, etc.).

        val rotation: Float? // in radians

    ): GameAction{

        override val enumRepresentation: ActionEnum = ActionEnum.ORDER_UNIT


        override fun serializeValue(): JsonElement {
            val obj = JsonObject()
            if (type == null) {
                obj.add("type", JsonPrimitive(-1))
            } else {
                obj.add("type", JsonPrimitive(type.id))
            }
            obj.add("unitName", JsonPrimitive(unitName))
            targetName?.let { targetName ->
                obj.add("targetName", JsonPrimitive(targetName))
            }

            path?.let { path ->
                val array = JsonArray()
                path.forEach { pos ->
                    array.add(pos.serializeAsArray())
                }
                obj.add("path", array)
            }

            pos?.let { pos ->
                obj.add("pos", pos.serializeAsArray())
            }

            rotation?.let { rotation ->
                obj.add("rotation", JsonPrimitive(rotation))
            }

            return obj
        }


    }

    companion object {
        fun deserialize(json: JsonObject): GameAction {
            val typeRaw = json.getAsJsonPrimitive("type").asString
            val type = ActionEnum.fromKey(typeRaw)
            val value = json.get("value")

            return when (type) {
                ActionEnum.ADD_UNIT -> {
                    val unitJson = value.asJsonArray
                    AddUnit(unitJson.map {
                        GameUnit.deserialize(it.asJsonObject)
                    })
                }
                ActionEnum.SHOW_MESSAGE -> {
                    val obj = value.asJsonObject
                    ShowMessage(
                        title = obj.getAsJsonPrimitive("title").asString,
                        message = obj.getAsJsonPrimitive("message").asString
                    )
                }
                ActionEnum.DEFEAT_PLAYER -> {
                    DefeatPlayer(Reference(value.asJsonPrimitive.asInt))
                }
                ActionEnum.ADD_TRIGGER -> {
                    val array = value.asJsonArray
                    AddTrigger(array.map { json ->
                        GameTrigger.deserialize(json.asJsonObject)
                    })
                }
                ActionEnum.MOVE_CAMERA -> {  // Fixed key from "defeatPlayer" to "moveCamera"
                    val obj = value.asJsonObject
                    val zoom = if (obj.has("zoom")) obj.getAsJsonPrimitive("zoom").asFloat
                    else null
                    MoveCamera(
                        position = Position.deserialize(obj),
                        zoom = zoom,
                        duration = obj.getAsJsonPrimitive("duration").asFloat
                    )
                }

                ActionEnum.REMOVE_UNIT -> {
                    RemoveUnit(
                        value.asJsonArray.map { it.asString }
                    )
                }
                ActionEnum.SPAWN_NEUTRAL_OBJECTIVES -> {
                    val obj = value.asJsonObject

                    SpawnNeutralObjectives(
                        obj.getOrNull("spacing")?.asFloat,
                        obj.getOrNull("amount")?.asJsonObject?.let { obj ->
                            obj.asMap()
                                .mapKeys { BattleType.valueOf(it.key.uppercase()) }
                                .mapValues { it.value.asInt }
                        },
                        obj.getOrNull("minX")?.asFloat,
                        obj.getOrNull("maxX")?.asFloat,
                        obj.getOrNull("minY")?.asFloat,
                        obj.getOrNull("maxY")?.asFloat,
                        obj.getOrNull("orientation")?.asString?.let { orientation ->
                            ObjectiveSpawnOrientation.valueOf(orientation.uppercase())
                        }
                    )
                }
                ActionEnum.SET_VAR -> {
                    val obj = value.asJsonObject
                    SetVar(
                        obj.get("name").asString,
                        obj.get("value").asFloat
                    )
                }
                ActionEnum.END_GAME -> {
                    val obj = value.asJsonObject
                    EndGame(
                        GameEndReason.valueOf(obj.get("reason").asString.uppercase())
                    )
                }
                ActionEnum.ORDER_UNIT -> {
                    val obj = value.asJsonObject
                    OrderUnit(
                        obj.get("type").asInt?.let { rawOrder ->
                            if (rawOrder <= 0) null
                            else OrderType.entries[rawOrder - 1]
                        },
                        obj.get("unitName").asString,
                        obj.getOrNull("targetName")?.asString,
                        obj.getOrNull("path")?.asJsonArray?.let { path ->
                            path.map { Position.deserializeArray(it.asJsonArray) }
                        },
                        obj.getOrNull("pos")?.asJsonArray?.let(Position::deserializeArray),
                        obj.getOrNull("rotation")?.asFloat
                    )
                }
            }
        }
    }
}