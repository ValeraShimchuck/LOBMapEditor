package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger

data class UpdateGameTriggerListCommand(
    val oldTriggers: List<GameTrigger>,
    val newTriggers: List<GameTrigger>,
): Command.CommonData {
    override fun execute(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(triggers = newTriggers)
    }

    override fun undo(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(triggers = oldTriggers)
    }

}
