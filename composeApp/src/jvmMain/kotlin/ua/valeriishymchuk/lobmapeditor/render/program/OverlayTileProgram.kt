package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.opengl.GL
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainMap
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.helper.*
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer

class OverlayTileProgram(
    ctx: CurrentGL,
    vertexSource: String,
    fragmentSource: String
): Program<FloatArray, OverlayTileProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val mvpLocation = ctx.glGetUniformLocation(program, "uMVP")
    val tileMapLocation = ctx.glGetUniformLocation(program, "uTileMap")
    val tileUnitLocation = ctx.glGetUniformLocation(program, "uTileUnit")
    val mapSizeLocation = ctx.glGetUniformLocation(program, "uMapSize")
    val colorTintLocation = ctx.glGetUniformLocation(program, "uColorTint")
    val overlayLocation = ctx.glGetUniformLocation(program, "uOverlayTexture")
    val randomRangeLocation = ctx.glGetUniformLocation(program, "uRandomRange")
    val overlayAmountLocation = ctx.glGetUniformLocation(program, "uOverlayAmount")
    val scaleLocation = ctx.glGetUniformLocation(program, "uScale")
    val offsetLocation = ctx.glGetUniformLocation(program, "uOffset")


    val tileMapTexture: Int = let {

        val textureID = IntPointer()
        ctx.glGenTextures(1, textureID.array, 0)
        ctx.glBindTexture(CurrentGL.GL_TEXTURE_2D, textureID.value)


        ctx.glTexParameteri(CurrentGL.GL_TEXTURE_2D, CurrentGL.GL_TEXTURE_WRAP_S, CurrentGL.GL_CLAMP_TO_EDGE)
        ctx.glTexParameteri(CurrentGL.GL_TEXTURE_2D, CurrentGL.GL_TEXTURE_WRAP_T, CurrentGL.GL_CLAMP_TO_EDGE)
        ctx.glTexParameteri(CurrentGL.GL_TEXTURE_2D, CurrentGL.GL_TEXTURE_MIN_FILTER, CurrentGL.GL_NEAREST)
        ctx.glTexParameteri(CurrentGL.GL_TEXTURE_2D, CurrentGL.GL_TEXTURE_MAG_FILTER, CurrentGL.GL_NEAREST)
        textureID.value
    }

    fun loadMap(ctx: CurrentGL, terrainMap: TerrainMap, terrainType: TerrainType) {


        ctx.glBindTexture(CurrentGL.GL_TEXTURE_2D, tileMapTexture)
        val width = terrainMap.sizeX
        val height = terrainMap.sizeY
        val buffer = terrainMap.getOverlayRenderMap(terrainType).buffer


        ctx.glTexImage2D(
            CurrentGL.GL_TEXTURE_2D,
            0,
            CurrentGL.GL_R32UI,
            width,
            height,
            0,
            CurrentGL.GL_RED_INTEGER,
            CurrentGL.GL_UNSIGNED_INT,
            buffer // Pass the buffer directly instead of null
        )


    }


    override fun setUpVBO(ctx: CurrentGL, data: FloatArray) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        val buffer = BufferHelper.wrapDirect(data)
        buffer.flip()
        ctx.glVBOData(data.size * Float.SIZE_BYTES, buffer)
    }

    override fun setUpVAO(ctx: CurrentGL) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0.toLong())
        ctx.glEnableVertexAttribArray(0)
    }

    override fun applyUniform(
        ctx: CurrentGL,
        data: Uniform
    ) {
        ctx.glUseProgram(program)
        ctx.applyTexture(tileMapLocation, 0, tileMapTexture)

        ctx.glActiveTexture(GL.GL_TEXTURE0 + 1)
        ctx.glBindTexture(CurrentGL.GL_TEXTURE_2D_ARRAY, data.overlayTexture)
        ctx.glUniform1i(overlayLocation, 1)

        ctx.glUniform2fv(tileUnitLocation, 1, floatArrayOf(data.tileUnit.x, data.tileUnit.y), 0)
        ctx.glUniform2fv(mapSizeLocation, 1, floatArrayOf(data.mapSize.x, data.mapSize.y), 0)

        ctx.glUniform4fv(colorTintLocation, 1, floatArrayOf(data.colorTint.x, data.colorTint.y, data.colorTint.z, data.colorTint.w), 0)

        ctx.glUniform1f(randomRangeLocation, data.overlayInfo.randomRange)
        ctx.glUniform1ui(overlayAmountLocation, data.overlayInfo.overlayAmount)
        ctx.glUniform1f(scaleLocation, data.overlayInfo.scale)
        ctx.glUniform1f(offsetLocation, data.overlayInfo.offset)

        val matrixBuffer = BufferHelper.allocateDirectFloatBuffer(4 * 4)
        data.mvp.get(matrixBuffer)
        matrixBuffer.flip()

        ctx.glUniformMatrix4fv(mvpLocation, 1, false, matrixBuffer)



    }

    private fun CurrentGL.applyTexture(uniformLocation: Int, textureUnit: Int, textureId: Int) {
        glActiveTexture(GL.GL_TEXTURE0 + textureUnit)
        glBindTexture(CurrentGL.GL_TEXTURE_2D, textureId)
        glUniform1i(uniformLocation, textureUnit)
    }


    data class Uniform(
        val mvp: Matrix4f,
        val overlayTexture: Int,
        val tileUnit: Vector2f,
        val mapSize: Vector2f, // in world units
        val colorTint: Vector4f,
        val overlayInfo: TerrainType.OverlayInfo
    ) {
        constructor(
            mvp: Matrix4f,
            overlayTexture: Int,
            mapTileSize: Vector2i,
            mapSize: Vector2i, // in world units
            colorTint: Vector4f,
            overlayInfo: TerrainType.OverlayInfo
        ): this(
            mvp,
            overlayTexture,
            Vector2f(1f / mapTileSize.x, 1f / mapTileSize.y),
            Vector2f(mapSize.x.toFloat(), mapSize.y.toFloat()),
            colorTint,
            overlayInfo,
        )
    }

}