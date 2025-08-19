package ua.valeriishymchuk.lobmapeditor.render

import com.jogamp.opengl.GL
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
import ua.valeriishymchuk.lobmapeditor.command.CommandDispatcher
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.BackgroundProgram
import ua.valeriishymchuk.lobmapeditor.render.program.ColorProgram
import ua.valeriishymchuk.lobmapeditor.render.program.TileMapProgram
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseWheelEvent
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

class OpenGLListener(private val commandDispatcher: CommandDispatcher<GameScenario.Preset>) : GLEventListener {

    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f().identity()
    private val textures: MutableMap<String, Int> = ConcurrentHashMap()
    private lateinit var colorProgram: ColorProgram
    private lateinit var backgroundProgram: BackgroundProgram
    private lateinit var tileMapProgram: TileMapProgram

    private var backgroundImage: Int = -1

    private var width: Int = 0
    private var height: Int = 0

    private val frame1BorderOffset = 21f
    private val frame1Color = Vector4f(33f, 19f, 10f, 255f).div(255f)
    private val frame1Vertices = floatArrayOf(
        -frame1BorderOffset, commandDispatcher.scenario.map.heightPixels + frame1BorderOffset,
        commandDispatcher.scenario.map.widthPixels + frame1BorderOffset, -frame1BorderOffset,
        -frame1BorderOffset, -frame1BorderOffset,

        -frame1BorderOffset, commandDispatcher.scenario.map.heightPixels + frame1BorderOffset,
        commandDispatcher.scenario.map.widthPixels + frame1BorderOffset, commandDispatcher.scenario.map.heightPixels + frame1BorderOffset,
        commandDispatcher.scenario.map.widthPixels + frame1BorderOffset, -frame1BorderOffset,
    )


    private val frame2BorderOffset = 16f
    private val frame2Color = Vector4f(162f, 157f, 131f, 255f).div(255f)
    private val frame2Vertices = floatArrayOf(
        -frame2BorderOffset, commandDispatcher.scenario.map.heightPixels + frame2BorderOffset,
        commandDispatcher.scenario.map.widthPixels + frame2BorderOffset, -frame2BorderOffset,
        -frame2BorderOffset, -frame2BorderOffset,

        -frame2BorderOffset, commandDispatcher.scenario.map.heightPixels + frame2BorderOffset,
        commandDispatcher.scenario.map.widthPixels + frame2BorderOffset, commandDispatcher.scenario.map.heightPixels + frame2BorderOffset,
        commandDispatcher.scenario.map.widthPixels + frame2BorderOffset, -frame2BorderOffset,
    )


    private var terrainMaskTexture: Int = -1

    private val tileMapVertices = floatArrayOf(
        0f, commandDispatcher.scenario.map.heightPixels.toFloat(),
        commandDispatcher.scenario.map.widthPixels.toFloat(), 0f,
        0f, 0f,

        0f, commandDispatcher.scenario.map.heightPixels.toFloat(),
        commandDispatcher.scenario.map.widthPixels.toFloat(), commandDispatcher.scenario.map.heightPixels.toFloat(),
        commandDispatcher.scenario.map.widthPixels.toFloat(), 0f,
    )


    val testObjectVertices = floatArrayOf(
            150.0f, 150.0f,
            100.0f, 150.0f,
            125.0f, 125.0f,
//        -0.5f, -0.5f,
//        0.5f, -0.5f,
//        0.0f, 0.5f,
    )

    val backgroundVertices = floatArrayOf(

        // clip space vertices           // supposed to be texCords, but not actually used
        -1f, -1f,                        0f, 0f,
         1f, -1f,                        1f, 0f,
         1f,  1f,                        1f, 1f,


        -1f, -1f,                        0f, 0f,
         1f,  1f,                        1f, 1f,
        -1f,  1f,                        0f, 1f
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
//        val ctx = TraceGL3(drawable.gl.gL3, System.out)
//        drawable.gl = ctx
        //loadTexture(ctx, "wood")
        loadTexture(ctx, "wood")
        loadTexture(ctx,"$TERRAIN_PREPEND/grass", false)
        loadTexture(ctx,"$TERRAIN_PREPEND/snow")
        backgroundImage = textures["wood"]!!

//        backgroundImage = getTerrain("grass")
        loadTexture(ctx,"tilesets/borderblending/mask")
        terrainMaskTexture = textures["tilesets/borderblending/mask"]!!


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

        tileMapProgram = TileMapProgram(
            ctx,
            loadShaderSource("vtilemap"),
            loadShaderSource("ftilemap")
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
        backgroundProgram.applyUniform(ctx, BackgroundProgram.Uniform(
            backgroundImage,
            invertedMatrix
        ))
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
        colorProgram.applyUniform(ctx, ColorProgram.Uniform(
            frame1Color,
            mvpMatrix
        ))
        ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)

        //frame2
        colorProgram.setUpVBO(ctx, ColorProgram.Data(frame2Vertices))
        colorProgram.setUpVAO(ctx)
        colorProgram.applyUniform(ctx, ColorProgram.Uniform(
            frame2Color,
            mvpMatrix
        ))
        ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)


        ctx.glUseProgram(tileMapProgram.program)
        ctx.glBindVertexArray(tileMapProgram.vao)
        ctx.glBindVBO(tileMapProgram.vbo)

        tileMapProgram.setUpVBO(ctx, tileMapVertices)
        tileMapProgram.setUpVAO(ctx)
        tileMapProgram.loadMap(ctx, commandDispatcher.scenario.map.terrainMap, TerrainType.GRASS)
        tileMapProgram.applyUniform(ctx, TileMapProgram.Uniform(
            mvpMatrix,
            terrainMaskTexture,
            getTerrain("grass"),
            Vector2i(commandDispatcher.scenario.map.widthTiles,commandDispatcher.scenario.map.heightTiles),
            Vector2i(4, 4),
            Vector2i(commandDispatcher.scenario.map.widthPixels,commandDispatcher.scenario.map.heightPixels),
            Vector4f(0.95f, 1f, 0.95f, 1f).mul(0.9f)
        ))
        ctx.glDrawArrays(GL.GL_TRIANGLES, 0, 6)




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

    private fun loadTexture(ctx: GL3, key: String, useNearest: Boolean = true) {
        val image = loadTextureData(key)
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

        ctx.glGenerateMipmap(GL.GL_TEXTURE_2D)

        ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT)
        ctx.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT)

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
            rgbaData[i]     = rawPixelData[i + 3]  // R (was last byte)
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

    private var lastX = 0
    private var lastY = 0
    private var isDragging = false

    inner class MouseMotionListener(private val rerender: () -> Unit) : MouseMotionAdapter() {

        override fun mouseDragged(e: MouseEvent) {
            if (isDragging) {
                val dx = e.x - lastX
                val dy = e.y - lastY
                lastX = e.x
                lastY = e.y

                if (dx == 0 && dy == 0 ) return

                cameraPosition = cameraPosition.add(dx.toFloat(), dy.toFloat())
                rerender()
                //moveCamera(dx, dy)
            }
        }


    }


    inner class MouseListener(private val rerender: () -> Unit) : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON2) return
            lastX = e.x
            lastY = e.y
            isDragging = true
        }

        override fun mouseReleased(e: MouseEvent) {
            if (e.button != MouseEvent.BUTTON2) return
            isDragging = false
        }

        private fun fromScreenToWorldSpace(
            cursorX: Int,
            cursorY: Int,
            viewMatrix: Matrix4f = this@OpenGLListener.viewMatrix,
            projectionMatrix: Matrix4f = this@OpenGLListener.projectionMatrix
        ): Vector2f {

            // 100 100 on screen space
            // turn them into NDC[-1..1]
            // using invert projection map to camera space
            // using invert view map to world space


            val invertProj = projectionMatrix.invert(Matrix4f())
            val invertView = viewMatrix.invert(Matrix4f())

            val winX = (cursorX - width.toFloat() / 2) / (width / 2)
            val winY = (cursorY - height.toFloat() / 2) / (height / 2) * -1


            val cords = Vector4f(winX, winY, 0f, 1f)

            val invProfView = invertView.mul(invertProj, Matrix4f())
            cords.mul(invProfView)
            return Vector2f(cords.x, cords.y)
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