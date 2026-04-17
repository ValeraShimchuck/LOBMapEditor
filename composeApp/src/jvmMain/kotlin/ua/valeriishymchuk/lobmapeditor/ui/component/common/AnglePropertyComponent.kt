package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Slider
import ua.valeriishymchuk.lobmapeditor.ui.component.AngleDial

@Composable
fun AnglePropertyComponent(defaultRotation: Float, onUpdate: (Float) -> Unit, color: Color, onFlush: () -> Unit) {
    var angle by remember { mutableStateOf(defaultRotation) }
    LaunchedEffect(angle) {
        onUpdate(angle)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Slider(
            value = angle,
            onValueChange = { angle = it },
            valueRange = 0f..(2 * Math.PI).toFloat(),
            modifier = Modifier.fillMaxWidth().weight(0.8f).onFocusChanged {
                if (!it.isFocused) {
                    onFlush()
                }
            }
        )

        AngleDial(
            angle,
            color = color,
            modifier = Modifier.fillMaxSize().weight(0.2f)
        )

    }
}