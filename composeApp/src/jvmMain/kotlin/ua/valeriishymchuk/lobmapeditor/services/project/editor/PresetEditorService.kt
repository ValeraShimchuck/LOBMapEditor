package ua.valeriishymchuk.lobmapeditor.services.project.editor

import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.collections.getValue
import kotlin.collections.minusAssign

class PresetEditorService(
    di: DI,
): EditorService<GameScenario.Preset>(di) {


    override fun importScenario(scenario: GameScenario.Preset) {
        lock {
            undoStack.clear()
            redoStack.clear()
            composedCommands.clear()
            selectedObjects.value = setOf()
            this.scenario.value = scenario
            openglUpdateState.value++
            println("Importing project ${openglUpdateState.value}")
            savingJob = null
            save(true)
        }
    }

    override fun getAllObjects(): Set<ScenarioReference> {
        val superList = super.getAllObjects()
        return superList.toMutableSet().apply {
            addAll(scenario.value!!.units.indices.map {
                GameUnit.ScenarioUnitReference(it)
            })
        }
    }

    override fun castCommandOrFail(command: Command<*>): Command<GameScenario.Preset> {
        return command as? Command.Preset ?: throw IllegalArgumentException("Invalid command: $command")
    }

    override fun convertCommonCommand(command: Command.CommonData): Command<GameScenario.Preset> {
        return command.asPreset()
    }

}
