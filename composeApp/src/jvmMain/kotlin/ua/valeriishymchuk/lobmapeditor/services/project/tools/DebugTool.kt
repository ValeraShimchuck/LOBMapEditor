package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class DebugTool: PresetTool() {

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Toolwindows.ToolWindowDebugger,
        "Debug tools",
        "Debug tools: tools for map editor debugging and developing"
    )

    val debugInfo: MutableStateFlow<RenderContext.DebugInfo> = MutableStateFlow(RenderContext.DebugInfo(
        Vector4f(0f),
        Vector4f(0f),
        false,
        false
    ))

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
    }

}