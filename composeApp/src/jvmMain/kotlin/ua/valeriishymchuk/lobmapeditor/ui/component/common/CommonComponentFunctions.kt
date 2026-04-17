package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VerticalSpacer(distance: Int) {
    Spacer(Modifier.height(distance.dp))
}

@Composable
fun DefaultVSpacer() {
    VerticalSpacer(7)
}

@Composable
fun LongVSpacer() {
    VerticalSpacer(10)
}

