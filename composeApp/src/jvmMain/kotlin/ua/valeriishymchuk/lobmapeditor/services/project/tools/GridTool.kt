package ua.valeriishymchuk.lobmapeditor.services.project.tools

import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class GridTool : PresetTool() {

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Graph.Grid,
        "Grid",
        "Grid: grid settings"
    )

    var enabled: Boolean = true
    var size: Vector2f = Vector2f(16f)
    var offset: Vector2f = Vector2f()
    var color: Vector4f = Vector4f(0f, 0f, 0f, 0.4f)
    var thickness: Float = 0.5f

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
    }

}