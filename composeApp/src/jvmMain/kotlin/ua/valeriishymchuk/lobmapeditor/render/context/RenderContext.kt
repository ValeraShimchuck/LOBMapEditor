package ua.valeriishymchuk.lobmapeditor.render.context

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage

data class RenderContext(
    val glCtx: CurrentGL,
    val windowDimensions: Vector2i,
    val textureStorage: TextureStorage,
    val viewMatrix: Matrix4f,
    val projectionMatrix: Matrix4f,
    val scenario: GameScenario.Preset,
    val selectedUnits: List<GameUnit>,
    val selectedObjectives: List<Objective>,
    val selection: SelectionContext,
    val gridContext: GridContext,
    val overlayReferenceContext: OverlayReferenceContext,
    val debugInfo: DebugInfo
) {
    fun getMvp(model: Matrix4f): Matrix4f {
        return Matrix4f(projectionMatrix)
            .mul(viewMatrix)
            .mul(model)
    }

    data class SelectionContext(
        val enabled: Boolean,
        val selectionStart: Vector2f,
        val selectionEnd: Vector2f
    )

    data class GridContext(
        val offset: Vector2f,
        val gridSize: Vector2f,
        val gridThickness: Float,
        val color: Vector4f
    )

    data class OverlayReferenceContext(
        val colorTint: Vector4f,
        val positionMatrix: Matrix4f
    )

    data class DebugInfo(
        val firstHeightColor: Vector4f,
        val secondHeightColor: Vector4f,
        val measurePerformanceCPU: Boolean,
        val measurePerformanceGPU: Boolean
    )


}
