package ua.valeriishymchuk.lobmapeditor.ui.component.project.objective

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.theme.defaultButtonStyle
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdatePlayerListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.objective.ObjectiveType
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService.Companion.deleteUnits
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceObjectiveTool
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.getValue
import kotlin.math.max
import kotlin.text.ifEmpty

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun ObjectivePropertiesConfig() {

    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val scenario by editorService.scenario.collectAsState()
    val rawSelection by editorService.selectedObjectives.collectAsState()

    if (rawSelection == null) return

    val selection by derivedStateOf {
        rawSelection!!.getValueOrNull(scenario!!.objectives::getOrNull)
    }

    if (selection == null) return

    fun updateObjective(updater: (Objective) -> Objective) {
        val oldList = scenario!!.objectives
        val newList = oldList.mapIndexed { index, objective ->
            if (index != rawSelection!!.key) return@mapIndexed objective
            return@mapIndexed updater(objective)
        }
        val command = UpdateObjectiveListCommand(
            oldList,
            newList
        )

        if (oldList == newList) return
        editorService.executeCompoundCommon(command)
    }

    val ownerPopupManager = remember { PopupManager() }
    val typePopupManager = remember { PopupManager() }

    var textFieldValue by remember(rawSelection) {
        mutableStateOf(
            TextFieldValue(
                text = selection!!.name ?: "",
                selection = TextRange(selection!!.name?.length ?: 0)
            )
        )
    }

    var xPositionTextFieldValue by remember {
        mutableStateOf(
            Unit.let {
                val currentText = selection!!.position.x.toString()
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // Or calculate appropriate position
                )
            }
        )
    }

    LaunchedEffect(rawSelection) {
        xPositionTextFieldValue = xPositionTextFieldValue.copy(text = selection!!.position.x.toString())
    }


    var yPositionTextFieldValue by remember {
        mutableStateOf(
            Unit.let {
                val currentText = selection!!.position.y.toString()
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // Or calculate appropriate position
                )
            }
        )
    }

    LaunchedEffect(rawSelection) {
        yPositionTextFieldValue = yPositionTextFieldValue.copy(text = selection!!.position.y.toString())
    }

    VerticallyScrollableContainer {
        Column {
            Spacer(Modifier.height(4.dp))

            Text("Name:")
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    textFieldValue = newValue

                    val finalText: String? = newValue.text.ifEmpty { null }
                    updateObjective { it.copy(name = finalText) }
                },
                modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        editorService.flushCompoundCommon()
                    }
                },
                placeholder = { Text("Empty") }
            )

            Spacer(Modifier.height(4.dp))

            Text("Owner:")
            val ownerLabel: String = if (selection!!.owner == null) "No one"
            else "${selection!!.owner!!.key + 1} ${scenario!!.players[selection!!.owner!!.key].team}"
            ComboBox(labelText = ownerLabel,
                popupManager = ownerPopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                    updateObjective { objective -> objective.copy(owner = null) }
                                    editorService.flushCompoundCommon()
                                    ownerPopupManager.setPopupVisible(false)
                                }) {
                                Text(
                                    text = "No one",
                                )
                            }

                            scenario!!.players.withIndex().sortedByDescending {
                                it.index
                            }.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        updateObjective { objective -> objective.copy(owner = Reference(item.index)) }
                                        editorService.flushCompoundCommon()
                                        ownerPopupManager.setPopupVisible(false)
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
            Spacer(Modifier.height(4.dp))


            Text("Type:")
            ComboBox(
                labelText = "${selection!!.type}",
                popupManager = typePopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {

                            ObjectiveType.entries.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        updateObjective { it.copy(type = item) }
                                        editorService.flushCompoundCommon()
                                        typePopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "$item",
                                    )
                                }

                            }
                        }
                    }
                })

            Spacer(Modifier.height(4.dp))


            Text("Position:")
            Row(horizontalArrangement = Arrangement.Center) {
                TextField(
                    value = xPositionTextFieldValue,
                    onValueChange = { newValue ->
                        xPositionTextFieldValue = newValue
                        xPositionTextFieldValue = xPositionTextFieldValue.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, scenario!!.map.widthPixels.toFloat())
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = xPositionTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateObjective { it.copy(position = it.position.copy(x = finalText)) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompoundCommon()
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
                    value = yPositionTextFieldValue,
                    onValueChange = { newValue ->
                        yPositionTextFieldValue = newValue
                        yPositionTextFieldValue = yPositionTextFieldValue.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, scenario!!.map.heightPixels.toFloat())
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = yPositionTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateObjective { it.copy(position = it.position.copy(y = finalText)) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompoundCommon()
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


            Text("Position:")
            // Victory Points
            var victoryPointsTextValue by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = selection!!.victoryPoints.toString(),
                        selection = TextRange(selection!!.victoryPoints.toString().length) // Or calculate appropriate position
                    )
                )
            }
            TextField(
                value = victoryPointsTextValue,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { newValue ->
                    victoryPointsTextValue = newValue
                    victoryPointsTextValue = victoryPointsTextValue.copy(
                        text = newValue.text
                            .replace(Regex("[^0-9]"), "").let { str ->
                                val value = str.toIntOrNull() ?: return@let str
                                val coercedValue = max(value, Objective.MIN_VICTORY_POINTS)
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )

                    val finalText: Int = victoryPointsTextValue.text.ifEmpty { Objective.MIN_VICTORY_POINTS.toString() }.toIntOrNull() ?: Objective.MIN_VICTORY_POINTS

                    updateObjective { it.copy(victoryPoints = finalText) }


                },

                leadingIcon = {
                    Row {
                        Text("Victory Points", color = JewelTheme.globalColors.text.info)
                        Spacer(Modifier.width(4.dp))
                    }
                }
            )

            // Delete objective button
            DefaultButton(
                style = JewelTheme.defaultButtonStyle.let { style ->
                    val color = Color(196, 27, 27, 255)
                    val color2 = Color(182, 25, 25, 255)
                    val color3 = Color(165, 21, 21, 255)
                    ButtonStyle(
                        colors = ButtonColors(
                            Brush.linearGradient(listOf(color, color)),
                            style.colors.backgroundDisabled,
                            Brush.linearGradient(listOf(color, color)),
                            Brush.linearGradient(listOf(color3, color3)),
                            Brush.linearGradient(listOf(color2, color2)),
                            style.colors.content,
                            style.colors.contentDisabled,
                            style.colors.contentFocused,
                            style.colors.contentPressed,
                            style.colors.contentHovered,
                            style.colors.border,
                            style.colors.borderDisabled,
                            style.colors.borderFocused,
                            style.colors.borderPressed,
                            style.colors.borderHovered
                        ),
                        metrics = style.metrics,
                        focusOutlineAlignment = style.focusOutlineAlignment
                    )
                },
                onClick = {
                    editorService.deleteObjectives(setOf(rawSelection!!))
                },
            ) {
                Text("Delete objective")
            }

        }
    }
}