package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import ua.valeriishymchuk.lobmapeditor.ui.JoglCanvas

object CanvasScreen: Screen {
    @Composable
    override fun Content() {
        Column(
            Modifier.fillMaxSize()
        ) {
            JoglCanvas {  }
        }
    }
}