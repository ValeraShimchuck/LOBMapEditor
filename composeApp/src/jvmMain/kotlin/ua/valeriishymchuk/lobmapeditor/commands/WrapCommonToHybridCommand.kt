package ua.valeriishymchuk.lobmapeditor.commands

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

class WrapCommonToHybridCommand(
    val commonCommand: Command.CommonData
) : Command.Hybrid {
    override fun execute(input: GameScenario.Hybrid): GameScenario.Hybrid {
        return input.copy(commonData = commonCommand.execute(input.commonData))
    }

    override fun undo(input: GameScenario.Hybrid): GameScenario.Hybrid {
        return input.copy(commonData = commonCommand.undo(input.commonData))
    }
}