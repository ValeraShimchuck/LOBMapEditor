package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainMap
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.helper.*
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer

class TileMapProgram(
    ctx: GL3,
    vertexSource: String,
    fragmentSource: String
): Program<FloatArray, TileMapProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val mvpLocation = ctx.glGetUniformLocation(program, "uMVP")
    val tileMapLocation = ctx.glGetUniformLocation(program, "uTileMap")
    val maskTextureLocation = ctx.glGetUniformLocation(program, "uMaskTexture")
    val tileTextureLocation = ctx.glGetUniformLocation(program, "uTileTexture")
    val tileUnitLocation = ctx.glGetUniformLocation(program, "uTileUnit")
    val textureScaleLocation = ctx.glGetUniformLocation(program, "uTextureScale")
    val mapSizeLocation = ctx.glGetUniformLocation(program, "uMapSize")
    val colorTintLocation = ctx.glGetUniformLocation(program, "uColorTint")
    val resolutionLocation = ctx.glGetUniformLocation(program, "uResolution")
    val overlayLocation = ctx.glGetUniformLocation(program, "uOverlayTexture")


    val tileMapTexture: Int = let {

        val textureID = IntPointer()
        ctx.glGenTextures(1, textureID.array, 0)
        ctx.glBindTexture(GL3.GL_TEXTURE_2D, textureID.value)


        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE)
        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE)
        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST)
        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST)
        textureID.value
    }

    // it will be painfully slow, we've got to change the storage format for the sake of performance
    // Yes, it is indeed slow
    // optimized a bit, but for the best perfomance I have to change the whole structure of the map storage, but right now it is an overkill
    fun loadMap(ctx: GL3, terrainMap: TerrainMap, terrainType: TerrainType): Boolean {
        val width = terrainMap.sizeX
        val height = terrainMap.sizeY
        val data = terrainMap.getRenderMap(terrainType)
        val hasSomethingToRender = data.shouldRender
        if (!hasSomethingToRender) return false
        val buffer = data.buffer
        ctx.glBindTexture(GL3.GL_TEXTURE_2D, tileMapTexture)

        ctx.glTexImage2D(
            GL3.GL_TEXTURE_2D,
            0,
            GL3.GL_R32UI,
            width,
            height,
            0,
            GL3.GL_RED_INTEGER,
            GL3.GL_UNSIGNED_INT,
            buffer // Pass the buffer directly instead of null
        )

        return true

    }


    override fun setUpVBO(ctx: GL3, data: FloatArray) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        val buffer = BufferHelper.wrapDirect(data)
        buffer.flip()
        ctx.glVBOData(data.size * Float.SIZE_BYTES, buffer)
    }

    override fun setUpVAO(ctx: GL3) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0.toLong())
        ctx.glEnableVertexAttribArray(0)
    }

    override fun applyUniform(
        ctx: GL3,
        data: Uniform
    ) {
        ctx.glUseProgram(program)
        ctx.applyTexture(tileMapLocation, 0, tileMapTexture)

        ctx.applyTexture(tileTextureLocation, 1, data.tileTexture)

        ctx.glActiveTexture(GL.GL_TEXTURE0 + 2)
        ctx.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, data.maskTexture)
        ctx.glUniform1i(maskTextureLocation, 2)


        ctx.glActiveTexture(GL.GL_TEXTURE0 + 3)
        ctx.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, data.overlayTexture)
        ctx.glUniform1i(overlayLocation, 3)
//        if (maskTextureLocation >= 0) // handling the fact that mask texute location can be discarded by compiler because its not being used
//            ctx.applyTexture(maskTextureLocation, 2, data.maskTexture)

        ctx.glUniform2fv(tileUnitLocation, 1, floatArrayOf(data.tileUnit.x, data.tileUnit.y), 0)
        ctx.glUniform2fv(textureScaleLocation, 1, floatArrayOf(data.textureScale.x, data.textureScale.y), 0)
        ctx.glUniform2fv(mapSizeLocation, 1, floatArrayOf(data.mapSize.x, data.mapSize.y), 0)
        ctx.glUniform2fv(resolutionLocation, 1, floatArrayOf(data.resolution.x, data.resolution.y), 0)

        ctx.glUniform4fv(colorTintLocation, 1, floatArrayOf(data.colorTint.x, data.colorTint.y, data.colorTint.z, data.colorTint.w), 0)

        val matrixBuffer = BufferHelper.allocateDirectFloatBuffer(4 * 4)
        data.mvp.get(matrixBuffer)
        matrixBuffer.flip()

        ctx.glUniformMatrix4fv(mvpLocation, 1, false, matrixBuffer)



    }

    private fun GL3.applyTexture(uniformLocation: Int, textureUnit: Int, textureId: Int) {
        glActiveTexture(GL.GL_TEXTURE0 + textureUnit)
        glBindTexture(GL3.GL_TEXTURE_2D, textureId)
        glUniform1i(uniformLocation, textureUnit)
    }


    data class Uniform(
        val mvp: Matrix4f,
//        val tileMap: Int, // 2d map of unsigned ints
        val maskTexture: Int,
        val overlayTexture: Int,
        val tileTexture: Int, // texture of the tile,
        val tileUnit: Vector2f,
        val textureScale: Vector2f,
        val mapSize: Vector2f, // in world units
        val colorTint: Vector4f,
        val resolution: Vector2f
    ) {
        constructor(
            mvp: Matrix4f,
            maskTexture: Int,
            overlayTexture: Int,
            tileTexture: Int,
            mapTileSize: Vector2i,
            textureTileSize: Vector2i,
            mapSize: Vector2i, // in world units
            colorTint: Vector4f,
            resolution: Vector2i
        ): this(
            mvp,
            maskTexture,
            overlayTexture,
            tileTexture,
            Vector2f(1f / mapTileSize.x, 1f / mapTileSize.y),
            Vector2f( mapTileSize.x.toFloat() / textureTileSize.x, mapTileSize.y.toFloat() / textureTileSize.y),
            Vector2f(mapSize.x.toFloat(), mapSize.y.toFloat()),
            colorTint,
            Vector2f(resolution)

        )
    }

}