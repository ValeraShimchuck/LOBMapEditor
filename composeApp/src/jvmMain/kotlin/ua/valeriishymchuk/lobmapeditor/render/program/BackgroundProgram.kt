package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector3f
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData

class BackgroundProgram(
    ctx: GL3,
    vertexSource: String,
    fragmentSource: String
) : Program<FloatArray, BackgroundProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val tileTextureLocation: Int = ctx.glGetUniformLocation(program, "uTileTexture")
    val tintColorLocation: Int = ctx.glGetUniformLocation(program, "uTintColor")
    val invViewProjectionLocation: Int = ctx.glGetUniformLocation(program, "uInvViewProjection")

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
        ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0.toLong())
        ctx.glEnableVertexAttribArray(0)
        ctx.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 4 * Float.SIZE_BYTES, 2L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(1)
    }

    override fun applyUniform(
        ctx: GL3,
        data: Uniform
    ) {
        ctx.glUseProgram(program)
        val tintBuffer = BufferHelper.allocateDirectFloatBuffer(3)
        data.tintColor.get(tintBuffer)
        tintBuffer.flip()
        ctx.glUniform3fv(tintColorLocation, 1, tintBuffer)
        val activeTexture = GL.GL_TEXTURE0
        ctx.glActiveTexture(activeTexture)
        ctx.glBindTexture(GL.GL_TEXTURE_2D, data.texture)
        ctx.glUniform1i(tileTextureLocation, activeTexture)

        val matrixBuffer = BufferHelper.allocateDirectFloatBuffer(4 * 4)
        data.invViewProjectionLocation.get(matrixBuffer)
        matrixBuffer.flip()
        ctx.glUniformMatrix4fv(invViewProjectionLocation, 1, false, matrixBuffer)
    }


    data class Uniform(
        val texture: Int,
        val invViewProjectionLocation: Matrix4f,
        val tintColor: Vector3f = Vector3f(1f),
    )

}