package ua.valeriishymchuk.lobmapeditor.ui.component.project

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService

data class ToolUiInfo(
    val icon: IconKey,
    val name: String,
    val tooltip: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolBar(
    modifier: Modifier = Modifier,
) {
    val toolService by rememberInstance<ToolService>()
    val currentTool by toolService.currentTool.collectAsState()

    Column(modifier) {
        GroupHeader(
            "Tools",
            startComponent = { Icon(AllIconsKeys.Actions.Edit, null) }
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),

            ) {
            toolService.tools.forEach { tool ->
                Tooltip(
                    { Text(tool.uiInfo.tooltip) }
                ) {
                    SelectableIconButton(
                        selected = currentTool == tool,
                        onClick = { toolService.setTool(tool) }
                    ) {
                        Icon(tool.uiInfo.icon, tool.uiInfo.tooltip)
                    }
                }
            }
        }
    }

}