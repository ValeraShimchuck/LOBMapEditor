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

class PresetRenderContext(
    glCtx: CurrentGL,
    windowDimensions: Vector2i,
    textureStorage: TextureStorage,
    viewMatrix: Matrix4f,
    projectionMatrix: Matrix4f,
    scenario: GameScenario.Preset,
    val selectedUnits: List<GameUnit>,
    selectedObjectives: List<Objective>,
    selection: SelectionContext,
    gridContext: GridContext,
    overlayReferenceContext: OverlayReferenceContext,
    debugInfo: DebugInfo
) : RenderContext<GameScenario.Preset>(
    glCtx,
    windowDimensions,
    textureStorage,
    viewMatrix,
    projectionMatrix,
    scenario,
    selectedObjectives,
    selection,
    gridContext,
    overlayReferenceContext,
    debugInfo
)

class HybridRenderContext(
    glCtx: CurrentGL,
    windowDimensions: Vector2i,
    textureStorage: TextureStorage,
    viewMatrix: Matrix4f,
    projectionMatrix: Matrix4f,
    scenario: GameScenario.Hybrid,
    selectedObjectives: List<Objective>,
    selection: SelectionContext,
    gridContext: GridContext,
    overlayReferenceContext: OverlayReferenceContext,
    debugInfo: DebugInfo
) : RenderContext<GameScenario.Hybrid>(
    glCtx,
    windowDimensions,
    textureStorage,
    viewMatrix,
    projectionMatrix,
    scenario,
    selectedObjectives,
    selection,
    gridContext,
    overlayReferenceContext,
    debugInfo
)

abstract class RenderContext<T: GameScenario<T>>(
    val glCtx: CurrentGL,
    val windowDimensions: Vector2i,
    val textureStorage: TextureStorage,
    val viewMatrix: Matrix4f,
    val projectionMatrix: Matrix4f,
    val scenario: T,
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
