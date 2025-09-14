package ua.valeriishymchuk.lobmapeditor.render

import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit.Companion.UNIT_DIMENSIONS
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.stage.BackgroundStage
import ua.valeriishymchuk.lobmapeditor.render.stage.BlobTileStage
import ua.valeriishymchuk.lobmapeditor.render.stage.ColorClosestPointStage
import ua.valeriishymchuk.lobmapeditor.render.stage.ColorStage
import ua.valeriishymchuk.lobmapeditor.render.stage.OverlayTileStage
import ua.valeriishymchuk.lobmapeditor.render.stage.RenderStage
import ua.valeriishymchuk.lobmapeditor.render.stage.SelectionStage
import ua.valeriishymchuk.lobmapeditor.render.stage.SpriteStage
import ua.valeriishymchuk.lobmapeditor.render.stage.TerrainMapStage
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.event.*
import kotlin.math.abs

class EditorRenderer(override val di: DI) : GLEventListener, DIAware {

    private val editorService: EditorService<GameScenario.Preset> by di.instance()
    private val textureStorage: TextureStorage = TextureStorage()

    private val frame1BorderOffset = 21f
    private val frame1Color = Vector4f(33f, 19f, 10f, 255f).div(255f)
    private val frame1Vertices = floatArrayOf(
        -frame1BorderOffset,
        editorService.scenario.map.heightPixels + frame1BorderOffset,
        editorService.scenario.map.widthPixels + frame1BorderOffset,
        -frame1BorderOffset,
        -frame1BorderOffset,
        -frame1BorderOffset,

        -frame1BorderOffset,
        editorService.scenario.map.heightPixels + frame1BorderOffset,
        editorService.scenario.map.widthPixels + frame1BorderOffset,
        editorService.scenario.map.heightPixels + frame1BorderOffset,
        editorService.scenario.map.widthPixels + frame1BorderOffset,
        -frame1BorderOffset,
    )


    private val frame2BorderOffset = 16f
    private val frame2Color = Vector4f(162f, 157f, 131f, 255f).div(255f)
    private val frame2Vertices = floatArrayOf(
        -frame2BorderOffset,
        editorService.scenario.map.heightPixels + frame2BorderOffset,
        editorService.scenario.map.widthPixels + frame2BorderOffset,
        -frame2BorderOffset,
        -frame2BorderOffset,
        -frame2BorderOffset,

        -frame2BorderOffset,
        editorService.scenario.map.heightPixels + frame2BorderOffset,
        editorService.scenario.map.widthPixels + frame2BorderOffset,
        editorService.scenario.map.heightPixels + frame2BorderOffset,
        editorService.scenario.map.widthPixels + frame2BorderOffset,
        -frame2BorderOffset,
    )


    private val tileMapVertices = floatArrayOf(
        0f, editorService.scenario.map.heightPixels.toFloat(),
        editorService.scenario.map.widthPixels.toFloat(), 0f,
        0f, 0f,

        0f, editorService.scenario.map.heightPixels.toFloat(),
        editorService.scenario.map.widthPixels.toFloat(), editorService.scenario.map.heightPixels.toFloat(),
        editorService.scenario.map.widthPixels.toFloat(), 0f,
    )


    // Order matters
    private lateinit var renderStages: List<RenderStage>

    override fun init(drawable: GLAutoDrawable) {
        val ctx = drawable.gl.gL3

        ctx.glEnable(GL_BLEND)
        ctx.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
//        val ctx = TraceGL3(drawable.gl.gL3, System.out)
//        drawable.gl = ctx

        textureStorage.loadTextures(ctx)

        renderStages = listOf(
            BackgroundStage(ctx),
            ColorStage(ctx, frame1Vertices, frame1Color),
            ColorStage(ctx, frame2Vertices, frame2Color),
            TerrainMapStage(ctx, tileMapVertices),
            BlobTileStage(ctx, tileMapVertices),
            OverlayTileStage(ctx, tileMapVertices),
            ColorClosestPointStage(ctx, tileMapVertices),
            SpriteStage(ctx),
            SelectionStage(ctx)

        )

        editorService.projectionMatrix.setOrtho(
            0f,
            drawable.surfaceWidth.toFloat(),
            drawable.surfaceHeight.toFloat(),
            0f,
            -1f,
            1f
        )
        editorService.width = drawable.surfaceWidth
        editorService.height = drawable.surfaceHeight

        println("GL initialized")

    }


    override fun display(drawable: GLAutoDrawable) {
        val ctx = drawable.gl.gL3
        ctx.glClearColor(0.5f, 0f, 0.5f, 1f)
        ctx.glClear(GL_COLOR_BUFFER_BIT)

        val renderCtx = RenderContext(
            ctx,
            Vector2i(editorService.width, editorService.height),
            textureStorage,
            editorService.viewMatrix,
            editorService.projectionMatrix,
            editorService.scenario,
            editorService.selectedUnits
                .mapNotNull { reference -> reference.getValueOrNull(editorService.scenario.units::getOrNull) },
            editorService.selectedObjectives?.let {
                listOf(it).mapNotNull { reference ->
                    reference.getValueOrNull(editorService.scenario.objectives::getOrNull)
                }
            } ?: emptyList(),
            RenderContext.SelectionContext(
                editorService.selectionEnabled,
                editorService.selectionStart,
                editorService.selectionEnd
            )
        )

        renderStages.forEach { stage ->
            stage.draw(renderCtx)
        }
        ctx.glBindVertexArray(0)
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        val ctx = drawable.gl.gL3
        ctx.glViewport(0, 0, width, height)

        editorService.projectionMatrix.setOrtho(
            0f,
            width.toFloat(),
            height.toFloat(),
            0f,
            -1f,
            1f
        )

        editorService.width = width
        editorService.height = height

    }

    override fun dispose(drawable: GLAutoDrawable) {}

}