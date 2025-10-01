package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.flow.MutableStateFlow
import org.joml.Vector2f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.tools.GridTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceObjectiveTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceUnitTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlayerTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PresetTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.ReferenceOverlayTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainPickTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool

class ToolService(override val di: DI) : DIAware {

    private val editorService: EditorService<GameScenario.Preset> by instance()


    val gridTool = GridTool()
    val refenceOverlayTool = ReferenceOverlayTool()
    val playerTool = PlayerTool()

    val tools = listOf(
        playerTool,
        HeightTool,
        TerrainTool,
        TerrainPickTool,
        PlaceUnitTool,
        PlaceObjectiveTool,
        gridTool,
        refenceOverlayTool
    )


    var currentTool = MutableStateFlow<PresetTool>(HeightTool)
        private set

    fun setTool(tool: PresetTool) {
        flushCompoundCommands()
        currentTool.value = tool

    }


    fun useTool(x: Float, y: Float): Boolean {
        return currentTool.value.useToolAt(editorService, x, y)
    }

    fun useToolManyTimes(tiles: List<Vector2f>, flush: Boolean = true): Boolean {
        if (!currentTool.value.canBeUsedMultipleTimes) return false
        val result = tiles.map {
            currentTool.value.useToolAt(editorService, it.x, it.y, false)
        }.firstOrNull { it } == true
        if (flush) currentTool.value.flush(editorService)
        return result
    }

    fun flushCompoundCommands() {
        currentTool.value.flush(editorService)
    }

}