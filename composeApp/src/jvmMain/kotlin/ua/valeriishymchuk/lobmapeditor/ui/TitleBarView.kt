package ua.valeriishymchuk.lobmapeditor.ui


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.DecoratedWindowScope
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import ua.valeriishymchuk.lobmapeditor.ui.screen.CanvasScreen

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalLayoutApi
@Composable
internal fun DecoratedWindowScope.TitleBarView() {
    val nav = LocalNavigator.currentOrThrow

    TitleBar(Modifier.newFullscreenControls()) {
        Row(Modifier.align(Alignment.Start).padding(horizontal = 8.dp)) {
            Tooltip(tooltip = {
                Text("Settings")
            }) {
                IconButton({}) {
                    Icon(AllIconsKeys.General.Settings, "")
                }
            }
            Tooltip(tooltip = {
                Text("Project settings")
            }) {
                IconButton({}) {
                    Icon(AllIconsKeys.General.ProjectStructure, "")
                }
            }
            Tooltip(tooltip = {
                Text("Canvas test")
            }) {
                IconButton({
                    nav.push(CanvasScreen)
                }) {
                    Icon(AllIconsKeys.Actions.StartDebugger, "")
                }
            }
        }
        Text(title)
    }
}