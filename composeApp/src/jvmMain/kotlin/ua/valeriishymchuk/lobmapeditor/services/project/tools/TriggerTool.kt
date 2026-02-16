package ua.valeriishymchuk.lobmapeditor.services.project.tools

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameTrigger
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class TriggerTool: Tool()  {

    val currentTrigger: MutableStateFlow<Reference<Int, GameTrigger>?> = MutableStateFlow(null)


    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Debugger.Db_exception_breakpoint,
        "Triggers",
        "Triggers: modify triggers of your scenario"
    )

    override fun flushGeneric(editorService: EditorService<*>) {

    }
}