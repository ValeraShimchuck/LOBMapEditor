package ua.valeriishymchuk.lobmapeditor.render

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import java.nio.FloatBuffer

@Deprecated(level = DeprecationLevel.ERROR, message = "BS, will not be used.")
data class DrawObject(
    val position: Vector2f,
    val uv: Vector2f, // no idea what is that
    val color: Vector4f,
    val textureId: Int, // not a texture pointer!!
    val shouldRoundPixels: Boolean
) {

    fun setUpVBO(ctx: GL3, vbo: Int) {
        ctx.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo)
        val floatBuffer: FloatBuffer = BufferHelper.allocateDirectFloatBuffer(VBO_SIZE / Float.SIZE_BYTES)

        position.get(floatBuffer)
        uv.get(floatBuffer)
        color.get(floatBuffer)
        floatBuffer.put(if (shouldRoundPixels) 1.0f else 0.0f)
        floatBuffer.put(textureId.toFloat())

        floatBuffer.flip()
        ctx.glBufferData(GL.GL_ARRAY_BUFFER, VBO_SIZE.toLong(), floatBuffer, GL.GL_STATIC_DRAW)
    }

    companion object {

        private val VBO_SIZE = Float.SIZE_BYTES * 10

        fun setUpVAO(ctx: GL3): Int {
            val vaoArray: IntArray = IntArray(1)
            ctx.glGenVertexArrays(1, vaoArray, 0)
            val vao = vaoArray[0]
            ctx.glBindVertexArray(vao)

            // vec2
            ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, VBO_SIZE, 0)
            ctx.glEnableVertexAttribArray(0)

            // vec2
            ctx.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, VBO_SIZE, Float.SIZE_BYTES * 2L)
            ctx.glEnableVertexAttribArray(1)

            // vec4
            ctx.glVertexAttribPointer(2, 4, GL.GL_FLOAT, false, VBO_SIZE, Float.SIZE_BYTES * 4L)
            ctx.glEnableVertexAttribArray(2)

            // vec2
            ctx.glVertexAttribPointer(3, 2, GL.GL_FLOAT, false, VBO_SIZE, Float.SIZE_BYTES * 8L)
            ctx.glEnableVertexAttribArray(3)

            ctx.glBindVertexArray(0)
            return vao

        }
    }
}
