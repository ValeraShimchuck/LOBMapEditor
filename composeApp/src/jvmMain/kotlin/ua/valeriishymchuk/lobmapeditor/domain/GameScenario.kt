package ua.valeriishymchuk.lobmapeditor.domain

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit

sealed interface GameScenario<T : GameScenario<T>> {

    val commonData: CommonData

    val name: String get() = commonData.name
    val description: String get() = commonData.description
    val scenarioTypeKey: String
    val map: Terrain get() = commonData.map
    val objectives: List<Objective> get() = commonData.objectives
    val triggers: List<GameTrigger> get() = commonData.triggers

    fun serialize(): JsonObject

    fun withCommonData(newCommonData: CommonData): T

    data class Preset(
        override val commonData: CommonData,
        val units: List<GameUnit>,
        val players: List<Player>,
    ): GameScenario<Preset> {



        override val scenarioTypeKey: String = "preset"

        override fun serialize(): JsonObject {
            return JsonObject().apply {
                add("name", JsonPrimitive(name))
                add("description", JsonPrimitive(description))
                add("type", JsonPrimitive(scenarioTypeKey))
                add("players", JsonArray().apply {
                    players.forEachIndexed { index, player ->
                        add(JsonObject().apply {
                            add("player", JsonPrimitive(index + 1))
                            add("team", JsonPrimitive(player.team.id))
                        })
                    }
                })
                add("map", map.serialize())
                add("units", JsonArray().apply {
                    units.forEach { unit ->
                        add(unit.serialize())
                    }
                })
                add("objectives", JsonArray().apply {
                    objectives.forEach {
                        add(it.serialize())
                    }
                })

                add("triggers", JsonArray().apply {
                    triggers.forEach {
                        add(it.serialize())
                    }
                })
            }
        }

        override fun withCommonData(newCommonData: CommonData): Preset {
            return copy(commonData = newCommonData)
        }
    }

    data class Hybrid(
        override val commonData: CommonData,
        val deploymentZones: List<DeploymentZone>
    ): GameScenario<Hybrid> {
        override val scenarioTypeKey: String = "hybrid"

        override fun withCommonData(newCommonData: CommonData): Hybrid {
            return copy(commonData = newCommonData)
        }

        override fun serialize(): JsonObject {
            return JsonObject().apply {
                add("name", JsonPrimitive(name))
                add("description", JsonPrimitive(description))
                add("type", JsonPrimitive(description))
                add("map", map.serialize().apply {
                    add("deploymentZones", JsonArray().apply {
                        deploymentZones.forEach {
                            add(it.serialize())
                        }
                    })
                })
                add("objectives", JsonArray().apply {
                    objectives.forEach {
                        add(it.serialize())
                    }
                })

                add("triggers", JsonArray().apply {
                    triggers.forEach {
                        add(it.serialize())
                    }
                })
            }
        }

    }

    data class CommonData(
        val name: String,
        val description: String,
        val map: Terrain,
        val objectives: List<Objective>,
        val triggers: List<GameTrigger>
    )

    companion object {
        fun deserialize(json: JsonObject): GameScenario<*> {
            val name = json.getAsJsonPrimitive("name").asString
            val description = json.getAsJsonPrimitive("description").asString
            val type = json.getAsJsonPrimitive("type").asString

            // Deserialize objectives
            val objectivesArray = json.getAsJsonArray("objectives") ?: JsonArray()
            val objectives = objectivesArray.map { element ->
                Objective.deserialize(element.asJsonObject)
            }.toList()

            // Deserialize triggers
            val triggersArray = json.getAsJsonArray("triggers") ?: JsonArray()
            val triggers = triggersArray.map { element ->
                GameTrigger.deserialize(element.asJsonObject)
            }.toList()

            // Deserialize map
            val mapJson = json.getAsJsonObject("map")
            val map = Terrain.deserialize(mapJson)

            val commonData = CommonData(name, description, map, objectives, triggers)

            return when (type) {
                "preset" -> {
                    // Deserialize players
                    val playersArray = json.getAsJsonArray("players") ?: JsonArray()
                    val players = playersArray.sortedBy { element ->
                        val playerId = element.asJsonObject.getAsJsonPrimitive("player").asInt
                        playerId
                    }.map { element ->
                        val playerObj = element.asJsonObject
                        val teamId = playerObj.getAsJsonPrimitive("team").asInt
                        Player(
                            team = PlayerTeam.fromId(teamId)
                        )
                    }.toList()

                    // Deserialize units
                    val unitsArray = json.getAsJsonArray("units") ?: JsonArray()
                    val units = unitsArray.map { element ->
                        GameUnit.deserialize(element.asJsonObject)
                    }.toList()

                    Preset(commonData, units, players)
                }
                "hybrid" -> {
                    // Deserialize deployment zones
                    val deploymentZonesArray = mapJson.getAsJsonArray("deploymentZones") ?: JsonArray()
                    val deploymentZones = deploymentZonesArray.map { element ->
                        DeploymentZone.deserialize(element.asJsonObject)
                    }.toList()

                    Hybrid(commonData, deploymentZones)
                }
                else -> throw IllegalArgumentException("Unknown scenario type: $type")
            }
        }
    }

}