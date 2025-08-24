package ua.valeriishymchuk.lobmapeditor.services.dto.tools

import ua.valeriishymchuk.lobmapeditor.services.EditorService
import ua.valeriishymchuk.lobmapeditor.commands.UpdateTerrainCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

object TerrainTool : PresetTool() {

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
        editorService.flushCompoundCommon()
    }

    override fun editTile(
        editorService: EditorService<GameScenario.Preset>,
        x: Int,
        y: Int,
        ctx: ToolContext,
        flushCompoundCommands: Boolean
    ): Boolean {
        val oldTerrain = editorService.scenario.map.terrainMap.get(x, y,) ?: return false
        val height = editorService.scenario.map.terrainHeight.get(x, y) ?: return false
        if (oldTerrain == ctx.terrain) return false
        val command = UpdateTerrainCommand(
            x,
            y,
            height,
            oldTerrain,
            ctx.terrain,
            height
        )
        if (flushCompoundCommands) editorService.executeCommon(command)
        else editorService.executeCompoundCommon(command)
        return true
    }


}