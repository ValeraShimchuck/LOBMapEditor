package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class GridTool : Tool() {

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Graph.Grid,
        "Grid",
        "Grid: grid settings"
    )

    val enabled = MutableStateFlow(true)
    val size = MutableStateFlow( Vector2f(16f))
    val offset = MutableStateFlow(Vector2f())
    val color = MutableStateFlow(Vector4f(0f, 0f, 0f, 0.4f))
    val thickness = MutableStateFlow(0.5f)

    override fun flushGeneric(editorService: EditorService<*>) { }

}