package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.flow.MutableStateFlow
import org.joml.Vector2i
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PresetTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainPickTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool

class ToolService(override val di: DI) : DIAware {

    private val editorService: EditorService<GameScenario.Preset> by instance()
    val tools = listOf(
        HeightTool,
        TerrainTool,
        TerrainPickTool,
    )


    var currentTool = MutableStateFlow<PresetTool>(HeightTool)
        private set

    fun setTool(tool: PresetTool) {
        flushCompoundCommands()
        currentTool.value = tool

    }


    fun useTool(x: Int, y: Int): Boolean {
        return currentTool.value.editTile(editorService, x, y)
    }

    fun useToolManyTimes(tiles: List<Vector2i>, flush: Boolean = true): Boolean {
        val result = tiles.map {
            currentTool.value.editTile(editorService, it.x, it.y, false)
        }.firstOrNull { it } == true
        if (flush) currentTool.value.flush(editorService)
        return result
    }

    fun flushCompoundCommands() {
        currentTool.value.flush(editorService)
    }

}