package ua.valeriishymchuk.lobmapeditor.ui.component.project.unit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jogamp.opengl.awt.GLCanvas
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.EditableComboBox
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.rememberProvider
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
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
            Row(it.widthIn(min = 120.dp, max = 260.dp).wrapContentWidth()) {

                val selectedUnits by editorService.selectedUnits.collectAsState()
                val filterText = remember { TextFieldState() }


                val filteredItems = remember(filterText.text, scenario) {
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
                ) {
                    VerticallyScrollableContainer {
                        Column {
                            filteredItems.forEach { unit ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
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
                                        IconActionButton(AllIconsKeys.Actions.MoveToButton, null, onClick = {})
                                        IconActionButton(AllIconsKeys.General.Delete, null, onClick = {
                                            editorService.selectedUnits.value =
                                                editorService.selectedUnits.value.filter {
                                                    it.getValue(scenario!!.units::get) != unit
                                                }.toSet()



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
