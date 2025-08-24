package ua.valeriishymchuk.lobmapeditor.services.project.tools

import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.commands.UpdateTerrainCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import java.util.LinkedList
import kotlin.math.abs

object HeightTool : PresetTool() {

    override fun editTile(
        editorService: EditorService<GameScenario.Preset>,
        x: Int,
        y: Int,
        ctx: ToolContext,
        flushCompoundCommands: Boolean
    ): Boolean {
        return trySetTileHeight(x, y, ctx.height, editorService, flushCompoundCommands)
    }

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
        editorService.flushCompoundCommon()
    }

    private fun set(
        tileX: Int,
        tileY: Int,
        height: Int,
        editorService: EditorService<GameScenario.Preset>
    ): Boolean {
        val terrain = editorService.scenario.map.terrainMap.get(tileX, tileY) ?: return false
        val oldValue = editorService.scenario.map.terrainHeight.get(tileX, tileY) ?: return false
        if (oldValue == height) return false
        editorService.executeCompoundCommon(
            UpdateTerrainCommand(
                tileX,
                tileY,
                oldValue,
                terrain,
                terrain,
                height
            )
        )
        return true
    }

    private fun trySetTileHeight(
        tileX: Int,
        tileY: Int,
        height: Int,
        editorService: EditorService<GameScenario.Preset>,
        flushCompoundCommands: Boolean
    ): Boolean {
        val heightMap = editorService.scenario.map.terrainHeight
        if (!set(tileX, tileY, height, editorService)) return false

        val queue = LinkedList<Pair<Int, Int>>()
        queue.add(tileX to tileY)

        while (queue.isNotEmpty()) {
            val (x, y) = queue.poll()
            val currentHeight = heightMap.get(x, y) ?: continue

            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    val nx = x + dx
                    val ny = y + dy
                    // Protect original tile from being modified
                    if (nx == tileX && ny == tileY) continue

                    val neighborHeight = heightMap.get(nx, ny) ?: continue
                    val diff = currentHeight - neighborHeight
                    if (abs(diff) <= 1) continue

                    val newHeight = if (diff > 0) currentHeight - 1 else currentHeight + 1
                    if (set(nx, ny, newHeight, editorService)) {
                        queue.add(nx to ny)
                    }
                }
            }
        }
        if (flushCompoundCommands) editorService.flushCompoundCommon()
        return true
    }

}