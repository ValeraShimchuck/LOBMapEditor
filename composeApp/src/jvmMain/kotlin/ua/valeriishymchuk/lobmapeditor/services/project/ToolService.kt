package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.flow.MutableStateFlow
import org.joml.Vector2i
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PresetTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.ToolContext

class ToolService(override val di: DI) : DIAware {

    private val editorService: EditorService<GameScenario.Preset> by instance()

    var height = MutableStateFlow(2)
    var terrain = MutableStateFlow(TerrainType.GRASS)

    var currentTool = MutableStateFlow<PresetTool>(HeightTool)
    private set

    fun setTool(tool: PresetTool) {
        flushCompoundCommands()
        currentTool.value = tool

    }


    val ctx: ToolContext get() = ToolContext(height.value, terrain.value)

    fun useTool(x: Int, y: Int): Boolean {
        return currentTool.value.editTile(editorService, x, y, ctx)
    }

    fun useToolManyTimes(tiles: List<Vector2i>, flush: Boolean = true): Boolean {
        val result = tiles.map {
            currentTool.value.editTile(editorService, it.x, it.y, ctx, false)
        }.firstOrNull { it } == true
        if (flush) currentTool.value.flush(editorService)
        return result
    }

    fun flushCompoundCommands() {
        currentTool.value.flush(editorService)
    }

}