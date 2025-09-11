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
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
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
import kotlin.math.floor
import kotlin.math.max

class EditorRenderer(override val di: DI) : GLEventListener, DIAware {

    private val editorService: EditorService<GameScenario.Preset> by di.instance()
    private val toolService: ToolService by di.instance()

    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f().identity()
    private val textures: MutableMap<String, Int> = ConcurrentHashMap()
    private lateinit var colorProgram: ColorProgram
    private lateinit var selectionProgram: SelectionProgram
    private lateinit var backgroundProgram: BackgroundProgram
    private lateinit var tileMapProgram: TileMapProgram
    private lateinit var blobProcessorProgram: BlobProcessorProgram
    private lateinit var overlayTileProgram: OverlayTileProgram
    private lateinit var spriteProgram: SpriteProgram
    private var selectionStart: Vector2f = Vector2f()
    private var selectionEnd: Vector2f = Vector2f()
    private var selectionEnabled: Boolean = false

    private var backgroundImage: Int = -1

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


    private var terrainMaskTexture: Int = -1
    private var farmOverlayTexture: Int = -1;
    private var heightBlobTexture: Int = -1
    private var objectiveMaskTexture: Int = -1
    private var objectiveOverlayTexture: Int = -1

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


    companion object {
        private const val TERRAIN_PREPEND = "tilesets/terrain"
    }

    private fun getTerrain(path: String): Int {
        return textures["$TERRAIN_PREPEND/$path"]!!
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
        //loadTexture(ctx, "wood")
        loadTexture(ctx, "wood")
//        loadTexture(ctx,"$TERRAIN_PREPEND/grass", false)
//        loadTexture(ctx,"$TERRAIN_PREPEND/snow", false)
        TerrainType.MAIN_TERRAIN.forEach { terrain ->
            loadTexture(ctx, "tilesets/${terrain.textureLocation}", false)
        }
        TerrainType.BLOB_TERRAIN.forEach { terrain ->
            loadAtlas(
                ctx, "tilesets/${terrain.textureLocation}", Vector2i(16), Vector2i(8, 6), ImageFilter(
                    useClamp = true,
                    useLinear = false

                )
            )
        }

        GameUnitType.entries.forEach { unitType ->
            loadTexture(ctx, unitType.maskTexture)
            unitType.overlayTexture?.let { loadTexture(ctx, it) }
        }

        loadTexture(ctx, "objectives/default", useNearest = false, useClamp = true)
        loadTexture(ctx, "objectives/default1", useNearest = false, useClamp = true)

        objectiveMaskTexture = textures["objectives/default"]!!
        objectiveOverlayTexture = textures["objectives/default1"]!!


        TerrainType.entries.mapNotNull { it.overlay }.distinct().forEach {
            println("Loading overlay: $it")
            loadAtlas(
                ctx, "tilesets/${it.textureLocation}", it.elementSize, Vector2i(4), ImageFilter(
                    useClamp = true,
                    useLinear = false
                )
            )
        }
        backgroundImage = textures["wood"]!!

//        backgroundImage = getTerrain("grass")
//        loadTexture(ctx,"tilesets/borderblending/mask", false, useClamp = true)
        loadAtlas(
            ctx, "tilesets/borderblending/mask", Vector2i(32, 32), Vector2i(16, 1), ImageFilter(
                useClamp = true,
                useLinear = false

            )
        )

        terrainMaskTexture = textures["tilesets/borderblending/mask"]!!


        loadAtlas(
            ctx, TerrainType.FARM_BORDERS_LOCATION, Vector2i(32, 32), Vector2i(16, 1), ImageFilter(
                useClamp = true,
                useLinear = false

            )
        )
        farmOverlayTexture = textures[TerrainType.FARM_BORDERS_LOCATION]!!

        loadAtlas(
            ctx, "tilesets/blending/height", Vector2i(16), Vector2i(8, 6), ImageFilter(
                useClamp = true,
                useLinear = false
            )
        )
        heightBlobTexture = textures["tilesets/blending/height"]!!

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

        // TODO continue work on selection. add click handler
        selectionEnabled = false
        selectionStart = Vector2f(-0.5f, 0.5f)
        selectionEnd = Vector2f(0.5f, -0.5f)

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
        ctx.glClear(GL.GL_COLOR_BUFFER_BIT)


        // background program
        ctx.glUseProgram(backgroundProgram.program)
        ctx.glBindVertexArray(backgroundProgram.vao)
        ctx.glBindVBO(backgroundProgram.vbo)
        val viewProjectionMatrix = projectionMatrix.mul(viewMatrix, Matrix4f())
        val invertedMatrix = viewProjectionMatrix.invert(Matrix4f())
        backgroundProgram.applyUniform(
            ctx, BackgroundProgram.Uniform(
                backgroundImage,
                invertedMatrix
            )
        )
        ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)


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
        ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)

        //frame2
        colorProgram.setUpVBO(ctx, ColorProgram.Data(frame2Vertices))
        colorProgram.setUpVAO(ctx)
        colorProgram.applyUniform(
            ctx, ColorProgram.Uniform(
                frame2Color,
                mvpMatrix
            )
        )
        ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)


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
                    terrainMaskTexture,
                    farmOverlayTexture,
                    textures["tilesets/${terrainToRender.textureLocation}"]!!,
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(4, 4),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    Vector4f(1f),
                    Vector2i(width, height)
                )
            )
            ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)
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
                    textures["tilesets/${terrain.textureLocation}"]!!,
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    Vector4f(1f),
                )
            )
            ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)
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
                    heightBlobTexture,
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    Vector4f(1f),
                )
            )
            ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)
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
                    textures["tilesets/${overlay.textureLocation}"]
                        ?: throw IllegalStateException("Can't find tilesets/${overlay.textureLocation}"),
                    Vector2i(editorService.scenario.map.widthTiles, editorService.scenario.map.heightTiles),
                    Vector2i(editorService.scenario.map.widthPixels, editorService.scenario.map.heightPixels),
                    Vector4f(1f),
                    overlay
                )
            )
            ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)
        }

//        val debugCtx = TraceGL3(ctx, System.out)


        ctx.glUseProgram(spriteProgram.program)
        ctx.glBindVertexArray(spriteProgram.vao)
        ctx.glBindVBO(spriteProgram.vbo)
        val unitDimensions = Vector2f(1f, 2f).mul(16f).mul(0.75f)


        val unitsToRender = editorService.scenario.units
            .groupBy { it.owner.getValue(editorService.scenario.players::get).team }
            .mapValues { (_, value) ->
                value.groupBy { it.type }
            }

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
                    textures[it.key]!!,
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
                textures[teamUnitType.second.maskTexture]!!,
                teamUnitType.second.overlayTexture?.let { textures[it]!! } ?: -1
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
                objectiveMaskTexture,
                -1
            ))

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
                    objectiveMaskTexture,
                    objectiveOverlayTexture
                ))

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

        val debugCtx = TraceGL3(ctx, System.out)
//        val debugCtx = ctx


        if (selectionEnabled) {
            debugCtx.glUseProgram(selectionProgram.program)
            debugCtx.glBindVertexArray(selectionProgram.vao)
            debugCtx.glBindVBO(selectionProgram.vbo)

            val min =  selectionStart.min(selectionEnd, Vector2f())
            val max = selectionStart.max(selectionEnd, Vector2f())


//            selectionProgram.setUpVBO(ctx, RectanglePoints.fromPoints(
//                Vector2f(min.x, max.y),
//                Vector2f(max.x, min.y))
//            )
            selectionProgram.setUpVBO(debugCtx, floatArrayOf(
                min.x, max.y,
                min.x, min.y,
                max.x, max.y,

                min.x, min.y,
                max.x, max.y,
                max.x, min.y,
            ))


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

            selectionProgram.applyUniform(debugCtx, SelectionProgram.Uniform(
                Vector4f(0f, 1f, 0f, 1f),
                min,
                max,
                0.54f
            ))

            debugCtx.glDrawArrays(GL_TRIANGLES, 0, 6 )
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

    private fun loadResource(path: String): ByteArray {
        return runBlocking {
            Res.readBytes(path)
        }
    }

    private fun loadShaderSource(path: String): String {
        return loadResource("files/shaders/desktop/${path}.glsl").decodeToString()
    }

    private data class ImageFilter(
        val useLinear: Boolean = true,
        val useClamp: Boolean = false,
        val useMipmaps: Boolean = true,
    ) {

    }

    private fun loadAtlas(
        ctx: GL3,
        key: String,
        tileSize: Vector2i, // size of a tile
        tileDimensions: Vector2i, // amount of tiles
        filter: ImageFilter = ImageFilter(),
    ) {
        val image = loadTextureData(key)


        // Calculate number of tiles
        val tiles = tileDimensions.x * tileDimensions.y

        // Create texture array
        val texturePointer = IntPointer()
        ctx.glGenTextures(1, texturePointer.array, 0)
        val textureId = texturePointer.value

        ctx.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, textureId)

        // Allocate storage for the texture array
        ctx.glTexStorage3D(
            GL3.GL_TEXTURE_2D_ARRAY,
            1, // Mipmap levels
            GL3.GL_RGBA8,
            tileSize.x,
            tileSize.y,
            tiles
        )

        val alphas: MutableList<Int> = mutableListOf()
        // Extract each tile and upload to the texture array
        for (y in 0 until tileDimensions.y) {
            for (x in 0 until tileDimensions.x) {
                val bufferImage = BufferedImage(tileSize.x, tileSize.y, BufferedImage.TYPE_INT_ARGB)

                val layer = y * tileDimensions.x + x

                // Allocate buffer for tile data
                val channel = 4 // Assuming RGBA
                val buffer = ByteBuffer.allocateDirect(tileSize.x * tileSize.y * channel)
                buffer.order(ByteOrder.nativeOrder())

                // Extract tile data from atlas
                for (row in 0 until tileSize.y) {
                    for (col in 0 until tileSize.x) {
                        // Calculate position in atlas
                        val atlasX = x * tileSize.x + col
                        val atlasY = y * tileSize.y + row

                        // Calculate index in atlas data
                        val atlasIndex = (atlasY * image.width + atlasX) * channel

                        // Copy pixel data to buffer
                        if (atlasIndex + 3 < image.image.capacity()) {
                            buffer.put(image.image.get(atlasIndex))     // R
                            buffer.put(image.image.get(atlasIndex + 1)) // G
                            buffer.put(image.image.get(atlasIndex + 2)) // B
                            buffer.put(image.image.get(atlasIndex + 3)) // A
                            alphas.add(image.image.get(atlasIndex + 3).toInt())
//                            val argb = ((image.image.getInt(atlasIndex)) shl 8) or image.image.get(atlasIndex + 3).toInt()

                            val r = image.image.get(atlasIndex).toInt() and 0xFF
                            val g = image.image.get(atlasIndex + 1).toInt() and 0xFF
                            val b = image.image.get(atlasIndex + 2).toInt() and 0xFF
                            val a = image.image.get(atlasIndex + 3).toInt() and 0xFF
                            val argb = (a shl 24) or (r shl 16) or (g shl 8) or b
                            bufferImage.setRGB(row, col, argb)
                        } else {
                            // Handle edge cases by adding transparent pixels
                            buffer.put(0) // R
                            buffer.put(0) // G
                            buffer.put(0) // B
                            buffer.put(0) // A
                            bufferImage.setRGB(row, col, 0)
                        }
                    }
                }
//                val location = "${key}/${layer}.png"
//                val file = File(location)
//                file.parentFile.mkdirs()
//                ImageIO.write(bufferImage, "PNG", file)
//                println("Storing image at ${file.absolutePath} ${bufferImage.width}x${bufferImage.height}")
                // Flip buffer for OpenGL
                buffer.flip()

                // Upload tile to texture array layer
                ctx.glTexSubImage3D(
                    GL3.GL_TEXTURE_2D_ARRAY,
                    0, // Mipmap level
                    0, 0, layer, // x, y, z offsets
                    tileSize.x, tileSize.y, 1, // width, height, depth
                    GL3.GL_RGBA,
                    GL3.GL_UNSIGNED_BYTE,
                    buffer
                )
            }
        }


        if (key.contains("road"))
            println("Average alpha for $key:${alphas.average()}")


        // Set texture parameters based on filter settings
        val minFilter = if (filter.useMipmaps) {
            if (filter.useLinear) GL3.GL_LINEAR_MIPMAP_LINEAR else GL3.GL_NEAREST_MIPMAP_NEAREST
        } else {
            if (filter.useLinear) GL3.GL_LINEAR else GL3.GL_NEAREST
        }

        ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_MIN_FILTER, minFilter)
        ctx.glTexParameteri(
            GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_MAG_FILTER,
            if (filter.useLinear) GL3.GL_LINEAR else GL3.GL_NEAREST
        )

        if (filter.useClamp) {
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE)
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE)
        } else {
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT)
            ctx.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT)
        }

        // Generate mipmaps if requested
        if (filter.useMipmaps) {
            ctx.glGenerateMipmap(GL3.GL_TEXTURE_2D_ARRAY)
        }

        // Unbind texture
        ctx.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, 0)

        textures[key] = texturePointer.value


    }

    private fun loadTexture(
        ctx: GL3,
        key: String,
        useNearest: Boolean = true,
        useClamp: Boolean = false,
    ) { // useNearest for those textures is set to false
        val image = loadTextureData(key) // totally fine
        val textureNameArray: IntArray = IntArray(1)
        ctx.glGenTextures(1, textureNameArray, 0)
        val texture: Int = textureNameArray[0]

        ctx.glBindTexture(GL.GL_TEXTURE_2D, texture)

        val format = GL.GL_RGBA
        ctx.glTexImage2D(
            GL.GL_TEXTURE_2D,
            0,
            format,
            image.width,
            image.height,
            0,
            format,
            GL.GL_UNSIGNED_BYTE,
            image.image
        )
//        ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAX_LEVEL, 4);
        ctx.glGenerateMipmap(GL.GL_TEXTURE_2D)

        if (useClamp) {
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
        } else {
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT)
        }


        if (useNearest)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST_MIPMAP_NEAREST)
        else ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR)
        if (useNearest)
            ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
        else ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR)

        ctx.glBindTexture(GL.GL_TEXTURE_2D, 0)

        textures[key] = texture
    }


    private fun loadTextureData(path: String): RGBAImage {
        val imageWebp = ImageIO.read(loadResource("drawable/images/${path}.webp").inputStream())
        val imageRgba = BufferedImage(
            imageWebp.width,
            imageWebp.height,
            BufferedImage.TYPE_4BYTE_ABGR
        )

        val g: Graphics2D = imageRgba.createGraphics()
        g.drawImage(imageWebp, 0, 0, null)
        g.dispose()
        val rawPixelData = (imageRgba.raster.dataBuffer as DataBufferByte).data

        val rgbaData = ByteArray(rawPixelData.size)



        for (i in 0..<rawPixelData.size step 4) {
            rgbaData[i] = rawPixelData[i + 3]  // R (was last byte)
            rgbaData[i + 1] = rawPixelData[i + 2]  // G
            rgbaData[i + 2] = rawPixelData[i + 1]  // B
            rgbaData[i + 3] = rawPixelData[i]      // A (was first byte)
        }

        val nioBuffer = ByteBuffer.allocateDirect(rgbaData.size)
        nioBuffer.put(rgbaData)
        nioBuffer.flip()
        return RGBAImage(nioBuffer, imageRgba.width, imageRgba.height)
    }

    private fun getMvp(model: Matrix4f): Matrix4f {
        return Matrix4f(projectionMatrix)
            .mul(viewMatrix)
            .mul(model)
    }

    private fun fromScreenToWorldSpace(
        cursorX: Int,
        cursorY: Int,
        viewMatrix: Matrix4f = this@EditorRenderer.viewMatrix,
        projectionMatrix: Matrix4f = this@EditorRenderer.projectionMatrix,
    ): Vector2f {

        val invertProj = projectionMatrix.invert(Matrix4f())
        val invertView = viewMatrix.invert(Matrix4f())

        val winX = (cursorX - width.toFloat() / 2) / (width / 2)
        val winY = (cursorY - height.toFloat() / 2) / (height / 2) * -1


        val cords = Vector4f(winX, winY, 0f, 1f)

        val invProfView = invertView.mul(invertProj, Matrix4f())
        cords.mul(invProfView)
        return Vector2f(cords.x, cords.y)
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
            checkMiddleMouse(e)
            checkTilePainting(e)
        }

        private fun checkTilePainting(e: MouseEvent) {
            if (!isToolDragging) return
            if (leftLastX == null || leftLastY == null) {
                leftLastX = e.x
                leftLastY = e.y
                return
            }
            val oldCords = getTileCordsFromScreenClamp(leftLastX ?: return, leftLastY ?: return)
                .mul(GameConstants.TILE_SIZE)
            leftLastX = e.x
            leftLastY = e.y
            val newCords = getTileCordsFromScreenClamp(leftLastX ?: return, leftLastY ?: return)
                .mul(GameConstants.TILE_SIZE)


            val shouldRender = toolService.useToolManyTimes(
                getPointsBetween(oldCords, newCords)
                    .distinct()
                    .map {
                        Vector2f(it)
                    }, false
            )
            if (shouldRender) rerender()
        }


        private fun checkMiddleMouse(e: MouseEvent) {
            if (!isDragging) return
            val dx = e.x - lastX
            val dy = e.y - lastY
            lastX = e.x
            lastY = e.y

            if (dx == 0 && dy == 0) return

            cameraPosition = cameraPosition.add(dx.toFloat(), dy.toFloat())
            rerender()

        }


    }


    inner class MouseListener(private val rerender: () -> Unit) : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            checkMiddlePressed(e)
            checkToolUsage(e)
        }

        private fun checkRightReleased(e: MouseEvent) {

            // TODO may be used later
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
            if (toolService.useTool(worldCoordinates.x , worldCoordinates.y)) rerender()
        }

        private fun checkEndOfToolUsage(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON3) return
            isToolDragging = false
            toolService.flushCompoundCommands()
            leftLastX = null
            leftLastY = null
        }

        override fun mouseReleased(e: MouseEvent) {
            checkMiddleReleased(e)
            checkRightReleased(e)
            checkEndOfToolUsage(e)
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