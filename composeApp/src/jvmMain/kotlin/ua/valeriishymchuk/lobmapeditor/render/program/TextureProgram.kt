package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import kotlin.let

class TextureProgram(
    ctx: GL3,
    vertexSource: String,
    fragmentSource: String
): Program<FloatArray, TextureProgram.Uniform> {

    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    private val textureLocation = ctx.glGetUniformLocation(program, "uTexture")
    private val colorTintLocation = ctx.glGetUniformLocation(program, "uColorTint")
    private val textureMatrixLocation = ctx.glGetUniformLocation(program, "uTextureMatrix")
    private val mvpLocation = ctx.glGetUniformLocation(program, "uMVP")


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

    override fun applyUniform(ctx: GL3, data: Uniform) {
        ctx.glUseProgram(program)
        val tintBuffer = BufferHelper.allocateDirectFloatBuffer(4)
        data.colorTint.get(tintBuffer)
        tintBuffer.flip()
        ctx.glUniform4fv(colorTintLocation, 1, tintBuffer)

        val matrixBuffer = BufferHelper.allocateDirectFloatBuffer(4 * 4)
        data.textureMatrix.get(matrixBuffer)
        matrixBuffer.flip()
        ctx.glUniformMatrix4fv(textureMatrixLocation, 1, false, matrixBuffer)


        data.mvp.get(matrixBuffer)
        matrixBuffer.flip()
        ctx.glUniformMatrix4fv(mvpLocation, 1, false, matrixBuffer)


        val activeTexture = GL.GL_TEXTURE0
        ctx.glActiveTexture(activeTexture)
        ctx.glBindTexture(GL.GL_TEXTURE_2D, data.texture)
        ctx.glUniform1i(textureLocation, 0)
    }

    data class Uniform(
        val mvp: Matrix4f,
        val textureMatrix: Matrix4f,
        val colorTint: Vector4f,
        val texture: Int
        )

}