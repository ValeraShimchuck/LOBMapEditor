package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class ReferenceOverlayTool : Tool() {

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.FileTypes.Image,
        "Reference image",
        "Refence image: set up an overlay setting for your image"
    )

    val enabled = MutableStateFlow(true)
    val hideSprites = MutableStateFlow(false)
    val hideRange = MutableStateFlow(false)
    val scale = MutableStateFlow( Vector2f(1f, 1f)) // just scale
    val offset = MutableStateFlow(Vector2f(0f)) // from -1 to 1
    val rotation = MutableStateFlow(0f) // radians
    val transparency = MutableStateFlow(0.4f) // value from 0 to 1

    override fun flushGeneric(editorService: EditorService<*>) {
    }

}