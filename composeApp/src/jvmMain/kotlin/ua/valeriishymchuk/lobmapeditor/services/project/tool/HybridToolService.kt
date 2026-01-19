package ua.valeriishymchuk.lobmapeditor.services.project.tool

import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.tools.DeploymentZoneTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceObjectiveTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainPickTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.Tool

class HybridToolService(di: DI) : ToolService<GameScenario.Hybrid>(di) {

    val deploymentZoneTool = DeploymentZoneTool()

    override val tools: List<Tool> = listOf(
        miscTool,
        deploymentZoneTool,
        HeightTool,
        TerrainTool,
        TerrainPickTool,
        PlaceObjectiveTool,
        gridTool,
        refenceOverlayTool
    )
}