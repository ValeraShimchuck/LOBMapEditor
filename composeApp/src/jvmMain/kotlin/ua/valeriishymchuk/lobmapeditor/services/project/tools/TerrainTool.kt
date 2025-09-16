package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector2i
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.commands.UpdateTerrainCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

object TerrainTool : BrushTool() {

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
        flushCompoundCommands: Boolean,
    ): Boolean =
        calcBrush(
            Vector2i(
                x.toInt() / GameConstants.TILE_SIZE,
                y.toInt() / GameConstants.TILE_SIZE
            )
        )
            .map { pos ->
                val tileX = pos.x
                val tileY = pos.y
                val oldTerrain = editorService.scenario.value!!.map.terrainMap.get(tileX, tileY) ?: return@map false
                val height = editorService.scenario.value!!.map.terrainHeight.get(tileX, tileY) ?: return@map false
                if (oldTerrain == terrain.value) return@map false
                val command = UpdateTerrainCommand(
                    tileX,
                    tileY,
                    height,
                    oldTerrain,
                    terrain.value,
                    height
                )
                editorService.executeCompoundCommon(command)
                true
            }
            .also {
                if (flushCompoundCommands) editorService.flushCompoundCommon()
            }
            .any { it }


}