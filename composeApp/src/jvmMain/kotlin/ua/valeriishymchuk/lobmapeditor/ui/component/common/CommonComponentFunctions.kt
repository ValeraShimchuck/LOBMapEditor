package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider

@Composable
fun VerticalSpacer(distance: Int) {
    Spacer(Modifier.height(distance.dp))
}

@Composable
fun HorizontalSpacer(distance: Int) {
    Spacer(Modifier.width(distance.dp))
}

@Composable
fun DefaultVSpacer() {
    VerticalSpacer(7)
}

@Composable
fun DefaultHSpacer() {
    HorizontalSpacer(7)
}

@Composable
fun LongVSpacer() {
    VerticalSpacer(10)
}

@Composable
fun LongHSpacer() {
    HorizontalSpacer(10)
}

@Composable
fun SimpleDivider() {
    DefaultVSpacer()

    CenteredRow {
        Divider(Orientation.Horizontal,
            modifier =  Modifier.fillMaxWidth(),
            color = Color(0.443f, 0.443f, 0.443f, 1.0f)
        )
    }


    DefaultVSpacer()
}
