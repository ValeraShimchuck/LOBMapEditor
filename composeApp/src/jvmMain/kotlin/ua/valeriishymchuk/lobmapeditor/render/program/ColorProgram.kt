package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.opengl.GL
import org.joml.Matrix4f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import java.nio.FloatBuffer

class ColorProgram(
    ctx: CurrentGL,
    vertexSource: String,
    fragmentSource: String,
): Program<ColorProgram.Data, ColorProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = let {
        val vao = IntPointer()
        ctx.glGenVertexArrays(1, vao.array, 0)
        vao.value
    }

    override val vbo: Int = let {
        ctx.glGenBuffer()
    }

    val mvpLocation: Int = let {
        ctx.glGetUniformLocation(program, "uMVP")
    }

    val colorLocation: Int = let {
        ctx.glGetUniformLocation(program, "color")
    }

    override fun setUpVBO(
        ctx: CurrentGL,
        data: Data
    ) {
        ctx.glBindVertexArray(vao)
        ctx.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo)
        val buffer = BufferHelper.wrapDirect(data.vertices)
        buffer.flip()
        ctx.glBufferData(GL.GL_ARRAY_BUFFER, data.vertices.size * Float.SIZE_BYTES.toLong(), buffer, GL.GL_STATIC_DRAW)
    }

    override fun setUpVAO(ctx: CurrentGL) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        ctx.glEnableVertexAttribArray(0)
    }

    override fun applyUniform(
        ctx: CurrentGL,
        data: Uniform
    ) {
        ctx.glUseProgram(program)
        val matrixBuffer = BufferHelper.allocateDirectFloatBuffer(4 * 4)
        data.modelViewProjection.get(matrixBuffer)
        matrixBuffer.flip()
        ctx.glUniformMatrix4fv(mvpLocation, 1, false, matrixBuffer)
        val colorBuffer = BufferHelper.allocateDirectFloatBuffer(4)
        data.color.get(colorBuffer)
        colorBuffer.flip()
        ctx.glUniform4fv(colorLocation, 1, colorBuffer)
    }


    data class Data(
        val vertices: FloatArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Data

            return vertices.contentEquals(other.vertices)
        }

        override fun hashCode(): Int {
            return vertices.contentHashCode()
        }
    }

    data class Uniform(
        val color: Vector4f,
        val modelViewProjection: Matrix4f
    )

}