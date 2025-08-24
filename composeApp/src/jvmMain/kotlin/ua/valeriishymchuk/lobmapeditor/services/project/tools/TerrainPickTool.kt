package ua.valeriishymchuk.lobmapeditor.services.project.tools

import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.direct
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolUiInfo

object TerrainPickTool : PresetTool() {
    override fun flush(editorService: EditorService<GameScenario.Preset>) {
        editorService.flushCompoundCommon()
    }

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.General.AddJdk,
        "Terrain Pick",
        "Terrain Pick: pick terrain from map"
    )

    override fun editTile(
        editorService: EditorService<GameScenario.Preset>,
        x: Int,
        y: Int,
        flushCompoundCommands: Boolean,
    ): Boolean {

        val toolService by editorService.di.instance<ToolService>()

        editorService.scenario.map.terrainMap.get(x, y)?.let {
            TerrainTool.terrain.value = it
            toolService.setTool(TerrainTool)
        }

        return false
    }
}