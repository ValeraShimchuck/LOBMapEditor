package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

data class UpdateScenarioDescriptionCommand(
    val oldDescription: String,
    val newDescription: String
): Command.CommonData {
    override fun execute(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(description = newDescription)
    }

    override fun undo(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(description = oldDescription)
    }

}
