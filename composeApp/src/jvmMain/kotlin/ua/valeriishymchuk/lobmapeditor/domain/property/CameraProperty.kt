package ua.valeriishymchuk.lobmapeditor.domain.property

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.Text
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.common.CenteredRow
import ua.valeriishymchuk.lobmapeditor.ui.component.common.DefaultVSpacer
import ua.valeriishymchuk.lobmapeditor.ui.component.common.FloatTextField
import kotlin.getValue

interface CameraProperty<SELF : CameraProperty<SELF>> : DomainProperty<SELF> {

    val zoom: Float?
    val duration: Float

    fun withZoom(zoom: Float?): SELF
    fun withDuration(duration: Float): SELF

    companion object {
        @OptIn(ExperimentalJewelApi::class)
        @Composable
        fun Component(
            selectedObjectsReferences: Set<ScenarioReference>,
            onUpdate: ((CameraProperty<*>) -> CameraProperty<*>) -> Unit,
            onFlush: () -> Unit
        ) {
            val editorService by rememberInstance<EditorService<*>>()

            val scenarioNullable by editorService.scenario.collectAsState()
            val scenario = scenarioNullable ?: return

            val selectedObjectsRaw = selectedObjectsReferences.map { it.dereference(scenario) }
            if (!selectedObjectsRaw.all { it is CameraProperty }) return

            val selectedObjects = selectedObjectsRaw.map { it as CameraProperty }
            if (selectedObjects.isEmpty()) return

            val isZoomMixed by derivedStateOf { selectedObjects.map { it.zoom }.distinct().size > 1 }
            val isDurationMixed by derivedStateOf { selectedObjects.map { it.duration }.distinct().size > 1 }
            val isZoomEnabled = selectedObjects.map { it.zoom }.any { it != null }
            CenteredRow {
                Checkbox(
                    isZoomEnabled,
                    { newState ->
                        if (newState) {
                            onUpdate { property ->
                                property.withZoom(1.0f)
                            }
                        } else {
                            onUpdate {
                                it.withZoom(null)
                            }
                        }

                        onFlush()
                    },
                )
                if (isZoomEnabled) Text(" Zoom:")
                else {
                    Text(" Zoom")
                }
            }

            if (isZoomEnabled) {
                FloatTextField(
                    selectedObjectsReferences,
                    {
                        if (isZoomMixed) null
                        else selectedObjects.first().zoom
                    },
                    onFocusLoss = onFlush,
                    onUpdate = { zoom ->
                        onUpdate {
                            it.withZoom(zoom)
                        }
                    },
                    placeholder = if (isZoomMixed) "Mixed" else "0"
                )
            }

            DefaultVSpacer()

            Text("Duration: ")
            FloatTextField(
                selectedObjectsReferences,
                {
                    if (isDurationMixed) null
                    else selectedObjects.first().duration
                },
                onFocusLoss = onFlush,
                onUpdate = { duration ->
                    onUpdate {
                        it.withDuration(duration)
                    }
                },
                placeholder = if (isDurationMixed) "Mixed" else "0"
            )



        }
    }


}