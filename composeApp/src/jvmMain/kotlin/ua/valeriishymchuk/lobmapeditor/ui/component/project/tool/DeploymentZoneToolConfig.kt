package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateDeploymentZoneCommand
import ua.valeriishymchuk.lobmapeditor.domain.DeploymentZone
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.HybridEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.HybridToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.getValue
import kotlin.text.ifEmpty

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun DeploymentZoneToolConfig() {
    val diEditorService by rememberInstance<EditorService<*>>()
    val editorService = diEditorService as? HybridEditorService ?: return
    val diToolService by rememberInstance<ToolService<*>>()
    val toolService = diToolService as HybridToolService

    val deploymentZoneTool = toolService.deploymentZoneTool
    val selectedReference by deploymentZoneTool.selected.collectAsState()
    val nullableScenario by editorService.scenario.collectAsState()
    val scenario = nullableScenario ?: return
    val selected = selectedReference?.getValue(scenario.deploymentZones::get)
    val isHidden by deploymentZoneTool.isHidden.collectAsState()
    val canBeSelected by deploymentZoneTool.canBeSelected.collectAsState()

    val zonePopupManager = remember { PopupManager() }




    Column {

        Spacer(Modifier.height(4.dp)) // hide checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text("Hide")
            Spacer(Modifier.width(4.dp))
            Checkbox(isHidden, onCheckedChange = {
                deploymentZoneTool.isHidden.value = it
                deploymentZoneTool.selected.value = null
            })
        }

        Spacer(Modifier.height(4.dp)) // allow selection checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Allow selection")
            Spacer(Modifier.width(4.dp))
            Checkbox(canBeSelected, onCheckedChange = {
                deploymentZoneTool.canBeSelected.value = it
                deploymentZoneTool.selected.value = null
            })
        }

        Spacer(Modifier.height(4.dp))
        Text("Current Zone")
        ComboBox(
            labelText = selectedReference?.let { "${it.key + 1} ${PlayerTeam.entries[it.key]}" } ?: "None",
            popupManager = zonePopupManager,
            popupContent = {
                VerticallyScrollableContainer {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                deploymentZoneTool.selected.value = null
                                zonePopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = "None",
                            )
                        }
                        scenario.deploymentZones.withIndex().forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                    deploymentZoneTool.selected.value = Reference(item.index)
                                    zonePopupManager.setPopupVisible(false)
                                }) {
                                Text(
                                    text = "${item.index + 1} ${item.value.team}",
                                )
                            }
                        }
                    }
                }
            }
        )

        // selected deployment zone properties
        if (selected != null) {
            fun updateSelection(updater: (DeploymentZone) -> DeploymentZone) {
                val newZone = updater(selected)
                val command = UpdateDeploymentZoneCommand(
                    selectedReference!!.key,
                    selected,
                    newZone
                )

                if (selected == newZone) return
                editorService.executeCompound(command)
            }

            var positionX by remember {
                val text = selected.position.x.toString()
                mutableStateOf(
                    TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }

            LaunchedEffect(selectedReference) {
                positionX = positionX.copy(text = selected.position.x.toString())
            }

            var positionY by remember {
                val text = selected.position.y.toString()
                mutableStateOf(
                    TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }

            LaunchedEffect(selectedReference) {
                positionY = positionY.copy(text = selected.position.y.toString())
            }

            var width by remember {
                val text = selected.width.toString()
                mutableStateOf(
                    TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }

            LaunchedEffect(selectedReference) {
                width = positionX.copy(text = selected.width.toString())
            }

            var height by remember {
                val text = selected.height.toString()
                mutableStateOf(
                    TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }

            LaunchedEffect(selectedReference) {
                height = positionY.copy(text = selected.height.toString())
            }

            Spacer(Modifier.height(4.dp))
            Text("Position:")
            Row(horizontalArrangement = Arrangement.Center) {
                TextField(
                    value = positionX,
                    onValueChange = { newValue ->
                        positionX = newValue
                        positionX = positionX.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, scenario.map.widthPixels.toFloat())
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = positionX.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateSelection { it.copy(position = it.position.copy(x = finalText)) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    leadingIcon = {
                        Row {
                            Text("X", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )

                TextField(
                    value = positionY,
                    onValueChange = { newValue ->
                        positionY = newValue
                        positionY = positionY.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, scenario.map.heightPixels.toFloat())
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = positionY.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateSelection { it.copy(position = it.position.copy(y = finalText)) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    leadingIcon = {
                        Row {
                            Text("Y", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )
            }

            Spacer(Modifier.height(4.dp))
            Text("Size:")
            Row(horizontalArrangement = Arrangement.Center) {
                TextField(
                    value = width,
                    onValueChange = { newValue ->
                        width = newValue
                        width = width.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    value.toString()
                                }
                        )


                        val finalText: Float = width.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateSelection { it.copy(width = finalText) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    leadingIcon = {
                        Row {
                            Text("Width", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )

                TextField(
                    value = height,
                    onValueChange = { newValue ->
                        height = newValue
                        height = height.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    value.toString()
                                }
                        )


                        val finalText: Float = height.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateSelection { it.copy(height = finalText) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    leadingIcon = {
                        Row {
                            Text("Height", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )
            }
        }

    }
}