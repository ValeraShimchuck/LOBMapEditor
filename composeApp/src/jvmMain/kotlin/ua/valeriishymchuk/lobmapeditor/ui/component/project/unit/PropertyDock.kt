package ua.valeriishymchuk.lobmapeditor.ui.component.project.unit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.Command.Companion.applyAllCompound
import ua.valeriishymchuk.lobmapeditor.domain.property.DomainProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import kotlin.getValue
import kotlin.reflect.KClass

@Composable
fun PropertyDock() { // TODO continue adding new properties and add selection dock from old dock
    val editorService by rememberInstance<EditorService<*>>()

    val scenarioNullable by editorService.scenario.collectAsState()
    val scenario = scenarioNullable ?: return

    val selectedObjectReferences by editorService.selectedObjects.collectAsState()
    if (selectedObjectReferences.isEmpty()) return

    PositionProperty.Component(
        selectedObjectReferences,
        { updater ->
            ScenarioReference.updateList(PositionProperty::class, selectedObjectReferences,scenario, updater)
                .applyAllCompound(editorService)
        },
        {
            editorService.flushCompound()
        }
    )

}

