package ua.valeriishymchuk.lobmapeditor.render.renderer

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.render.context.HybridRenderContext
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.stage.BackgroundStage
import ua.valeriishymchuk.lobmapeditor.render.stage.BlobTileStage
import ua.valeriishymchuk.lobmapeditor.render.stage.ColorStage
import ua.valeriishymchuk.lobmapeditor.render.stage.DeploymentZoneStage
import ua.valeriishymchuk.lobmapeditor.render.stage.GridStage
import ua.valeriishymchuk.lobmapeditor.render.stage.OverlayTileStage
import ua.valeriishymchuk.lobmapeditor.render.stage.ReferenceOverlayStage
import ua.valeriishymchuk.lobmapeditor.render.stage.RenderStage
import ua.valeriishymchuk.lobmapeditor.render.stage.SelectionStage
import ua.valeriishymchuk.lobmapeditor.render.stage.SpriteStage
import ua.valeriishymchuk.lobmapeditor.render.stage.TerrainMapStage
import ua.valeriishymchuk.lobmapeditor.services.project.editor.HybridEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.HybridToolService

class HybridEditorRenderer(di: DI) : EditorRenderer<GameScenario.Hybrid, HybridRenderContext>(di) {

    val hybridEditorService: HybridEditorService by lazy {
        editorService as HybridEditorService
    }

    private val hybridToolService: HybridToolService by lazy {
        toolService as HybridToolService
    }

    override fun createRenderStages(ctx: CurrentGL): List<RenderStage> {
        return listOf(
            BackgroundStage(ctx),
            ColorStage(ctx, frame1Vertices, frame1Color),
            ColorStage(ctx, frame2Vertices, frame2Color),
            TerrainMapStage(ctx, tileMapVertices),
            BlobTileStage(ctx, tileMapVertices),
            OverlayTileStage(ctx, tileMapVertices),
//            ColorClosestPointStage(ctx, tileMapVertices),
            ReferenceOverlayStage(ctx, tileMapVertices),
            GridStage(ctx, tileMapVertices),
//            RangeStage(ctx),
            DeploymentZoneStage(ctx),
            SpriteStage(ctx),
            SelectionStage(ctx),
//            UnitBarsStage(ctx)
        )
    }

    override fun prepareContext(gl: CurrentGL): HybridRenderContext {
        return HybridRenderContext(
            gl,
            Vector2i(editorService.width, editorService.height),
            textureStorage,
            editorService.viewMatrix,
            editorService.projectionMatrix,
            hybridEditorService.scenario.value!!,
            editorService.selectedObjectives.value?.let {
                listOf(it).mapNotNull { reference ->
                    reference.getValueOrNull(editorService.scenario.value!!.objectives::getOrNull)
                }
            } ?: emptyList(),
            RenderContext.SelectionContext(
                editorService.selectionEnabled,
                editorService.selectionStart,
                editorService.selectionEnd
            ),
            RenderContext.GridContext(
                toolService.gridTool.offset.value,
                toolService.gridTool.size.value,
                toolService.gridTool.thickness.value,
                toolService.gridTool.color.value
            ),
            RenderContext.OverlayReferenceContext(
                Vector4f(1f, 1f, 1f, toolService.refenceOverlayTool.transparency.value),
                Matrix4f().apply {

                    val center = Vector2f(0.5f, 0.5f)

                    // Translate to origin, rotate, then translate back to center
                    translate(center.x, center.y, 0f)  // Move center to origin
                    scale(Vector3f(Vector2f(1f).div(Vector2f(toolService.refenceOverlayTool.scale.value)), 1f))
                    val normalizedRotation = (Math.PI.toFloat() * 2) - toolService.refenceOverlayTool.rotation.value
                    rotateZ(normalizedRotation)  // Perform rotation
                    translate(-center.x, -center.y, 0f)  // Move back to original position

                    // Apply other transformations (scale and offset)

                    translate(Vector3f(toolService.refenceOverlayTool.offset.value.mul(-1f, Vector2f()), 0f))
                }
            ),
            toolService.miscTool.debugInfo.value,
            hybridToolService
        )
    }
}