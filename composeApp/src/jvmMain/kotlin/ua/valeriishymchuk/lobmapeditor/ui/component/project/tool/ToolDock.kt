package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.ui.component.DockContainer
import kotlin.getValue

@Composable
fun ToolDock() {
    val toolService by rememberInstance<ToolService>()
    val currentTool by toolService.currentTool.collectAsState()

    DockContainer(
        startComponent = {
            Row(it) {
                Icon(currentTool.uiInfo.icon, null)
                Spacer(Modifier.width(4.dp))
                Text(currentTool.uiInfo.name)
            }
        },
        endComponent = {
            ToolBar(it)
        },
        content = {
            ToolConfig()
        }
    )
}