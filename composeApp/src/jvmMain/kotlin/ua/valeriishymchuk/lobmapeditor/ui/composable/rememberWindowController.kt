package ua.valeriishymchuk.lobmapeditor.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle

class WindowScope internal constructor(
    private val controller: WindowController
) {
    fun close() = controller.close()
    fun open() = controller.open()
    val isOpen: Boolean get() = controller.isOpen
}

class WindowController(
    private val title: String,
    private val state: WindowState,
    private val content: @Composable WindowScope.() -> Unit
) {
    var isOpen by mutableStateOf(false)
        private set

    fun open() { isOpen = true }
    fun close() { isOpen = false }

    @Composable
    fun Render() {
        if (isOpen) {
            DecoratedWindow(
                onCloseRequest = { close() },
                title = title,
                style = DecoratedWindowStyle.dark(),
                state = state,
                content = {
                    TitleBar(Modifier.newFullscreenControls()) {
                        Text(title, modifier = Modifier.align(Alignment.Start).padding(8.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(JewelTheme.globalColors.panelBackground)
                            .fillMaxSize()
                    ) {
                        val scope = remember { WindowScope(this@WindowController) }
                        scope.content()
                    }
                }
            )
        }
    }
}

@Composable
fun rememberWindowController(
    title: String,
    state: WindowState = rememberWindowState(),
    content: @Composable WindowScope.() -> Unit,
): WindowController {
    return remember { WindowController(title, state, content) }
}