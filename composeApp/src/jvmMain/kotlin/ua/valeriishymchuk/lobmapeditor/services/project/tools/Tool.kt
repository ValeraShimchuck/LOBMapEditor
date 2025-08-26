package ua.valeriishymchuk.lobmapeditor.services.project.tools

import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.shared.ui.UiInfoProvider
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolUiInfo

abstract class Tool<T : GameScenario<T>>: UiInfoProvider<ToolUiInfo> {

    open fun useToolAt(
        editorService: EditorService<T>,
        x: Float, y: Float,
        flushCompoundCommands: Boolean = true
    ): Boolean {
        return false
    }

    abstract fun flush(editorService: EditorService<T>)

    open val canBeUsedMultipleTimes = true


}