package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.commands.UpdateTerrainCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
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

    override fun editTile(
        editorService: EditorService<GameScenario.Preset>,
        x: Int,
        y: Int,
        flushCompoundCommands: Boolean
    ): Boolean {
        val oldTerrain = editorService.scenario.map.terrainMap.get(x, y,) ?: return false
        val height = editorService.scenario.map.terrainHeight.get(x, y) ?: return false
        if (oldTerrain == terrain.value) return false
        val command = UpdateTerrainCommand(
            x,
            y,
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