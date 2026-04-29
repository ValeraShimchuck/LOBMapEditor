package ua.valeriishymchuk.lobmapeditor.domain.property

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.joml.Vector2f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.AngleDial

interface PositionProperty<SELF : PositionProperty<SELF>> : DomainProperty<SELF> {

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

            val rerenderTriggerState by editorService.rerenderTrigger.collectAsState()
            val finalRerenderTriggerState = rerenderTriggerState
            val scenarioNullable by editorService.scenario.collectAsState()
            val scenario = scenarioNullable ?: return

            val selectedObjectsRaw = selectedObjectsReferences.map { it.dereference(scenario) }
            if (!selectedObjectsRaw.all { it is PositionProperty }) return

            val selectedObjects = selectedObjectsRaw.map { it as PositionProperty }
            if (selectedObjects.isEmpty()) return


            val isXPositionMixed by derivedStateOf { selectedObjects.map { it.position.x }.distinct().size > 1 }
            val isYPositionMixed by derivedStateOf { selectedObjects.map { it.position.y }.distinct().size > 1 }


            var xPositionTextFieldValue by remember(finalRerenderTriggerState) {
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


            var yPositionTextFieldValue by remember(finalRerenderTriggerState) {
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

            Spacer(Modifier.height(10.dp))
            Text("Position:")
            // position
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {

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

            if (!selectedObjects.all { it.rotation != null }) return

            val isRotationMixed by derivedStateOf { selectedObjects.map { it.rotation }.distinct().size > 1 }

            var rotationTextFieldValue by remember(finalRerenderTriggerState) {
                mutableStateOf(
                    Unit.let {

                        val currentText = when {
                            selectedObjects.isEmpty() -> ""
                            isRotationMixed -> ""
                            else -> selectedObjects.map {
                                org.joml.Math.toDegrees(it.rotation!!)
                            }.distinct().firstOrNull()?.toString() ?: ""
                        }

                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length) // Or calculate appropriate position
                        )
                    }
                )
            }

            LaunchedEffect(selectedObjectsReferences) {

                val textValue = rotationTextFieldValue.text.toFloatOrNull()
                val rotation = selectedObjects.map {
                    org.joml.Math.toDegrees(it.rotation!!)
                }.distinct().firstOrNull()
                if (textValue != rotation || (textValue != null && isRotationMixed)) {
                    val finalValue: String = if (rotation != null && !isRotationMixed) rotation.toString()
                    else ""
                    rotationTextFieldValue = rotationTextFieldValue.copy(text = finalValue)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text("Rotation:")
            AngleDial(
                org.joml.Math.toRadians(rotationTextFieldValue.text.toFloatOrNull() ?: 0f),
                color = Color(230, 230, 230),
                modifier = Modifier.size(100.dp)
            )

            if (isRotationMixed) {
                Spacer(Modifier.height(4.dp))
                Text("Mixed")
                Spacer(Modifier.height(4.dp))
            }

            TextField(
                value = rotationTextFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    rotationTextFieldValue = newValue
                    rotationTextFieldValue = rotationTextFieldValue.copy(
                        text = newValue.text
                            .replace(Regex("[^0-9.]"), "").let { str ->
                                val value = str.toFloatOrNull() ?: return@let str
                                val coercedValue = value.coerceIn(0f, 359f)
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )


                    val finalText: Float = org.joml.Math.toRadians(
                        rotationTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                    )

                    onUpdate {
                        it.withRotation(finalText)
                    }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        onFlush()
                    }
                },
                placeholder = { Text(if (isRotationMixed) "Mixed" else "0") },

                )

            Slider(
                value = org.joml.Math.toRadians(rotationTextFieldValue.text.toFloatOrNull() ?: 0f),
                onValueChange = { newRotation ->

                    rotationTextFieldValue = rotationTextFieldValue.copy(
                        text = org.joml.Math.toDegrees(newRotation).coerceIn(0f, 359f).toString()
                    )

                    onUpdate {
                        it.withRotation(org.joml.Math.toRadians(rotationTextFieldValue.text.ifEmpty { "0" }
                            .toFloatOrNull() ?: 0f))
                    }
                },
                valueRange = 0f..(2 * Math.PI).toFloat(),
                modifier = Modifier.fillMaxWidth().onFocusChanged {
                    if (!it.isFocused) {
                        onFlush()
                    }
                }
            )
        }


    }

}