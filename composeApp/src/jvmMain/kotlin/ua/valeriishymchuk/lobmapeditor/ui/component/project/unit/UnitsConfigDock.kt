package ua.valeriishymchuk.lobmapeditor.ui.component.project.unit

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
import com.jogamp.opengl.awt.GLCanvas
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
import org.joml.Vector3f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.toVector2f
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService.Companion.deleteUnits
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.DockContainer
import kotlin.getValue

@OptIn(ExperimentalJewelApi::class)
@Composable
fun UnitsConfigDock() {
    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()

    val scenario by editorService.scenario.collectAsState()

    val canvas by rememberInstance<GLCanvas>()

    DockContainer(
        startComponent = {
            Row(it) {
                Icon(AllIconsKeys.Nodes.Editorconfig, null)
                Spacer(Modifier.width(4.dp))
                Text("Units configuration")
            }
        },
        endComponent = {
            Row(it.widthIn(min = 140.dp, max = 300.dp).wrapContentWidth()) {

                val selectedUnits by editorService.selectedUnits.collectAsState()
                val filterText = remember { TextFieldState() }


                val filteredUnits = remember(filterText.text, scenario) {
                    scenario!!.units.filter { unit ->
                        filterText.text.split(Regex(" ")).any { part ->
                            unit.type.name.contains(part, ignoreCase = true) ||
                                    (unit.name?.contains(part, ignoreCase = true) ?: false)
                        }
                    }
                }


//                val labelText = currentSelectedUnits.joinToString(", ") { it.name ?: it.type.name }


                EditableComboBox(
                    filterText,


                    popupModifier = Modifier,

                ) {
                    VerticallyScrollableContainer(
                        Modifier.heightIn( max= 350.dp)
                    ) {

                        Column(Modifier.padding(end = 8.dp)) {

                            if ( filteredUnits.isEmpty() ) {
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
                                val map = selectedUnits.map { it.getValue(scenario!!.units::get) }

                                val state = when {
                                    map.toSet() == filteredUnits.toSet() -> ToggleableState.On
                                    map.toSet().intersect(filteredUnits.toSet())
                                        .isNotEmpty() -> ToggleableState.Indeterminate

                                    else -> ToggleableState.Off
                                }

                                TriStateCheckbox(
                                    state = state,
                                    onClick = {
                                        when (state) {
                                            ToggleableState.Off, ToggleableState.Indeterminate ->
                                                editorService.selectedUnits.value += filteredUnits.map {
                                                    Reference<Int, GameUnit>(
                                                        scenario!!.units.indexOf(it)
                                                    )
                                                }

                                            ToggleableState.On -> editorService.selectedUnits.value =
                                                setOf()
                                        }

                                        canvas.repaint()
                                    }
                                )
                                Spacer(Modifier.weight(1f))
                                Spacer(Modifier.weight(1f))
                                Row {

                                    IconActionButton(AllIconsKeys.Actions.MoveToButton, null, onClick = {

                                        editorService.cameraPosition =
                                            selectedUnits.map { it.getValue(scenario!!.units::get).position.toVector2f() }
                                                .let {
                                                    it.fold(Vector2f()) { sum, vector ->
                                                        sum.add(vector) // Накопичуємо суму в sum
                                                    }.div(it.size.toFloat())
                                                }.also { println("${it.x} ${it.y}")}

                                        canvas.repaint()
                                    })

                                    IconActionButton(AllIconsKeys.General.Delete, null, onClick = {
                                        editorService.deleteUnits(selectedUnits.toSet())
                                        canvas.repaint()
                                    })

                                }
                            }

                            filteredUnits.forEach { unit ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Checkbox(
                                        selectedUnits.map { it.getValue(scenario!!.units::get) }.contains(unit),
                                        onCheckedChange = {
                                            if (it) editorService.selectedUnits.value += Reference<Int, GameUnit>(
                                                scenario!!.units.indexOf(unit)
                                            )
                                            else editorService.selectedUnits.value =
                                                editorService.selectedUnits.value.filter {
                                                    it.getValue(scenario!!.units::get) != unit
                                                }.toSet()

                                            canvas.repaint()
                                        }
                                    )
                                    Text(unit.type.name, Modifier.weight(1f))
                                    Text(unit.name ?: "", Modifier.weight(1f))
                                    Row {
                                        IconActionButton(AllIconsKeys.Actions.MoveToButton, null, onClick = {
                                            editorService.cameraPosition = Vector2f(
                                                unit.position.x,
                                                unit.position.y
                                            )
                                            canvas.repaint()
                                        })
                                        IconActionButton(AllIconsKeys.General.Delete, null, onClick = {
                                            editorService.selectedUnits.value =
                                                editorService.selectedUnits.value.filter {
                                                    it.getValue(scenario!!.units::get) != unit
                                                }.toSet()

                                            editorService.deleteUnits(
                                                setOf(
                                                    Reference(
                                                        scenario!!.units.indexOf(unit)
                                                    )
                                                )
                                            )


                                            canvas.repaint()
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
            VerticallyScrollableContainer {
                Column {
                    scenario!!.units.forEach { u ->
                        Text(u.type.toString() + " " + u.name)
                    }
                }
            }
        }
    )
}
