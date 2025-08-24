package ua.valeriishymchuk.lobmapeditor.services.dto.tools

import ua.valeriishymchuk.lobmapeditor.services.EditorService
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

abstract class Tool<T : GameScenario<T>> {

    open fun editTile(
        editorService: EditorService<T>,
        x: Int, y: Int,
        ctx: ToolContext,
        flushCompoundCommands: Boolean = true
    ): Boolean {
        return false
    }

    abstract fun flush(editorService: EditorService<T>)




}