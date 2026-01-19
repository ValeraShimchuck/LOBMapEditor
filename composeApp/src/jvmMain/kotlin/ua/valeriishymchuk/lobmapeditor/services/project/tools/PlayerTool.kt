package ua.valeriishymchuk.lobmapeditor.services.project.tools

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class PlayerTool: PresetTool()  {

    val currentPlayer = MutableStateFlow(Reference<Int, Player>(0))

    override fun flush(editorService: PresetEditorService) {
        editorService.flushCompound()
    }

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.CodeWithMe.Users,
        "Configure players",
        "Configure players: delete/add/change players"
    )
}