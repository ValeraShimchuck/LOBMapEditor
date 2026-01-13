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
    val type: GameUnitType,
    val status: UnitStatus,
    val formation: UnitFormation?,
    val health: Int,
    val organization: Int,
    val stamina: Int?

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
            formation?.let {
                if (it != UnitFormation.MASS) {
                    add("f", JsonPrimitive(it.name))
                }
            }

            if (health != type.defaultHealth) {
                add("hp", JsonPrimitive(health))
            }

            if (organization != type.defaultOrganization) {
                add("org", JsonPrimitive(organization))
            }

            if (stamina != type.defaultStamina) {
                add("st", JsonPrimitive(stamina))
            }

            if (status != UnitStatus.STANDING) {
                add("status", JsonPrimitive(status.id))
            }

        }
    }

    companion object {
        const val MIN_ORGANIZATION = 0
        const val MIN_HEALTH = 1
        const val MIN_STAMINA = 0
        val UNIT_DIMENSIONS = Vector2f(1f, 2f).mul(16f).mul(0.75f)

        fun deserialize(json: JsonObject): GameUnit {
            val name = json.getAsJsonPrimitive("name")?.asString
            val playerKey = json.getAsJsonPrimitive("player").asInt - 1
            val position = Position.deserialize(json.getAsJsonObject("pos"))
            val rotation = json.getAsJsonPrimitive("rotation").asFloat
            val typeId = json.getAsJsonPrimitive("type").asInt
            val unitType = GameUnitType.fromId(typeId)
            val status = json.getAsJsonPrimitive("status")?.asInt
                ?.let(UnitStatus.Companion::fromId) ?: UnitStatus.STANDING
            val formation = if (unitType.texture is UnitTypeTexture.Formation) {
                json.getAsJsonPrimitive("f")?.asString?.let { name ->
                    UnitFormation.valueOf(name.uppercase())
                } ?: UnitFormation.MASS
            } else null
            val health = json.getAsJsonPrimitive("hp")?.asInt ?: unitType.defaultHealth
            val organization = json.getAsJsonPrimitive("org")?.asInt ?: unitType.defaultOrganization
            val stamina = if (unitType.defaultStamina == null) {
                null
            } else json.getAsJsonPrimitive("st")?.asInt ?: unitType.defaultStamina

            return GameUnit(
                name = name,
                owner = Reference(playerKey),
                position = position,
                rotationRadians = rotation,
                type = unitType,
                status = status,
                formation = formation,
                health = health,
                organization = organization,
                stamina = stamina
            )
        }
    }
}