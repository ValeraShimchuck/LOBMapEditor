package ua.valeriishymchuk.lobmapeditor.services.project.tool

import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

class PresetToolService(di: DI) : ToolService<GameScenario.Preset>(di) {
    val playerTool = PlayerTool()

    override val tools: List<Tool> = listOf(
        miscTool,
        playerTool,
        HeightTool,
        TerrainTool,
        TerrainPickTool,
        PlaceUnitTool,
        PlaceObjectiveTool,
        triggerTool,
        gridTool,
        refenceOverlayTool
    )


}