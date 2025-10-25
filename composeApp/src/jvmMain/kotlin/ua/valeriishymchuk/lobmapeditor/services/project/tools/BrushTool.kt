package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.joml.Vector2i
import kotlin.math.absoluteValue

abstract class BrushTool : PresetTool() {
    enum class BrushShape {
        SQUARE, CIRCLE
    }

    val brushSize = MutableStateFlow(1)
    val brushShape = MutableStateFlow(BrushShape.CIRCLE)

    protected fun calcBrush(pos: Vector2i): List<Vector2i> =
        ((-brushSize.value / 2)..(brushSize.value / 2)).let { range ->
            range.map { y ->
                range.mapNotNull { x ->
                    Vector2i(
                        pos.x + x, pos.y + y
                    ).takeIf {
                        it.distance(pos).absoluteValue <= brushSize.value / 2 || brushShape.value == BrushShape.SQUARE
                    }
                }
            }.flatten()
        }
}