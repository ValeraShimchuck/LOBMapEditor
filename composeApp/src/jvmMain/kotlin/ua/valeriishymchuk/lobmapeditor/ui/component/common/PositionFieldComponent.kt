package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.joml.Vector2fc
import ua.valeriishymchuk.lobmapeditor.domain.Position

@Composable
fun PositionFieldComponent(
    mapDimensions: Vector2fc,
    currentPos: Position,
    onUpdate:(Position) -> Unit,
    flush: () -> Unit = {}
) {

    CenteredRow {
        // X
        FloatTextField(
            currentPos,
            { it.x },
            { onUpdate(currentPos.copy(x = it)) },
            flush,
            { x -> x.coerceIn(0f, mapDimensions.x()) },
            "X",
            Modifier.weight(0.5f)
        )

        // Y
        FloatTextField(
            currentPos,
            { it.y },
            { onUpdate(currentPos.copy(y = it)) },
            flush,
            { y -> y.coerceIn(0f, mapDimensions.y()) },
            "Y",
            Modifier.weight(0.5f)
        )
    }

}