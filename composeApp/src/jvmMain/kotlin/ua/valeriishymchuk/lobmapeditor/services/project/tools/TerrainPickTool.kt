package ua.valeriishymchuk.lobmapeditor.services.project.tools

import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

object TerrainPickTool : Tool() {
    override fun flushGeneric(editorService: EditorService<*>) {
        editorService.flushCompoundCommon()
    }

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.General.AddJdk,
        "Terrain Pick",
        "Terrain Pick: pick terrain from map"
    )

    override fun useToolAtGeneric(
        editorService: EditorService<*>,
        x: Float,
        y: Float,
        flushCompoundCommands: Boolean,
    ): Boolean {

        val toolService by editorService.di.instance<ToolService<*>>()

        editorService.scenario.value!!.map.terrainMap.get(x.toInt() / GameConstants.TILE_SIZE, y.toInt() / GameConstants.TILE_SIZE)?.let {
            TerrainTool.terrain.value = it
            toolService.setTool(TerrainTool)
        }

        return false
    }
}