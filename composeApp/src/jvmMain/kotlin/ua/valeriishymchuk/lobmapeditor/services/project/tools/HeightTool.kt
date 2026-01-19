package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Vector2i
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.commands.UpdateTerrainCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo
import java.util.LinkedList
import kotlin.math.abs

object HeightTool : BrushTool() {

    val height = MutableStateFlow(1)

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Actions.Commit,
        "Height",
        "Height: change height of terrain"
    )

    override fun useToolAtGeneric(
        editorService: EditorService<*>,
        x: Float,
        y: Float,
        flushCompoundCommands: Boolean,
    ): Boolean = calcBrush(
        Vector2i(
            x.toInt() / GameConstants.TILE_SIZE,
            y.toInt() / GameConstants.TILE_SIZE
        )
    )
        .map { pos ->
            trySetTileHeight(
                pos.x,
                pos.y,
                height.value,
                editorService,
                false
            )
        }
        .also {
            if (flushCompoundCommands) editorService.flushCompoundCommon()
        }
        .any { it }

    override fun flushGeneric(editorService: EditorService<*>) {
        editorService.flushCompoundCommon()
    }

    private fun set(
        tileX: Int,
        tileY: Int,
        height: Int,
        editorService: EditorService<*>,
    ): Boolean {
        val terrain = editorService.scenario.value!!.map.terrainMap.get(tileX, tileY) ?: return false
        val oldValue = editorService.scenario.value!!.map.terrainHeight.get(tileX, tileY) ?: return false
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
        editorService: EditorService<*>,
        flushCompoundCommands: Boolean,
    ): Boolean {
        val heightMap = editorService.scenario.value!!.map.terrainHeight
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