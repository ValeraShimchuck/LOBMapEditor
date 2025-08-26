package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.commands.UpdateTerrainCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolUiInfo

object TerrainTool : PresetTool() {

    val terrain = MutableStateFlow(TerrainType.GRASS)

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Actions.Edit,
        "Terrain",
        "Terrain: change type of terrain"
    )

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
        editorService.flushCompoundCommon()
    }

    override fun useToolAt(
        editorService: EditorService<GameScenario.Preset>,
        x: Float,
        y: Float,
        flushCompoundCommands: Boolean
    ): Boolean {
        val tileX = x.toInt() / GameConstants.TILE_SIZE
        val tileY = y.toInt() / GameConstants.TILE_SIZE
        val oldTerrain = editorService.scenario.map.terrainMap.get(tileX, tileY) ?: return false
        val height = editorService.scenario.map.terrainHeight.get(tileX, tileY) ?: return false
        if (oldTerrain == terrain.value) return false
        val command = UpdateTerrainCommand(
            tileX,
            tileY,
            height,
            oldTerrain,
            terrain.value,
            height
        )
        if (flushCompoundCommands) editorService.executeCommon(command)
        else editorService.executeCompoundCommon(command)
        return true
    }


}