package ua.valeriishymchuk.lobmapeditor.services.project.tools

import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.shared.ui.UiInfoProvider
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

abstract class SpecificTool<T : GameScenario<T>, SERVICE: EditorService<T>>: Tool() {

    open fun useToolAt(
        editorService: SERVICE,
        x: Float, y: Float,
        flushCompoundCommands: Boolean = true
    ): Boolean {
        return false
    }

    override fun useToolAtGeneric(
        editorService: EditorService<*>,
        x: Float,
        y: Float,
        flushCompoundCommands: Boolean
    ): Boolean {
        return useToolAt(editorService as SERVICE, x, y, flushCompoundCommands)
    }

    abstract fun flush(editorService: SERVICE)

    override fun flushGeneric(editorService: EditorService<*>) {
        flush(editorService as SERVICE)
    }

}

abstract class Tool: UiInfoProvider<ToolUiInfo> {
    open fun useToolAtGeneric(
        editorService: EditorService<*>,
        x: Float, y: Float,
        flushCompoundCommands: Boolean = true
    ): Boolean {
        return false
    }

    abstract fun flushGeneric(editorService: EditorService<*>)

    open val canBeUsedMultipleTimes = true
}