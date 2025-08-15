package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

data class UpdateScenarioNameCommand(
    val oldName: String,
    val newName: String
): Command.CommonData {
    override fun execute(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(name = newName)
    }

    override fun undo(input: GameScenario.CommonData): GameScenario.CommonData {
        return input.copy(name = oldName)
    }

}
