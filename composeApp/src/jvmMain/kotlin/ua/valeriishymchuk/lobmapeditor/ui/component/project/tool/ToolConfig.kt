package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.runtime.*
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.*

@Composable
fun ToolConfig() {
    val toolService by rememberInstance<ToolService<*>>()
    val currentTool by toolService.currentTool.collectAsState()


    val content: @Composable (() -> Unit)? = when (currentTool) {
        is TerrainTool -> {
            { TerrainToolConfig() }
        }

        is HeightTool -> {
            { HeightToolConfig() }
        }

        is PlaceUnitTool -> {
            { PlaceUnitToolConfig() }
        }

        is PlaceObjectiveTool -> {
            { PlaceObjectiveToolConfig() }
        }

        is GridTool -> {
            { GridToolConfig() }
        }

        is ReferenceOverlayTool -> {
            { ReferenceOverlayToolConfig() }
        }

        is PlayerTool -> {
            { PlayerToolConfig() }
        }

        is MiscTool -> {
            { MiscToolConfig() }
        }

        is DeploymentZoneTool -> {
            { DeploymentZoneToolConfig() }
        }

        is TriggerTool -> {
            { TriggerToolConfig() }
        }

        else -> null
    }

    if (content != null) {
        content()
    }
}