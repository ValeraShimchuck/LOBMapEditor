package ua.valeriishymchuk.lobmapeditor.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.joml.Vector2i
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.services.dto.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.dto.tools.PresetTool
import ua.valeriishymchuk.lobmapeditor.services.dto.tools.ToolContext

class ToolService(override val di: DI) : DIAware {

    val editorService: EditorService<GameScenario.Preset> by di.instance()

    var height by mutableStateOf(2)
    var terrain by mutableStateOf(TerrainType.GRASS)

    var currentTool by mutableStateOf<PresetTool>(HeightTool)
    private set

    fun setTool(tool: PresetTool) {
        flushCompoundCommands()
        currentTool = tool
    }


    val ctx: ToolContext get() = ToolContext(height, terrain)

    fun useTool(x: Int, y: Int): Boolean {
        return currentTool.editTile(editorService, x, y, ctx)
    }

    fun useToolManyTimes(tiles: List<Vector2i>, flush: Boolean = true): Boolean {
        val result = tiles.map {
            currentTool.editTile(editorService, it.x, it.y, ctx, false)
        }.firstOrNull { it } == true
        if (flush) currentTool.flush(editorService)
        return result
    }

    fun flushCompoundCommands() {
        currentTool.flush(editorService)
    }

}