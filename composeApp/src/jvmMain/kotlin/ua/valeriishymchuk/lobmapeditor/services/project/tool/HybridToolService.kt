package ua.valeriishymchuk.lobmapeditor.services.project.tool

import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

class HybridToolService(di: DI) : ToolService<GameScenario.Hybrid>(di) {

    val deploymentZoneTool = DeploymentZoneTool()

    override val tools: List<Tool> = listOf(
        miscTool,
        deploymentZoneTool,
        HeightTool,
        TerrainTool,
        TerrainPickTool,
        PlaceObjectiveTool,
        triggerTool,
        gridTool,
        refenceOverlayTool
    )
}