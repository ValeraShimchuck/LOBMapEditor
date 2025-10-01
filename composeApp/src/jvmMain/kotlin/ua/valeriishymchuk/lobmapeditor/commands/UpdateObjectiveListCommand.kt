package ua.valeriishymchuk.lobmapeditor.commands

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective

data class UpdateObjectiveListCommand(
    private val oldObjectives: List<Objective>,
    private val newObjectives: List<Objective>,
): Command.CommonData {
    override fun execute(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(objectives = newObjectives)
    }

    override fun undo(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(objectives = oldObjectives)
    }
}