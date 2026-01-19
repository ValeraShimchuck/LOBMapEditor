package ua.valeriishymchuk.lobmapeditor.services.project.editor

import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

class HybridEditorService(di: DI) : EditorService<GameScenario.Hybrid>(di) {

    // TODO add deployment zone selection, probably will need it

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
}