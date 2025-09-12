package ua.valeriishymchuk.lobmapeditor.render

import androidx.compose.ui.graphics.Color
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.TraceGL3
import kotlinx.coroutines.runBlocking
import lobmapeditor.composeapp.generated.resources.Res
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Objective
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import ua.valeriishymchuk.lobmapeditor.render.program.*
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.Graphics2D
import java.awt.event.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs
import kotlin.math.max

// DANGER!!!!! DO NOT ENTER WITHOUT A HAZMAT SUIT!!!!
// THE CODE IS VERY RADIOACTIVE AND TOXIC!!!
class EditorRenderer(override val di: DI) : GLEventListener, DIAware {

    private val editorService: EditorService<GameScenario.Preset> by di.instance()
    private val toolService: ToolService by di.instance()
    private val textureStorage: TextureStorage = TextureStorage()

    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f().identity()
    private lateinit var colorProgram: ColorProgram
    private lateinit var selectionProgram: SelectionProgram
    private lateinit var backgroundProgram: BackgroundProgram
    private lateinit var tileMapProgram: TileMapProgram
    private lateinit var blobProcessorProgram: BlobProcessorProgram
    private lateinit var overlayTileProgram: OverlayTileProgram
    private lateinit var spriteProgram: SpriteProgram


    private val unitDimensions = Vector2f(1f, 2f).mul(16f).mul(0.75f)


    private var width: Int = 0
    private var height: Int = 0

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

    val backgroundVertices = floatArrayOf(

        // clip space vertices           // supposed to be texCords, but not actually used
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        1f, 1f, 1f, 1f,


        -1f, -1f, 0f, 0f,
        1f, 1f, 1f, 1f,
        -1f, 1f, 0f, 1f
    )

    private var cameraPosition: Vector2f
        get() = viewMatrix.getColumn(3, Vector4f()).let {
            Vector2f(it.x, it.y)
        }
        set(value) {
            val vector4 = Vector4f()
            vector4.w = 1.0f
            vector4.x = value.x
            vector4.y = value.y
            viewMatrix.setColumn(3, vector4)
        }


    override fun init(drawable: GLAutoDrawable) {
        val ctx = drawable.gl.gL3
//        val debugCtx = TraceGL3(ctx, System.out)
        val debugCtx = ctx


        ctx.glEnable(GL_BLEND)
        ctx.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
//        ctx.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
//        val ctx = TraceGL3(drawable.gl.gL3, System.out)
//        drawable.gl = ctx
        textureStorage.loadTextures(ctx)

//        loadAtlas()

        colorProgram = ColorProgram(
            ctx,
            loadShaderSource("vcolor"),
            loadShaderSource("fcolor")
        )

        backgroundProgram = BackgroundProgram(
            ctx,
            loadShaderSource("vbackground"),
            loadShaderSource("fbackground")
        )

        selectionProgram = SelectionProgram(
            ctx,
            loadShaderSource("vselection"),
            loadShaderSource("fselection")
        )

        tileMapProgram = TileMapProgram(
            ctx,
            loadShaderSource("vtilemap"),
            loadShaderSource("ftilemap")
        )
        blobProcessorProgram = BlobProcessorProgram(
            ctx,
            loadShaderSource("vblobprocessor"),
            loadShaderSource("fblobprocessor")
        )
        overlayTileProgram = OverlayTileProgram(
            ctx,
            loadShaderSource("voverlaytile"),
            loadShaderSource("foverlaytile")
        )

        spriteProgram = SpriteProgram(
            debugCtx,
            loadShaderSource("vsprite"),
            loadShaderSource("fsprite")
        )

        projectionMatrix.setOrtho(
            0f,
            drawable.surfaceWidth.toFloat(),
            drawable.surfaceHeight.toFloat(),
            0f,
            -1f,
            1f
        )
        width = drawable.surfaceWidth
        height = drawable.surfaceHeight

        //vbo = ctx.glGenBuffer()

        backgroundProgram.setUpVBO(ctx, backgroundVertices)
        backgroundProgram.setUpVAO(ctx)

        println("GL initialized")
//        println("Loaded textures: ${textures.keys.joinToString(separator = "\n")}")

    }


    override fun display(drawable: GLAutoDrawable) {
        val ctx = drawable.gl.gL3
        ctx.glClearColor(0.5f, 0f, 0.5f, 1f)
        ctx.glClear(GL_COLOR_BUFFER_BIT)


        // background program
        ctx.glUseProgram(backgroundProgram.program)
        ctx.glBindVertexArray(backgroundProgram.vao)
        ctx.glBindVBO(backgroundProgram.vbo)
        val viewProjectionMatrix = projectionMatrix.mul(viewMatrix, Matrix4f())
        val invertedMatrix = viewProjectionMatrix.invert(Matrix4f())
        backgroundProgram.applyUniform(
            ctx, BackgroundProgram.Uniform(
                textureStorage.backgroundImage,
                invertedMatrix
            )
        )
        ctx.glDrawArrays(GL_TRIANGLES, 0, 6)


        // color program
        ctx.glUseProgram(colorProgram.program)
        val model = Matrix4f().identity()
        val mvpMatrix = getMvp(model)
        ctx.glBindVertexArray(colorProgram.vao)
        ctx.glBindVBO(colorProgram.vbo)

        //frame1
        colorProgram.setUpVBO(ctx, ColorProgram.Data(frame1Vertices))
        colorProgram.setUpVAO(ctx)
        colorProgram.applyUniform(
            ctx, ColorProgram.Uniform(
                frame1Color,
                mvpMatrix
            )
        )
        ctx.glDrawArrays(GL_TRIANGLES, 0, 6)

        //frame2
        colorProgram.setUpVBO(ctx, ColorProgram.Data(frame2Vertices))
        colorProgram.setUpVAO(ctx)
        colorProgram.applyUniform(
            ctx, ColorProgram.Uniform(
                frame2Color,
                mvpMatrix
            )
        )
        ctx.glDrawArrays(GL_TRIANGLES, 0, 6)


        ctx.glUseProgram(tileMapProgram.program)
        ctx.glBindVertexArray(tileMapProgram.vao)
        ctx.glBindVBO(tileMapProgram.vbo)


        TerrainType.entries.sortedBy { it.dominance }.forEach { terrain ->
            tileMapProgram.setUpVBO(ctx, tileMapVertices)
            tileMapProgram.setUpVAO(ctx)
            tileMapProgram.loadMap(ctx, editorService.scenario.map.terrainMap, terrain)
            val terrainToRender = terrain.mainTerrain ?: terrain
            tileMapProgram.applyUniform(
                ctx, TileMapProgram.Uniform(
                    mvpMatrix,
                    textureStorage.terrainMaskTexture,
                    textureStorage.farmOverlayTexture,
                    textureStorage.getTerrainTile(terrainToRender),
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(4, 4),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    terrainToRender.colorTint,
                    Vector2i(width, height)
                )
            )
            ctx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }

        ctx.glUseProgram(blobProcessorProgram.program)
        ctx.glBindVertexArray(blobProcessorProgram.vao)
        ctx.glBindVBO(blobProcessorProgram.vbo)

        TerrainType.BLOB_TERRAIN.reversed().forEach { terrain ->
            blobProcessorProgram.setUpVBO(ctx, tileMapVertices)
            blobProcessorProgram.setUpVAO(ctx)
            blobProcessorProgram.loadMap(ctx, editorService.scenario.map.terrainMap, terrain)
            blobProcessorProgram.applyUniform(
                ctx, BlobProcessorProgram.Uniform(
                    mvpMatrix,
                    textureStorage.getTerrainTile(terrain),
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    Vector4f(1f),
                )
            )
            ctx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }


        val heightMap = editorService.scenario.map.terrainHeight
        val maxTerrain: Int = heightMap.map.flatMap { it }.distinct().max()
        val minTerrain: Int = heightMap.map.flatMap { it }.distinct().min() + 1

        for (heightTile in minTerrain..maxTerrain) {
            blobProcessorProgram.setUpVBO(ctx, tileMapVertices)
            blobProcessorProgram.setUpVAO(ctx)
            blobProcessorProgram.loadHeight(ctx, editorService.scenario.map.terrainHeight, heightTile)
            blobProcessorProgram.applyUniform(
                ctx, BlobProcessorProgram.Uniform(
                    mvpMatrix,
                    textureStorage.heightBlobTexture,
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    Vector4f(1f),
                )
            )
            ctx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }


        ctx.glUseProgram(overlayTileProgram.program)
        ctx.glBindVertexArray(overlayTileProgram.vao)
        ctx.glBindVBO(overlayTileProgram.vbo)

        TerrainType.entries.sortedBy { it.dominance }.filter { it.overlay != null }.forEach { terrain ->
            val overlay = terrain.overlay!!
            overlayTileProgram.setUpVBO(ctx, tileMapVertices)
            overlayTileProgram.setUpVAO(ctx)
            overlayTileProgram.loadMap(ctx, editorService.scenario.map.terrainMap, terrain)
            overlayTileProgram.applyUniform(
                ctx, OverlayTileProgram.Uniform(
                    mvpMatrix,
                    textureStorage.getTerrainOverlay(terrain),
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    Vector4f(1f),
                    overlay
                )
            )
            ctx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }

//        val debugCtx = TraceGL3(ctx, System.out)


        ctx.glUseProgram(spriteProgram.program)
        ctx.glBindVertexArray(spriteProgram.vao)
        ctx.glBindVBO(spriteProgram.vbo)


        val unitsToRender: Map<PlayerTeam, Map<GameUnitType, List<GameUnit>>> = editorService.scenario.units
            .groupBy { it.owner.getValue(editorService.scenario.players::get).team }
            .mapValues { (_, value) ->
                value.groupBy { it.type }
            }

        // rendering selection
        val selectionsToRender = editorService.selectedUnits.toList().mapNotNull { reference ->
            reference.getValueOrNull(editorService.scenario.units::getOrNull)?.position.also { position ->
                if (position == null) {
                    editorService.selectedUnits.remove(reference)
                    println("Deleted invalid reference")
                }
            }
        }
//        unitsToRender.entries
//            .flatMap { units -> units.value.values.flatten() }
//            .map { it.position }

        let {
            spriteProgram.setUpVAO(ctx)
            spriteProgram.applyUniform(
                ctx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    false,
                    true,
                    Vector4f(0f, 0f, 0f, 0.6f),
                    -1,
                    textureStorage.selectionTexture
                )
            )
            val vbo = selectionsToRender.map { position ->
                val positionMatrix = Matrix4f()
//                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(position.x, position.y, 0f))
                val selectionDimensions = Vector2f(
                    32f
                )
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        selectionDimensions.div(-2f, Vector2f()),
                        selectionDimensions.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(ctx, vbo)


            ctx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

//        if (true) return

        val unitShadowsToRender: MutableMap<String, MutableList<GameUnit>> = mutableMapOf()
        val preparedUnitsToRender: MutableMap<Pair<PlayerTeam, GameUnitType>, MutableList<GameUnit>> = mutableMapOf()


        unitsToRender.forEach { (team, units) ->
            units.forEach { (unitType, unitInfo) ->
                unitShadowsToRender.computeIfAbsent(unitType.maskTexture) { mutableListOf() }.addAll(unitInfo)
                preparedUnitsToRender.computeIfAbsent(team to unitType) { mutableListOf() }.addAll(unitInfo)
            }
        }

        unitShadowsToRender.forEach {
            spriteProgram.setUpVAO(ctx)
            spriteProgram.applyUniform(
                ctx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    false,
                    Vector4f(0f, 0f, 0f, 0.6f),
                    textureStorage.textures[it.key]!!,
                    -1
                )
            )
            val vbo = it.value.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(unit.position.x + 2, unit.position.y + 2, 0f))
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        unitDimensions.div(-2f, Vector2f()),
                        unitDimensions.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(ctx, vbo)

            ctx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)


        }

        preparedUnitsToRender.forEach { (teamUnitType, units) ->
            spriteProgram.setUpVAO(ctx)
            spriteProgram.applyUniform(
                ctx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    teamUnitType.second.overlayTexture != null,
                    Vector4f(
                        teamUnitType.first.color.red,
                        teamUnitType.first.color.green,
                        teamUnitType.first.color.blue,
                        1f
                    ),
                    textureStorage.textures[teamUnitType.second.maskTexture]!!,
                    teamUnitType.second.overlayTexture?.let { textureStorage.textures[it]!! } ?: -1
                ))

            val vboInput = units.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        unitDimensions.div(-2f, Vector2f()),
                        unitDimensions.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }


            spriteProgram.setUpVBO(ctx, vboInput)

            ctx.glDrawArrays(GL_TRIANGLES, 0, 6 * vboInput.size)
        }

        val objectiveShadowsToRender: List<Objective> = editorService.scenario.objectives
        val objectivesToRender: Map<Optional<PlayerTeam>, List<Objective>> = objectiveShadowsToRender.groupBy {
            Optional.ofNullable(it.owner?.getValue(editorService.scenario.players::get)?.team)
        }


        val objectiveScale = max((2.5f / viewMatrix.getScale(Vector3f()).x), 1f)


        val objectiveDimensions = Vector2f(
            GameConstants.TILE_SIZE.toFloat()
        ).mul(1.3f)

        spriteProgram.setUpVAO(ctx)
        spriteProgram.applyUniform(
            ctx, SpriteProgram.Uniform(
                projectionMatrix,
                viewMatrix,
                true,
                false,
                Vector4f(0f, 0f, 0f, 0.6f),
                textureStorage.objectiveMaskTexture,
                -1
            )
        )

        val objectiveShadowsVbo = objectiveShadowsToRender.map { unit ->
            val positionMatrix = Matrix4f()
            positionMatrix.setTranslation(Vector3f(unit.position.x + 1, unit.position.y + 1, 0f))
            positionMatrix.scale(objectiveScale)
            SpriteProgram.BufferData(
                RectanglePoints.fromPoints(
                    objectiveDimensions.div(-2f, Vector2f()),
                    objectiveDimensions.div(2f, Vector2f()),
                ),
                RectanglePoints.TEXTURE_CORDS,
                positionMatrix
            )
        }


        spriteProgram.setUpVBO(ctx, objectiveShadowsVbo)

        ctx.glDrawArrays(GL_TRIANGLES, 0, 6 * objectiveShadowsVbo.size)

        objectivesToRender.forEach { (teamOpt, objectives) ->
            val color = teamOpt.getOrNull()?.color ?: Color(0.6f, 0.6f, 0.6f, 1f)
            spriteProgram.setUpVAO(ctx)
            spriteProgram.applyUniform(
                ctx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    true,
                    Vector4f(
                        color.red,
                        color.green,
                        color.blue,
                        color.alpha
                    ),
                    textureStorage.objectiveMaskTexture,
                    textureStorage.objectiveOverlayTexture
                )
            )

            val vbo = objectives.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                positionMatrix.scale(objectiveScale)
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        objectiveDimensions.div(-2f, Vector2f()),
                        objectiveDimensions.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }


            spriteProgram.setUpVBO(ctx, vbo)

            ctx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

//        val debugCtx = TraceGL3(ctx, System.out)
        val debugCtx = ctx


        if (selectionEnabled) {
            debugCtx.glUseProgram(selectionProgram.program)
            debugCtx.glBindVertexArray(selectionProgram.vao)
            debugCtx.glBindVBO(selectionProgram.vbo)

            val min = selectionStart.min(selectionEnd, Vector2f())
            val max = selectionStart.max(selectionEnd, Vector2f())


//            selectionProgram.setUpVBO(ctx, RectanglePoints.fromPoints(
//                Vector2f(min.x, max.y),
//                Vector2f(max.x, min.y))
//            )
            selectionProgram.setUpVBO(
                debugCtx, floatArrayOf(
                    min.x, max.y,
                    min.x, min.y,
                    max.x, max.y,

                    min.x, min.y,
                    max.x, max.y,
                    max.x, min.y,
                )
            )


//            selectionProgram.setUpVBO(debugCtx, floatArrayOf(
//                -0.5f, 0.5f,
//                -0.5f, -0.5f,
//                0.5f, 0.5f,
//
//                -0.5f, -0.5f,
//                0.5f, 0.5f,
//                0.5f, -0.5f,
//
//            ))
            selectionProgram.setUpVAO(debugCtx)

            val borderSizePx = 4

            val thickness = Vector2f(
                1f / width * borderSizePx,
                1f / height * borderSizePx
            )

            selectionProgram.applyUniform(
                debugCtx, SelectionProgram.Uniform(
                    Vector4f(0f, 1f, 0f, 1f),
                    min,
                    max,
                    thickness.y,
                    thickness.x
                )
            )

            debugCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
//            debugCtx.glDrawArrays(GL_LINE_LOOP, 0, 6 )
            val error = debugCtx.glGetError()
            if (error != GL_NO_ERROR) {
                println("OpenGL error: $error")
            }

        }
        ctx.glBindVertexArray(0)


    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        val ctx = drawable.gl.gL3
        ctx.glViewport(0, 0, width, height)

        projectionMatrix.setOrtho(
            0f,
            width.toFloat(),
            height.toFloat(),
            0f,
            -1f,
            1f
        )

        this.width = width
        this.height = height

    }

    override fun dispose(drawable: GLAutoDrawable) {}

    private fun getMvp(model: Matrix4f): Matrix4f {
        return Matrix4f(projectionMatrix)
            .mul(viewMatrix)
            .mul(model)
    }

    private fun fromScreenToNDC(
        cursorX: Int, cursorY: Int,
    ): Vector2f {

        val winX = (cursorX - width.toFloat() / 2) / (width / 2)
        val winY = (cursorY - height.toFloat() / 2) / (height / 2) * -1
        return Vector2f(winX, winY)
    }

    private fun fromNDCToWorldSpace(
        ndc: Vector2f,
        viewMatrix: Matrix4f = this@EditorRenderer.viewMatrix,
        projectionMatrix: Matrix4f = this@EditorRenderer.projectionMatrix,
    ): Vector2f {
        val invertProj = projectionMatrix.invert(Matrix4f())
        val invertView = viewMatrix.invert(Matrix4f())
        val invProfView = invertView.mul(invertProj, Matrix4f())
        val cords = Vector4f(ndc, 0f, 1f)
        cords.mul(invProfView)
        return Vector2f(cords.x, cords.y)
    }

    private fun fromScreenToWorldSpace(
        cursorX: Int,
        cursorY: Int,
        viewMatrix: Matrix4f = this@EditorRenderer.viewMatrix,
        projectionMatrix: Matrix4f = this@EditorRenderer.projectionMatrix,
    ): Vector2f {

//        val invertProj = projectionMatrix.invert(Matrix4f())
//        val invertView = viewMatrix.invert(Matrix4f())
//
////        val winX = (cursorX - width.toFloat() / 2) / (width / 2)
////        val winY = (cursorY - height.toFloat() / 2) / (height / 2) * -1
//
//
//        val cords = Vector4f(fromScreenToNDC(cursorX, cursorY), 0f, 1f)
//
//        val invProfView = invertView.mul(invertProj, Matrix4f())
//        cords.mul(invProfView)
//        return Vector2f(cords.x, cords.y)
        return fromNDCToWorldSpace(
            fromScreenToNDC(cursorX, cursorY),
            viewMatrix,
            projectionMatrix
        )
    }

    private fun getTileCordsFromScreenClamp(cursorX: Int, cursorY: Int): Vector2i {
        val worldCoordinates = fromScreenToWorldSpace(cursorX, cursorY)
        val map = editorService.scenario.map

        // Clamp world coordinates to map boundaries
        val clampedX = worldCoordinates.x.coerceIn(0f, (map.widthPixels - 1).toFloat())
        val clampedY = worldCoordinates.y.coerceIn(0f, (map.heightPixels - 1).toFloat())

        // Convert to tile coordinates
        val tileX = (clampedX / GameConstants.TILE_SIZE).toInt()
        val tileY = (clampedY / GameConstants.TILE_SIZE).toInt()

        return Vector2i(tileX, tileY)
    }


    fun getPointsBetween(start: Vector2i, end: Vector2i): List<Vector2i> {
        val points = mutableListOf<Vector2i>()

        val x0 = start.x
        val y0 = start.y
        val x1 = end.x
        val y1 = end.y

        val dx = abs(x1 - x0)
        val dy = -abs(y1 - y0)

        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1

        var error = dx + dy
        var currentX = x0
        var currentY = y0

        while (true) {
            points.add(Vector2i(currentX, currentY))
            if (currentX == x1 && currentY == y1) break
            val e2 = 2 * error
            if (e2 >= dy) {
                if (currentX == x1) break
                error += dy
                currentX += sx
            }
            if (e2 <= dx) {
                if (currentY == y1) break
                error += dx
                currentY += sy
            }
        }

        return points
    }

    private var lastX = 0
    private var lastY = 0
    private var isDragging = false

    private var leftLastX: Int? = null
    private var leftLastY: Int? = null
    private var isToolDragging = false

    private var isShiftPressed = false
    private var isCtrlPressed = false

    private var selectionStart: Vector2f = Vector2f()
    private var selectionEnd: Vector2f = Vector2f()
    private var selectionEnabled: Boolean = false
    private var isSelectionDragging: Boolean = false


    inner class KeyPressListener(private val rerender: () -> Unit) : KeyAdapter() {

        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_SHIFT -> isShiftPressed = true
                KeyEvent.VK_CONTROL -> isCtrlPressed = true
            }
        }

        override fun keyReleased(e: KeyEvent) {
            when (e.keyCode) {
//                KeyEvent.VK_F -> setTerrainHeight = !setTerrainHeight
                KeyEvent.VK_Z -> {
                    if (!isCtrlPressed) return
                    if (isShiftPressed) {
                        editorService.redo()
                    } else editorService.undo()
                    rerender()
                }

                KeyEvent.VK_SHIFT -> isShiftPressed = false
                KeyEvent.VK_CONTROL -> isCtrlPressed = false
            }
        }
    }

    inner class MouseMotionListener(private val rerender: () -> Unit) : MouseMotionAdapter() {

        override fun mouseDragged(e: MouseEvent) {

            val shouldRender = listOf(
                checkMiddleMouse(e), checkTilePainting(e), checkSelectionDrag(e)
            ).any { it }
            if (shouldRender) rerender()
        }

        private fun checkTilePainting(e: MouseEvent): Boolean {
            if (!isToolDragging) return false
            if (leftLastX == null || leftLastY == null) {
                leftLastX = e.x
                leftLastY = e.y
                return false
            }
            val oldCords = getTileCordsFromScreenClamp(
                leftLastX ?: return false,
                leftLastY ?: return false
            )
                .mul(GameConstants.TILE_SIZE)
            leftLastX = e.x
            leftLastY = e.y
            val newCords = getTileCordsFromScreenClamp(
                leftLastX ?: return false,
                leftLastY ?: return false
            )
                .mul(GameConstants.TILE_SIZE)


            val shouldRender = toolService.useToolManyTimes(
                getPointsBetween(oldCords, newCords)
                    .distinct()
                    .map {
                        Vector2f(it)
                    }, false
            )
            return shouldRender
        }


        private fun checkMiddleMouse(e: MouseEvent): Boolean {
            if (!isDragging) return false
            val dx = e.x - lastX
            val dy = e.y - lastY
            lastX = e.x
            lastY = e.y

            if (dx == 0 && dy == 0) return false

            cameraPosition = cameraPosition.add(dx.toFloat(), dy.toFloat())
            return true

        }

        private fun checkSelectionDrag(e: MouseEvent): Boolean {
            if (!isSelectionDragging) return false
            selectionEnd = fromScreenToNDC(e.x, e.y)
            if (!selectionEnabled && selectionStart.distance(selectionEnd) > 0.05f) selectionEnabled = true
            return selectionEnabled
        }


    }


    inner class MouseListener(private val rerender: () -> Unit) : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            checkMiddlePressed(e)
            checkToolUsage(e)
            checkStartOfSelection(e)
        }

        override fun mouseReleased(e: MouseEvent) {
            checkMiddleReleased(e)
            checkEndOfSelection(e)
            checkEndOfToolUsage(e)
        }

        private fun checkStartOfSelection(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON1) return
            selectionStart = fromScreenToNDC(e.x, e.y)
            selectionEnd = fromScreenToNDC(e.x, e.y)
            isSelectionDragging = true
        }

        private fun checkEndOfSelection(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON1) return
            isSelectionDragging = false
            if (!selectionEnabled) {
//                if (!isShiftPressed) editorService.selectedUnits.clear()
                val clickedPoint = fromScreenToWorldSpace(e.x, e.y)
                val unitDimensionsMin = unitDimensions.div(-2f, Vector2f())
                val unitDimensionsMax = unitDimensions.div(2f, Vector2f())

                val selectedUnits = editorService.scenario.units.filter { unit ->
                    val positionMatrix = Matrix4f()
                    positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                    positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                    val inversePositionMatrix = positionMatrix.invert(Matrix4f())
                    val localPoint4f = Vector4f(clickedPoint, 0f, 1f)
                        .mul(inversePositionMatrix, Vector4f())
                    val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
                    unitDimensionsMin.x < localPoint.x && localPoint.x < unitDimensionsMax.x &&
                            unitDimensionsMin.y < localPoint.y && localPoint.y < unitDimensionsMax.y
                }



                val newSelectedUnits = selectedUnits.map { unit ->
                    Reference<Int, GameUnit>(editorService.scenario.units.indexOf(unit))
                }
                if (!isShiftPressed && !isCtrlPressed) editorService.selectedUnits.clear()
                if (!isCtrlPressed) editorService.selectedUnits.addAll(newSelectedUnits)
                else editorService.selectedUnits.removeAll(newSelectedUnits.toSet())
                rerender()

                return
            }
            selectionEnabled = false
            val worldPosStart = fromNDCToWorldSpace(selectionStart)
            val worldPosEnd = fromNDCToWorldSpace(selectionEnd)
            val worldPosMin = worldPosStart.min(worldPosEnd, Vector2f())
            val worldPosMax = worldPosStart.max(worldPosEnd, Vector2f())
            val selectedUnits = editorService.scenario.units.filter { unit ->
                val pos = unit.position
                worldPosMin.x < pos.x && pos.x < worldPosMax.x &&
                        worldPosMin.y < pos.y && pos.y < worldPosMax.y
            }
            val newSelectedUnits = selectedUnits.map { unit ->
                Reference<Int, GameUnit>(editorService.scenario.units.indexOf(unit))
            }
            if (!isShiftPressed && !isCtrlPressed) editorService.selectedUnits.clear()
            if (!isCtrlPressed) editorService.selectedUnits.addAll(newSelectedUnits)
            else editorService.selectedUnits.removeAll(newSelectedUnits.toSet())
            rerender()
        }

        private fun checkMiddlePressed(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON2) return
            lastX = e.x
            lastY = e.y
            isDragging = true
        }

        private fun checkMiddleReleased(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON2) return
            leftLastX = e.x
            leftLastX = e.y
            isDragging = false
        }

        private fun checkToolUsage(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON3) return
            isToolDragging = true
            val worldCoordinates = fromScreenToWorldSpace(e.x, e.y)
            if (worldCoordinates.x < 0 || worldCoordinates.y < 0) return
            if (worldCoordinates.x > editorService.scenario.map.widthPixels || worldCoordinates.y > editorService.scenario.map.heightPixels) return
            if (toolService.useTool(worldCoordinates.x, worldCoordinates.y)) rerender()
        }

        private fun checkEndOfToolUsage(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON3) return
            isToolDragging = false
            toolService.flushCompoundCommands()
            leftLastX = null
            leftLastY = null
        }


        override fun mouseWheelMoved(e: MouseWheelEvent) {
            val zoomIntensity = 0.1
            val zoomFactor = 1.0 + zoomIntensity * -e.wheelRotation // Invert scroll direction

            // Get world position of mouse before scaling
            val oldWorldPos = fromScreenToWorldSpace(e.x, e.y)

            // Create a copy of the current view matrix and apply scaling
            val newView = Matrix4f(viewMatrix)
            newView.scaleLocal(zoomFactor.toFloat(), zoomFactor.toFloat(), 1f)

            // Get world position of mouse after scaling (without translation adjustment)
            val newWorldPos = fromScreenToWorldSpace(e.x, e.y, newView)

            // Calculate required translation adjustment in world space
            val delta = Vector2f(oldWorldPos).sub(newWorldPos)

            // Extract scale factors from scaled view matrix
            val scaleX = newView.m00()
            val scaleY = newView.m11()

            if (scaleX !in 0.08..14.0) return

            // Apply translation adjustment (inverse because view matrix is inverse of camera)
            val translation = newView.getColumn(3, Vector4f())
            translation.x -= delta.x * scaleX
            translation.y -= delta.y * scaleY
            newView.setColumn(3, translation)

            // Update main view matrix and request redraw
            viewMatrix.set(newView)
            rerender()
        }

    }


}