package ua.valeriishymchuk.lobmapeditor.services.project.editor

import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

class HybridEditorService(di: DI) : EditorService<GameScenario.Hybrid>(di) {

    override fun importScenario(scenario: GameScenario.Hybrid) {
        lock {
            undoStack.clear()
            redoStack.clear()
            composedCommands.clear()
            selectedObjectives.value = null
            this.scenario.value = scenario
            openglUpdateState.value++
            println("Importing project ${openglUpdateState.value}")
            savingJob = null
            save(true)
        }
    }

    override fun castCommandOrFail(command: Command<*>): Command<GameScenario.Hybrid> {
        return command as? Command.Hybrid ?: throw IllegalArgumentException("Invalid command: $command")
    }

    override fun convertCommonCommand(command: Command.CommonData): Command<GameScenario.Hybrid> {
        return command.asHybrid()
    }

}