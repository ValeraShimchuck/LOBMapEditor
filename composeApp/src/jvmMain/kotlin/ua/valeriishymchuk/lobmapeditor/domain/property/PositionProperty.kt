package ua.valeriishymchuk.lobmapeditor.domain.property

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.joml.Vector2f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import kotlin.getValue
import kotlin.text.ifEmpty

interface PositionProperty<SELF: PositionProperty<SELF>> : DomainProperty<SELF> {

    val position: Position
    val hitboxDimensions: Vector2f
    val rotation: Float? // in radians

    fun withPosition(pos: Position): SELF
    fun withRotation(rotation: Float): SELF

    companion object {

        @OptIn(ExperimentalJewelApi::class)
        @Composable
        fun Component(
            selectedObjectsReferences: Set<ScenarioReference>,
            onUpdate: ((PositionProperty<*>) -> PositionProperty<*>) -> Unit,
            onFlush: () -> Unit
        ) {
            val editorService by rememberInstance<EditorService<*>>()


            val scenarioNullable by editorService.scenario.collectAsState()
            val scenario = scenarioNullable ?: return

            val selectedObjectsRaw = selectedObjectsReferences.map { it.dereference(scenario) }
            if (!selectedObjectsRaw.all { it is PositionProperty }) return

            val selectedObjects = selectedObjectsRaw.map { it as PositionProperty }
            if (selectedObjects.isEmpty()) return


            val isXPositionMixed by derivedStateOf { selectedObjects.map { it.position.x }.distinct().size > 1 }
            val isYPositionMixed by derivedStateOf { selectedObjects.map { it.position.y }.distinct().size > 1 }

            var xPositionTextFieldValue by remember {
                mutableStateOf(
                    Unit.let {
                        val currentText = when {
                            isXPositionMixed -> ""
                            else -> selectedObjects.map { it.position.x }.distinct().firstOrNull()?.toString() ?: ""
                        }
                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length) // Or calculate appropriate position
                        )
                    }
                )
            }

            // xPosition Selection handler
            LaunchedEffect(selectedObjectsReferences) {
                val textValue = xPositionTextFieldValue.text.toFloatOrNull()
                val xValue = selectedObjects.map { it.position.x }.distinct().firstOrNull()
                if (textValue != xValue || (textValue != null && isXPositionMixed)) {
                    val finalValue: String = if (xValue != null && !isXPositionMixed) xValue.toString()
                    else ""
                    xPositionTextFieldValue = xPositionTextFieldValue.copy(text = finalValue)
                }
            }


            var yPositionTextFieldValue by remember {
                mutableStateOf(
                    Unit.let {
                        val currentText = when {
                            isYPositionMixed -> ""
                            else -> selectedObjects.map { it.position.y }.distinct().firstOrNull()?.toString() ?: ""
                        }
                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length) // Or calculate appropriate position
                        )
                    }
                )
            }

            // yPosition Selection handler
            LaunchedEffect(selectedObjectsReferences) {

                val textValue = yPositionTextFieldValue.text.toFloatOrNull()
                val yValue = selectedObjects.map { it.position.y }.distinct().firstOrNull()
                if (textValue != yValue || (textValue != null && isYPositionMixed)) {
                    val finalValue: String = if (yValue != null && !isYPositionMixed) yValue.toString()
                    else ""
                    yPositionTextFieldValue = yPositionTextFieldValue.copy(text = finalValue)
                }
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Position")
                Spacer(Modifier.width(10.dp))
                TextField(
                    value = xPositionTextFieldValue,
                    onValueChange = { newValue ->
                        // Simply update the state with the complete new value
                        xPositionTextFieldValue = newValue
                        xPositionTextFieldValue = xPositionTextFieldValue.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, scenario.map.widthPixels.toFloat())
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = xPositionTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        onUpdate {
                            it.withPosition(it.position.copy(x = finalText))
                        }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            onFlush()
                        }
                    },
                    placeholder = { Text(if (isXPositionMixed) "Mixed" else "0") },

                    leadingIcon = {
                        Row {
                            Text("X", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )
                Spacer(Modifier.width(4.dp))

                TextField(
                    value = yPositionTextFieldValue,
                    onValueChange = { newValue ->
                        // Simply update the state with the complete new value
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
                        onUpdate {
                            it.withPosition(it.position.copy(y = finalText))
                        }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            onFlush()
                        }
                    },
                    placeholder = { Text(if (isYPositionMixed) "Mixed" else "0") },

                    leadingIcon = {
                        Row {
                            Text("Y", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )
            }

        }
    }

}