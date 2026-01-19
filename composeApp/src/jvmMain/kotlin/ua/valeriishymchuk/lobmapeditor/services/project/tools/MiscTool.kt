package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class MiscTool: Tool() {

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Actions.InlayRenameInNoCodeFiles,
        "Misc tools",
        "Misc tools: general category for tools, includes debug tools"
    )

    val debugInfo: MutableStateFlow<RenderContext.DebugInfo> = MutableStateFlow(RenderContext.DebugInfo(
        Vector4f(0f),
        Vector4f(0f),
        false,
        false
    ))

    override fun flushGeneric(editorService: EditorService<*>) {
    }

}