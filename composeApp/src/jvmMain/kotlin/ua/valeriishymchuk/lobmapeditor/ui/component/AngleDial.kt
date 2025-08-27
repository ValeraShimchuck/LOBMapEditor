package ua.valeriishymchuk.lobmapeditor.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AngleDial(
    angleRad: Float,
    color: Color = Color.Red,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .aspectRatio(1f) // квадратне поле
            .padding(16.dp)
    ) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // малюємо коло
        drawCircle(
            color = Color.Gray,
            radius = radius,
            style = Stroke(width = 4f)
        )

        // напрямок стрілки
        val arrowLength = radius * 0.9f
        val arrowEnd = Offset(
            x = center.x + cos(angleRad) * arrowLength,
            y = center.y + sin(angleRad) * arrowLength
        )

        // малюємо стрілку
        drawLine(
            color = color,
            start = center,
            end = arrowEnd,
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )
    }
}
