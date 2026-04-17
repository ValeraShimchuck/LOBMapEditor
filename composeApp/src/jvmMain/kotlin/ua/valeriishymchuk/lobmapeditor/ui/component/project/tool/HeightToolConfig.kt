package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.services.project.tool.HeightTool
import kotlin.math.roundToInt

@Composable
fun HeightToolConfig() {
    val currentHeight by HeightTool.height.collectAsState()

    var value by remember { mutableStateOf(currentHeight.toFloat()) }

    LaunchedEffect(value) {
        HeightTool.height.value = value.roundToInt()
    }


    BrushToolConfig(HeightTool)

    Text("Height: $currentHeight")
    Slider(
        value = value, // Float
        onValueChange = { newValue ->
            value = newValue
        }, valueRange = 0f..Terrain.MAX_TERRAIN_HEIGHT.toFloat(),
        steps = 0,
        modifier = Modifier.fillMaxWidth()
    )
}