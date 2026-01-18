package ua.valeriishymchuk.lobmapeditor.services.project.editor

import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.collections.getValue
import kotlin.collections.minusAssign

class PresetEditorService(
    di: DI,
): EditorService<GameScenario.Preset>(di) {

    val selectedUnits: MutableStateFlow<Set<Reference<Int, GameUnit>>> = MutableStateFlow(setOf())

    override fun importScenario(scenario: GameScenario.Preset) {
        lock {
            undoStack.clear()
            redoStack.clear()
            composedCommands.clear()
            selectedObjectives.value = null
            selectedUnits.value = setOf()
            this.scenario.value = scenario
            openglUpdateState.value++
            println("Importing project ${openglUpdateState.value}")
            savingJob = null
            save(true)
        }
    }

    fun deleteUnits(map: Set<Reference<Int, GameUnit>>) {
        selectedUnits.value -= map
        val oldSelectedUnits = selectedUnits.value.map { it.getValue(scenario.value!!.units::get) }
        val oldList = scenario.value!!.units
        val newList = oldList.filterIndexed { index, _ -> !map.contains(Reference(index)) }
        execute(UpdateGameUnitListCommand(oldList, newList))
        val newSelectedList = scenario.value!!.units.mapIndexedNotNull { index, unit ->
            if (oldSelectedUnits.contains(unit)) return@mapIndexedNotNull Reference<Int, GameUnit>(index)
            null
        }
        selectedUnits.value = newSelectedList.toSet()
    }

}
