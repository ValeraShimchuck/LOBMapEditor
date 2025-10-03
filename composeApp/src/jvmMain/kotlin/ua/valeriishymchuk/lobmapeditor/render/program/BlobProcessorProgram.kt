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
import ua.valeriishymchuk.lobmapeditor.shared.dimension.ArrayMap2d

class BlobProcessorProgram(
    ctx: GL3,
    vertexSource: String,
    fragmentSource: String
): Program<FloatArray, BlobProcessorProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val mvpLocation = ctx.glGetUniformLocation(program, "uMVP")
    val tileMapLocation = ctx.glGetUniformLocation(program, "uTileMap")
    val blobTextureLocation = ctx.glGetUniformLocation(program, "uBlobTexture")
    val tileUnitLocation = ctx.glGetUniformLocation(program, "uTileUnit")
    val mapSizeLocation = ctx.glGetUniformLocation(program, "uMapSize")
    val colorTintLocation = ctx.glGetUniformLocation(program, "uColorTint")


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

    fun loadHeight(ctx: GL3, heightMap: ArrayMap2d<Int>, tileHeight: Int) {
        ctx.glBindTexture(GL3.GL_TEXTURE_2D, tileMapTexture)
        val width = heightMap.sizeX
        val height = heightMap.sizeY
        val buffer = Buffers.newDirectIntBuffer(width * height)

        val serializedData = heightMap.map.flatMap { it }.map {
            if (tileHeight <= it) 1 else 0
        }

        serializedData.forEach {
            buffer.put(it)
        }
//        println("Found ${serializedData.filter { it != 0 }.size} of $terrainType")
        // Found 4 of SNOW out of thousands, thats fine
        // but everything is set to snow
        // something fishy is going on with loading the data
        buffer.flip()


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
    }

    // it will be painfully slow, we've got to change the storage format for the sake of performance
    fun loadMap(ctx: GL3, terrainMap: TerrainMap, terrainType: TerrainType) {

        // also we might want to use Pixel Buffer Objects

        ctx.glBindTexture(GL3.GL_TEXTURE_2D, tileMapTexture)
        val width = terrainMap.sizeX
        val height = terrainMap.sizeY
        val buffer = Buffers.newDirectIntBuffer(width * height)

        val serializedData = terrainMap.map.flatMap { it }.map {
            if (terrainType == it) return@map 1
            if (it == TerrainType.BRIDGE && (terrainType == TerrainType.ROAD || terrainType == TerrainType.ROAD_WINTER || terrainType == TerrainType.SUNKEN_ROAD)) return@map 2
            if (terrainType == TerrainType.BRIDGE && (it == TerrainType.ROAD || it == TerrainType.ROAD_WINTER || it == TerrainType.SUNKEN_ROAD)) return@map 2
            if (it == TerrainType.SUNKEN_ROAD && terrainType == TerrainType.ROAD || it == TerrainType.ROAD && terrainType == TerrainType.SUNKEN_ROAD ) return@map 2
            0
        }

        serializedData.forEach {
            buffer.put(it)
        }
//        println("Found ${serializedData.filter { it != 0 }.size} of $terrainType")
        // Found 4 of SNOW out of thousands, thats fine
        // but everything is set to snow
        // something fishy is going on with loading the data
        buffer.flip()


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
        ctx.glActiveTexture(GL.GL_TEXTURE0 + 1)
        ctx.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, data.blobTexture)
        ctx.glUniform1i(blobTextureLocation, 1)
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

    private fun GL3.applyTexture(uniformLocation: Int, textureUnit: Int, textureId: Int) {
        glActiveTexture(GL.GL_TEXTURE0 + textureUnit)
        glBindTexture(GL3.GL_TEXTURE_2D, textureId)
        glUniform1i(uniformLocation, textureUnit)
    }


    data class Uniform(
        val mvp: Matrix4f,
        val blobTexture: Int, // texture of the tile,
        val tileUnit: Vector2f,
        val mapSize: Vector2f, // in world units
        val colorTint: Vector4f,
    ) {
        constructor(
            mvp: Matrix4f,
            blobTexture: Int,
            mapTileSize: Vector2i,
            mapSize: Vector2i, // in world units
            colorTint: Vector4f,
        ): this(
            mvp,
            blobTexture,
            Vector2f(1f / mapTileSize.x, 1f / mapTileSize.y),
            Vector2f(mapSize.x.toFloat(), mapSize.y.toFloat()),
            colorTint,

        )
    }

}