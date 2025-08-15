package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit

data class UpdateGameUnitListCommand(
    val oldGameUnits: List<GameUnit>,
    val newGameUnits: List<GameUnit>,
): Command.Preset {
    override fun execute(input: GameScenario.Preset): GameScenario.Preset {
        return input.copy(units = newGameUnits)
    }

    override fun undo(input: GameScenario.Preset): GameScenario.Preset {
        return input.copy(units = oldGameUnits)
    }

}
