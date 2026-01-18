package ua.valeriishymchuk.lobmapeditor.services.project.tool

import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceObjectiveTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceUnitTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlayerTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainPickTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.Tool

class PresetToolService(di: DI) : ToolService<GameScenario.Preset>(di) {
    val playerTool = PlayerTool()

    override val tools: List<Tool> = listOf(
        debugTool,
        playerTool,
        HeightTool,
        TerrainTool,
        TerrainPickTool,
        PlaceUnitTool,
        PlaceObjectiveTool,
        gridTool,
        refenceOverlayTool
    )


}