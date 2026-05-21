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
    rememberSubject: Any,
    currentPos: Position,
    onUpdate:(Position) -> Unit,
    flush: () -> Unit = {}
) {

    CenteredRow {
        // X
        FloatTextField(
            rememberSubject,
            { currentPos.x },
            { onUpdate(currentPos.copy(x = it)) },
            flush,
            { x -> x.coerceIn(0f, mapDimensions.x()) },
            "X",
            modifier = Modifier.weight(0.5f)
        )

        // Y
        FloatTextField(
            rememberSubject,
            { currentPos.y },
            { onUpdate(currentPos.copy(y = it)) },
            flush,
            { y -> y.coerceIn(0f, mapDimensions.y()) },
            "Y",
            modifier = Modifier.weight(0.5f)
        )
    }

}