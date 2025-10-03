package ua.valeriishymchuk.lobmapeditor.render

import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GL4
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLDebugMessage
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
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.stage.*
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import java.awt.event.*
import java.lang.Math
import java.lang.System


class EditorRenderer(override val di: DI) : GLEventListener, DIAware {

    private val editorService: EditorService<GameScenario.Preset> by di.instance()
    private val toolService: ToolService by di.instance()
    private val textureStorage: TextureStorage by di.instance()
    private val projectRef: ProjectRef by di.instance()

    private val frame1BorderOffset = 21f
    private val frame1Color = Vector4f(33f, 19f, 10f, 255f).div(255f)
    private val frame1Vertices = floatArrayOf(
        -frame1BorderOffset,
        editorService.scenario.value!!.map.heightPixels + frame1BorderOffset,
        editorService.scenario.value!!.map.widthPixels + frame1BorderOffset,
        -frame1BorderOffset,
        -frame1BorderOffset,
        -frame1BorderOffset,

        -frame1BorderOffset,
        editorService.scenario.value!!.map.heightPixels + frame1BorderOffset,
        editorService.scenario.value!!.map.widthPixels + frame1BorderOffset,
        editorService.scenario.value!!.map.heightPixels + frame1BorderOffset,
        editorService.scenario.value!!.map.widthPixels + frame1BorderOffset,
        -frame1BorderOffset,
    )


    private val frame2BorderOffset = 16f
    private val frame2Color = Vector4f(162f, 157f, 131f, 255f).div(255f)
    private val frame2Vertices = floatArrayOf(
        -frame2BorderOffset,
        editorService.scenario.value!!.map.heightPixels + frame2BorderOffset,
        editorService.scenario.value!!.map.widthPixels + frame2BorderOffset,
        -frame2BorderOffset,
        -frame2BorderOffset,
        -frame2BorderOffset,

        -frame2BorderOffset,
        editorService.scenario.value!!.map.heightPixels + frame2BorderOffset,
        editorService.scenario.value!!.map.widthPixels + frame2BorderOffset,
        editorService.scenario.value!!.map.heightPixels + frame2BorderOffset,
        editorService.scenario.value!!.map.widthPixels + frame2BorderOffset,
        -frame2BorderOffset,
    )


    private val tileMapVertices = floatArrayOf(
        0f, editorService.scenario.value!!.map.heightPixels.toFloat(),
        editorService.scenario.value!!.map.widthPixels.toFloat(), 0f,
        0f, 0f,

        0f, editorService.scenario.value!!.map.heightPixels.toFloat(),
        editorService.scenario.value!!.map.widthPixels.toFloat(), editorService.scenario.value!!.map.heightPixels.toFloat(),
        editorService.scenario.value!!.map.widthPixels.toFloat(), 0f,
    )


    // Order matters
    private lateinit var renderStages: List<RenderStage>

    override fun init(drawable: GLAutoDrawable) {
        val ctx = drawable.gl.gL3
        textureStorage.referenceFile = projectRef.referenceFile

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
            ReferenceOverlayStage(ctx, tileMapVertices),
            GridStage(ctx, tileMapVertices),
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
        editorService.save()
        val ctx = drawable.gl.gL3
        textureStorage.loadReference(ctx)
        ctx.glClearColor(0.5f, 0f, 0.5f, 1f)
        ctx.glClear(GL_COLOR_BUFFER_BIT)

        val renderCtx = RenderContext(
            ctx,
            Vector2i(editorService.width, editorService.height),
            textureStorage,
            editorService.viewMatrix,
            editorService.projectionMatrix,
            editorService.scenario.value!!,
            editorService.selectedUnits.value
                .mapNotNull { reference -> reference.getValueOrNull(editorService.scenario.value!!.units::getOrNull) },
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
            )
        )



        renderStages.forEach { stage ->
            if (stage is ColorClosestPointStage && !editorService.enableColorClosestPoint) return@forEach
            if (stage is GridStage && !toolService.gridTool.enabled.value) return@forEach
            if (stage is ReferenceOverlayStage && !toolService.refenceOverlayTool.enabled.value) return@forEach
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

    override fun dispose(drawable: GLAutoDrawable) {
        editorService.save(true)
    }

}