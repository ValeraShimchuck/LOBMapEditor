package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.GL
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.terrain.HeightMap
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainMap
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.helper.*
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import ua.valeriishymchuk.lobmapeditor.shared.dimension.ArrayMap2d

class BlobProcessorProgram(
    ctx: CurrentGL,
    vertexSource: String,
    fragmentSource: String
): Program<FloatArray, BlobProcessorProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val mvpLocation = ctx.glGetUniformLocation(program, "uMVP")
    val tileMapLocation = ctx.glGetUniformLocation(program, "uTileMap")
    val blobTextureLocation = ctx.glGetUniformLocation(program, "uBlobTexture")
    val shouldDrawMaskLocation = ctx.glGetUniformLocation(program, "uShouldDrawMask")
    val maskColorLocation = ctx.glGetUniformLocation(program, "uMaskColor")
    val blobMaskTextureLocation = ctx.glGetUniformLocation(program, "uBlobMask")
    val tileUnitLocation = ctx.glGetUniformLocation(program, "uTileUnit")
    val mapSizeLocation = ctx.glGetUniformLocation(program, "uMapSize")
    val colorTintLocation = ctx.glGetUniformLocation(program, "uColorTint")


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

    fun loadHeight(ctx: CurrentGL, heightMap: HeightMap, tileHeight: Int) {
        ctx.glBindTexture(CurrentGL.GL_TEXTURE_2D, tileMapTexture)
        val width = heightMap.sizeX
        val height = heightMap.sizeY
        val buffer = heightMap.getHeightBlobMap(tileHeight).buffer


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

    fun loadMap(ctx: CurrentGL, terrainMap: TerrainMap, terrainType: TerrainType) {


        ctx.glBindTexture(CurrentGL.GL_TEXTURE_2D, tileMapTexture)
        val width = terrainMap.sizeX
        val height = terrainMap.sizeY
        val buffer = terrainMap.getBlobRenderMap(terrainType).buffer
        ctx.glTexImage2D(
            CurrentGL.GL_TEXTURE_2D,
            0,
            CurrentGL.GL_R32UI,
            width,
            height,
            0,
            CurrentGL.GL_RED_INTEGER,
            CurrentGL.GL_UNSIGNED_INT,
            buffer
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
        ctx.glBindTexture(CurrentGL.GL_TEXTURE_2D_ARRAY, data.blobTexture)
        ctx.glUniform1i(blobTextureLocation, 1)

        val mask = data.mask
        ctx.glUniform1i(shouldDrawMaskLocation, if(mask != null ) 1 else 0)
        if (mask != null) {
            ctx.glActiveTexture(GL.GL_TEXTURE0 + 2)
            ctx.glBindTexture(CurrentGL.GL_TEXTURE_2D_ARRAY, mask.blobMask)
            ctx.glUniform1i(blobMaskTextureLocation, 2)
            ctx.glUniform4fv(maskColorLocation, 1, floatArrayOf(mask.maskColor.x, mask.maskColor.y, mask.maskColor.z, mask.maskColor.w), 0)
        }

//        if (maskTextureLocation >= 0) // handling the fact that mask texute location can be discarded by compiler because its not being used
//            ctx.applyTexture(maskTextureLocation, 2, data.maskTexture)

        ctx.glUniform2fv(tileUnitLocation, 1, floatArrayOf(data.tileUnit.x, data.tileUnit.y), 0)
        ctx.glUniform2fv(mapSizeLocation, 1, floatArrayOf(data.mapSize.x, data.mapSize.y), 0)

        ctx.glUniform4fv(colorTintLocation, 1, floatArrayOf(data.colorTint.x, data.colorTint.y, data.colorTint.z, data.colorTint.w), 0)

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
        val blobTexture: Int, // texture of the tile,
        val tileUnit: Vector2f,
        val mapSize: Vector2f, // in world units
        val colorTint: Vector4f,
        val mask: Mask?

    ) {
        constructor(
            mvp: Matrix4f,
            blobTexture: Int,
            mapTileSize: Vector2i,
            mapSize: Vector2i, // in world units
            colorTint: Vector4f,
            mask: Mask? = null
        ): this(
            mvp,
            blobTexture,
            Vector2f(1f / mapTileSize.x, 1f / mapTileSize.y),
            Vector2f(mapSize.x.toFloat(), mapSize.y.toFloat()),
            colorTint,
            mask
        )

        data class Mask(
            val blobMask: Int,
            val maskColor: Vector4f
        )

    }

}