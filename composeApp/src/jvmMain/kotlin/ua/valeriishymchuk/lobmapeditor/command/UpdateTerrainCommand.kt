package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType

data class UpdateTerrainCommand(
    val x: Int,
    val y: Int,
    val oldHeight: Int,
    val oldTerrain: TerrainType,
    val newTerrain: TerrainType,
    val newHeight: Int

): Command.CommonData {
    override fun execute(input: GameScenario.CommonData): GameScenario.CommonData {
        val terrain = input.map.terrainMap.clone()
        val heightMap = input.map.terrainHeight.clone()
        terrain.set(x, y, newTerrain)
        heightMap.set(x, y, newHeight)
        return input.copy(map = input.map.copy(terrainMap = terrain, heightMap))
    }

    override fun undo(input: GameScenario.CommonData): GameScenario.CommonData {
        val terrain = input.map.terrainMap.clone()
        val heightMap = input.map.terrainHeight.clone()
        terrain.set(x, y, oldTerrain)
        heightMap.set(x, y, oldHeight)
        return input.copy(map = input.map.copy(terrainMap = terrain, heightMap))
    }
}
