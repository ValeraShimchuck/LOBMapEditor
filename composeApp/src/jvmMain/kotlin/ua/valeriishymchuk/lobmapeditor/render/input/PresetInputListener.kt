package ua.valeriishymchuk.lobmapeditor.render.input

import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService

class PresetInputListener(di: DI) : InputListener<GameScenario.Preset>(di) {


    override val editorService: PresetEditorService by lazy {
        super.editorService as PresetEditorService
    }

    val scenario: GameScenario.Preset get() = editorService.scenario.value!!

    override fun getAllObjects(): List<ScenarioReference> {
        val superList = super.getAllObjects()
        return superList.toMutableList().apply {
            addAll(scenario.units.indices.map {
                GameUnit.ScenarioUnitReference(it)
            })
        }
    }


}