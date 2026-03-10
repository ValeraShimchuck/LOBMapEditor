package ua.valeriishymchuk.lobmapeditor.ui.component.project.objects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.EditableComboBox
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TriStateCheckbox
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector2f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.toVector2f
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.shared.utils.StringSimilarityUtils
import ua.valeriishymchuk.lobmapeditor.ui.component.DockContainer
import kotlin.collections.plus
import kotlin.getValue



@OptIn(ExperimentalJewelApi::class)
@Composable
fun SelectionDock() {

    val diEditorService by rememberInstance<EditorService<*>>()
    val editorService = diEditorService

    val scenarioNullable by editorService.scenario.collectAsState()
    val scenario = scenarioNullable ?: return

    DockContainer(
        startComponent = {
            Row(it) {
                Icon(AllIconsKeys.Nodes.Editorconfig, null)
                Spacer(Modifier.width(4.dp))
                Text("Objects configuration")
            }
        },
        endComponent = { modifier ->
            Row(modifier.widthIn(min = 140.dp, max = 300.dp).wrapContentWidth()) {
                val selectedObjects by editorService.selectedObjects.collectAsState()
                val filterText = remember { TextFieldState() }



                val filteredObjects = remember(filterText.text, scenario) {

                    val allObjects = editorService.getAllObjects()
                    val candidates = allObjects.map {
                        ref ->
                        val value = ref.dereference(scenario).identification
                        ref to (value to StringSimilarityUtils.jaroDistance(
                            value.lowercase(),
                            filterText.text.toString().lowercase()
                        ))
                    }
                    val sortedCandidates = candidates
                        .filter { (_, value) -> value.second > 0.3 }
                        .sortedBy { (_, value) -> 1.0 - value.second }
//                        .reversed()
                    if (sortedCandidates.isEmpty()) return@remember candidates
                    sortedCandidates
                }

                val filteredObjectRefs = filteredObjects.map { pair -> pair.first }.toSet()

                EditableComboBox(
                    filterText,
                    popupModifier = Modifier,
                ) {
                    VerticallyScrollableContainer(
                        Modifier.heightIn( max= 350.dp)
                    ) {

                        Column(Modifier.padding(end = 8.dp)) {

                            if (filteredObjects.isEmpty() ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                ) {
                                    Text("No results...", color = JewelTheme.globalColors.text.info)
                                }
                                return@Column
                            }


                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                val map = selectedObjects

                                val state = when {
                                    map.toSet() == filteredObjectRefs -> ToggleableState.On
                                    map.toSet().intersect(filteredObjectRefs)
                                        .isNotEmpty() -> ToggleableState.Indeterminate

                                    else -> ToggleableState.Off
                                }

                                TriStateCheckbox(
                                    state = state,
                                    onClick = {
                                        when (state) {
                                            ToggleableState.Off, ToggleableState.Indeterminate ->
                                                editorService.selectedObjects.value += filteredObjectRefs

                                            ToggleableState.On -> editorService.selectedObjects.value = setOf()
                                        }

                                    }
                                )
                                Spacer(Modifier.weight(1f))
                                Spacer(Modifier.weight(1f))
                                Row {

                                    IconActionButton(AllIconsKeys.Actions.MoveToButton, null, onClick = {

                                        editorService.cameraPosition =
                                            selectedObjects.mapNotNull { ref ->
                                                (ref.dereference(scenario) as? PositionProperty)?.position?.toVector2f()
                                            }.let { collection ->
                                                if (collection.isEmpty()) return@let editorService.cameraPosition
                                                collection.fold(Vector2f()) { sum, vector ->
                                                        sum.add(vector)
                                                    }.div(collection.size.toFloat())
                                                }

                                    })

                                    IconActionButton(AllIconsKeys.General.Delete, null, onClick = {
                                        editorService.deleteObjects(selectedObjects)
                                    })

                                }
                            }

                            filteredObjects.forEach { (ref, pair) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    val obj = ref.dereference(scenario)
                                    val position = (obj as? PositionProperty)?.position
                                    Checkbox(
                                        selectedObjects.contains(ref),
                                        onCheckedChange = {
                                            if (it) editorService.selectedObjects.value += ref
                                            else editorService.selectedObjects.value -= ref

                                        }
                                    )
                                    Text(pair.first, Modifier.weight(1f))

                                    Row {
                                        position?.let { position ->
                                            IconActionButton(AllIconsKeys.Actions.MoveToButton, null, onClick = {
                                                editorService.cameraPosition = Vector2f(
                                                    position.x,
                                                    position.y
                                                )
                                            })
                                        }

                                        IconActionButton(AllIconsKeys.General.Delete, null, onClick = {
                                            editorService.selectedObjects.value -= ref
                                            editorService.deleteObjects(setOf(ref))
                                        })

                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        content = {
            PropertyDock()
        }
    )

}