package ua.valeriishymchuk.lobmapeditor.render

import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL3
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
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.currentGl
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import ua.valeriishymchuk.lobmapeditor.render.stage.*
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import java.lang.Math
import kotlin.time.TimeSource


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
        0f,
        editorService.scenario.value!!.map.heightPixels.toFloat(),
        editorService.scenario.value!!.map.widthPixels.toFloat(),
        0f,
        0f,
        0f,

        0f,
        editorService.scenario.value!!.map.heightPixels.toFloat(),
        editorService.scenario.value!!.map.widthPixels.toFloat(),
        editorService.scenario.value!!.map.heightPixels.toFloat(),
        editorService.scenario.value!!.map.widthPixels.toFloat(),
        0f,
    )

    class PerformanceQueries(
        val map: Map<RenderStage, Int>,
        var available: IntPointer,
        var shouldStartCounter: Boolean
    ) {
        val queryPointers: IntArray = IntArray(map.size + 1)
        val disabledRenderStages: MutableSet<RenderStage> = mutableSetOf()
    }

    // Order matters
    private lateinit var renderStages: List<RenderStage>
    private lateinit var performanceQueries: PerformanceQueries

    override fun init(drawable: GLAutoDrawable) {
        val ctx = drawable.gl.currentGl()
        textureStorage.referenceFile = projectRef.referenceFile

        ctx.glEnable(GL_BLEND)
        ctx.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

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
            RangeStage(ctx),
            SpriteStage(ctx),
            SelectionStage(ctx),
            UnitBarsStage(ctx)
        )

        var queryIndex = 0;

        performanceQueries = PerformanceQueries(
            renderStages.associateWith { queryIndex++ },
            IntPointer(),
            true
        )

        ctx.glGenQueries(performanceQueries.queryPointers.size, performanceQueries.queryPointers, 0)

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
        val ctx = drawable.gl.currentGl()

        if (!performanceQueries.shouldStartCounter) {
            ctx.glGetQueryObjectuiv(
                performanceQueries.queryPointers.last(),
                GL3.GL_QUERY_RESULT_AVAILABLE,
                performanceQueries.available.array,
                0
            )
            if (performanceQueries.available.value != 0) {
                performanceQueries.shouldStartCounter = true
                val startedQuery = LongArray(1)
                ctx.glGetQueryObjectui64v(
                    performanceQueries.queryPointers.last(),
                    GL3.GL_QUERY_RESULT,
                    startedQuery,
                    0
                )
                var lastTimeStamp = startedQuery[0]
                val elapsedTimes =
                    performanceQueries.map.filterNot { (key, _) -> performanceQueries.disabledRenderStages.contains(key) }
                        .mapValues { (_, value) ->
                            val longArray = LongArray(1)
                            ctx.glGetQueryObjectui64v(
                                performanceQueries.queryPointers[value],
                                GL3.GL_QUERY_RESULT,
                                longArray,
                                0
                            )
                            val elapsed = longArray[0] - lastTimeStamp
                            lastTimeStamp = longArray[0]
                            return@mapValues elapsed
                        }

                val divider = 1_000_000.0
                val totalElapsed = lastTimeStamp - startedQuery[0]
                println("Finished render GPU SIDE measurements, total elapsed: ${totalElapsed / divider} millis")
                elapsedTimes.forEach { (key, value) ->
                    println("Elapsed for ${key::class.simpleName} - ${value / divider} millis")
                }
                performanceQueries.available.array[0] = 0
            }

        }

        val shouldQueryTimer = toolService.debugTool.debugInfo.value.measurePerformanceGPU
                && performanceQueries.shouldStartCounter
        if (shouldQueryTimer) performanceQueries.shouldStartCounter = false

        fun queryTimer(index: Int) {
            if (!shouldQueryTimer) return
            ctx.glQueryCounter(performanceQueries.queryPointers[index], GL3.GL_TIMESTAMP)
        }

        fun queryTimer(stage: RenderStage) {
            queryTimer(performanceQueries.map[stage]!!)

        }

        queryTimer(performanceQueries.queryPointers.size - 1)

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
            ),
            toolService.debugTool.debugInfo.value
        )


        val timeSource = TimeSource.Monotonic
        val start = timeSource.markNow()
        val marks = mutableListOf<Pair<String, TimeSource.Monotonic.ValueTimeMark>>()
        renderStages.forEach { stage ->

            if (stage is SpriteStage && toolService.refenceOverlayTool.hideSprites.value) {
                performanceQueries.disabledRenderStages.add(stage)
                return@forEach
            }

            if (stage is RangeStage && (toolService.refenceOverlayTool.hideSprites.value || toolService.refenceOverlayTool.hideRange.value)) {
                performanceQueries.disabledRenderStages.add(stage)
                return@forEach
            }

            if (stage is ColorClosestPointStage && !editorService.enableColorClosestPoint) {
                performanceQueries.disabledRenderStages.add(stage)
                return@forEach
            }
            if (stage is GridStage && !toolService.gridTool.enabled.value) {
                performanceQueries.disabledRenderStages.add(stage)
                return@forEach
            }
            if (stage is ReferenceOverlayStage && !toolService.refenceOverlayTool.enabled.value) {
                performanceQueries.disabledRenderStages.add(stage)
                return@forEach
            }
            stage.draw(renderCtx)
            queryTimer(stage)
            performanceQueries.disabledRenderStages.remove(stage)
            marks.add(stage::class.simpleName!! to timeSource.markNow())
        }

        val end = timeSource.markNow()
        if (toolService.debugTool.debugInfo.value.measurePerformanceCPU) {
            println("Prepared frame on CPU SIDE, it took: ${end - start} to render it. Summary for every stage:")
            var lastMark = start
            marks.forEach { (stage, mark) ->
                println("$stage: ${mark - lastMark}")
                lastMark = mark
            }
        }

        ctx.glBindVertexArray(0)
        val error = ctx.glGetError()
        if (error != GL_NO_ERROR) {
            System.err.println("OpenGL error $error")
        }


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