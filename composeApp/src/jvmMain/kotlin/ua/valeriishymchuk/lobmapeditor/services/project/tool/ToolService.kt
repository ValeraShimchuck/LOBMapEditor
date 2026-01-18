package ua.valeriishymchuk.lobmapeditor.services.project.tool

import kotlinx.coroutines.flow.MutableStateFlow
import org.joml.Vector2f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tools.DebugTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.GridTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.ReferenceOverlayTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.Tool

abstract class ToolService<S: GameScenario<S>>(override val di: DI) : DIAware {

    protected val editorService: EditorService<*> by instance()


    val gridTool = GridTool()
    val refenceOverlayTool = ReferenceOverlayTool()
    val debugTool = DebugTool()

    abstract val tools: List<Tool>


    var currentTool = MutableStateFlow<Tool>(HeightTool)
        private set

    fun setTool(tool: Tool) {
        flushCompoundCommands()
        currentTool.value = tool

    }


    fun useTool(x: Float, y: Float): Boolean {
        return currentTool.value.useToolAtGeneric(editorService, x, y)
    }

    fun useToolManyTimes(tiles: List<Vector2f>, flush: Boolean = true): Boolean {
        if (!currentTool.value.canBeUsedMultipleTimes) return false
        val result = tiles.map {
            currentTool.value.useToolAtGeneric(editorService, it.x, it.y, false)
        }.firstOrNull { it } == true
        if (flush) currentTool.value.flushGeneric(editorService)
        return result
    }

    fun flushCompoundCommands() {
        currentTool.value.flushGeneric(editorService)
    }

}