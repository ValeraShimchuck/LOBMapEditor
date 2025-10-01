package ua.valeriishymchuk.lobmapeditor.commands

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

class WrapCommonToPresetCommand(
    val commonCommand: Command.CommonData
) : Command.Preset {
    override fun execute(input: GameScenario.Preset): GameScenario.Preset {
        return input.copy(commonData = commonCommand.execute(input.commonData))
    }

    override fun undo(input: GameScenario.Preset): GameScenario.Preset {
        return input.copy(commonData = commonCommand.undo(input.commonData))
    }
}