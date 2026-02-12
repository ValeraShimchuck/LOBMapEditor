package ua.valeriishymchuk.lobmapeditor.render.renderer

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.currentGl
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import ua.valeriishymchuk.lobmapeditor.render.stage.ColorClosestPointStage
import ua.valeriishymchuk.lobmapeditor.render.stage.DeploymentZoneStage
import ua.valeriishymchuk.lobmapeditor.render.stage.GridStage
import ua.valeriishymchuk.lobmapeditor.render.stage.RangeStage
import ua.valeriishymchuk.lobmapeditor.render.stage.ReferenceOverlayStage
import ua.valeriishymchuk.lobmapeditor.render.stage.RenderStage
import ua.valeriishymchuk.lobmapeditor.render.stage.SpriteStage
import ua.valeriishymchuk.lobmapeditor.render.stage.UnitBarsStage
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.HybridToolService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import kotlin.math.min
import kotlin.time.TimeSource

abstract class EditorRenderer<S: GameScenario<S>, CTX: RenderContext<S>>(override val di: DI) : GLEventListener, DIAware {

    protected open val editorService: EditorService<*> by di.instance()
    protected val toolService: ToolService<*> by di.instance()
    protected val textureStorage: TextureStorage by di.instance()
    protected val projectRef: ProjectRef by di.instance()

    protected val frame1BorderOffset = 21f
    protected val frame1Color = Vector4f(33f, 19f, 10f, 255f).div(255f)
    protected val frame1Vertices = floatArrayOf(
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


    protected val frame2BorderOffset = 16f
    protected val frame2Color = Vector4f(162f, 157f, 131f, 255f).div(255f)
    protected val frame2Vertices = floatArrayOf(
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


    protected val tileMapVertices = floatArrayOf(
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
    protected lateinit var renderStages: List<RenderStage>
    protected lateinit var performanceQueries: PerformanceQueries

    abstract fun createRenderStages(ctx: CurrentGL): List<RenderStage>

    override fun init(drawable: GLAutoDrawable) {
        val ctx = drawable.gl.currentGl()
        textureStorage.referenceFile = projectRef.referenceFile

        ctx.glEnable(GL.GL_BLEND)
        ctx.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)

        textureStorage.loadTextures(ctx)

        renderStages = createRenderStages(ctx)

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

    abstract fun prepareContext(gl: CurrentGL): CTX

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

        val shouldQueryTimer = toolService.miscTool.debugInfo.value.measurePerformanceGPU
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
        ctx.glClear(GL.GL_COLOR_BUFFER_BIT)

        val renderCtx = prepareContext(ctx)


        val timeSource = TimeSource.Monotonic
        val start = timeSource.markNow()
        val marks = mutableListOf<Pair<String, TimeSource.Monotonic.ValueTimeMark>>()
        renderStages.forEach { stage ->

            if (stage is SpriteStage && toolService.refenceOverlayTool.hideSprites.value) {
                performanceQueries.disabledRenderStages.add(stage)
                return@forEach
            }

            if (stage is UnitBarsStage && toolService.refenceOverlayTool.hideSprites.value) {
                performanceQueries.disabledRenderStages.add(stage)
                return@forEach
            }
            val hybridToolService = toolService as? HybridToolService
            if (stage is DeploymentZoneStage && hybridToolService != null && hybridToolService.deploymentZoneTool.isHidden.value) {
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
        if (toolService.miscTool.debugInfo.value.measurePerformanceCPU) {
            println("Prepared frame on CPU SIDE, it took: ${end - start} to render it. Summary for every stage:")
            var lastMark = start
            marks.forEach { (stage, mark) ->
                println("$stage: ${mark - lastMark}")
                lastMark = mark
            }
        }

        ctx.glBindVertexArray(0)
        val error = ctx.glGetError()
        if (error != GL.GL_NO_ERROR) {
            System.err.println("OpenGL error $error")
        }


    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        val ctx = drawable.gl.gL3
        val fbWidth = drawable.surfaceWidth
        val fbHeight = drawable.surfaceHeight
        ctx.glViewport(0, 0, fbWidth, fbHeight)



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