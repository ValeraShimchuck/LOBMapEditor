package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.SegmentedControl
import org.jetbrains.jewel.ui.component.SegmentedControlButtonData
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import ua.valeriishymchuk.lobmapeditor.services.project.tools.BrushTool
import kotlin.math.roundToInt

private val brushRange = 1f..16f

@Composable
fun BrushToolConfig(tool: BrushTool) {
    val brushSize by tool.brushSize.collectAsState() // одне джерело правди
    val shape by tool.brushShape.collectAsState()

    // Текстове поле
    val sizeTextFieldState = rememberTextFieldState(brushSize.toString())

    // Якщо brushSize змінюється – оновлюємо текст у полі
    LaunchedEffect(brushSize) {
        val asString = brushSize.toString()
        if (sizeTextFieldState.text != asString) {
            sizeTextFieldState.edit {
                replace(0, sizeTextFieldState.text.length, asString)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Brush size (diameter)")
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Jewel TextField працює напряму з state
            TextField(
                sizeTextFieldState,
                modifier = Modifier.widthIn(max = 60.dp)
            )

            Spacer(Modifier.width(9.dp))

            var sliderValue by remember { mutableFloatStateOf(tool.brushSize.value.toFloat()) }


            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue              // плавно оновлюємо float
                    tool.brushSize.value = newValue.roundToInt() // синхронізуємо з int
                },

                valueRange = brushRange.start..brushRange.endInclusive,
                modifier = Modifier.weight(1f)
            )
        }

        SegmentedControl(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            buttons = listOf(
                SegmentedControlButtonData(
                    selected = shape == BrushTool.BrushShape.SQUARE,
                    onSelect = { tool.brushShape.value = BrushTool.BrushShape.SQUARE },
                    content = { Text("Square") }
                ),
                SegmentedControlButtonData(
                    selected = shape == BrushTool.BrushShape.CIRCLE,
                    onSelect = { tool.brushShape.value = BrushTool.BrushShape.CIRCLE },
                    content = { Text("Circle") }
                ),
            )
        )
    }

    // Окремо слідкуємо за текстовим інпутом і оновлюємо brushSize
    LaunchedEffect(sizeTextFieldState) {
        snapshotFlow { sizeTextFieldState.text }
            .collect { rawText ->
                rawText
                    .replace(Regex("[^0-9]"), "")
                    .toIntOrNull()
                    ?.coerceIn(
                        brushRange.start.toInt()..brushRange.endInclusive.toInt()
                    )
                    ?.let { tool.brushSize.value = it }
            }
    }
}