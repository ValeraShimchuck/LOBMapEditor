package ua.valeriishymchuk.lobmapeditor.services.project.tool

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class TriggerTool: Tool()  {

    @Deprecated("remember { mutableStateOf } is used instead ")
    val currentTrigger: MutableStateFlow<Reference<Int, GameTrigger>?> = MutableStateFlow(null)


    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Debugger.Db_exception_breakpoint,
        "Triggers",
        "Triggers: modify triggers of your scenario"
    )

    override fun flushGeneric(editorService: EditorService<*>) {

    }
}