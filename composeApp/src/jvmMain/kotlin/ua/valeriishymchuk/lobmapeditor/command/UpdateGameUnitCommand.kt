package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit

@Deprecated("Not sure whether should be used at all. UpdateGameUnitListCommand exists already.",level = DeprecationLevel.ERROR)
data class UpdateGameUnitCommand(
    val unitIndex: Int,
    val oldUnit: GameUnit,
    val newUnit: GameUnit
): Command.Preset {
    override fun execute(input: GameScenario.Preset): GameScenario.Preset {
        val list = input.units.toMutableList()
        list[unitIndex] = newUnit
        return input.copy(units = list)
    }

    override fun undo(input: GameScenario.Preset): GameScenario.Preset {
        val list = input.units.toMutableList()
        list[unitIndex] = oldUnit
        return input.copy(units = list)
    }
}