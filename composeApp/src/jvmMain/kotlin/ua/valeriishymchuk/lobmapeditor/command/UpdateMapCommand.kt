package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain

class UpdateMapCommand(
    val oldMap: Terrain,
    val newMap: Terrain
) : Command.CommonData {
    override fun execute(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(map = newMap)
    }

    override fun undo(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(map = oldMap)
    }
}