package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer

class ColorClosestPointProgram(
    ctx: GL3,
    vertexSource: String,
    fragmentSource: String
): Program<FloatArray, ColorClosestPointProgram.Uniform> {

    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val pointTextureLocation: Int = ctx.glGetUniformLocation(program, "pointTexture")
    val totalPointsLocation: Int = ctx.glGetUniformLocation(program, "totalPoints")
    val mvpLocation: Int = ctx.glGetUniformLocation(program, "uMVP")

    val pointMapTexture: Int = let {

        val textureID = IntPointer()
        ctx.glGenTextures(1, textureID.array, 0)
        ctx.glBindTexture(GL3.GL_TEXTURE_2D, textureID.value)


        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE)
        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE)
        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST)
        ctx.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST)
        textureID.value
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

    private fun loadPoints(ctx: GL3, uniform: Uniform) {
        ctx.glBindTexture(GL3.GL_TEXTURE_2D, pointMapTexture)

        val width = uniform.points.entries.sumOf { it.value.size }
        val buffer = Buffers.newDirectFloatBuffer(width * 4)

        uniform.points.entries.flatMap { entry -> entry.value.map { entry.key to it } }
            .forEachIndexed { i, (team, point) ->
                buffer.put(point.x)
                buffer.put(point.y)
                buffer.put(if (team == PlayerTeam.RED) 0f else 1f)
                buffer.put(0f)
            }

        buffer.flip()

        ctx.glTexImage2D(
            GL3.GL_TEXTURE_2D,
            0,
            GL3.GL_RGBA32F,
            width,
            1,
            0,
            GL3.GL_RGBA,
            GL3.GL_FLOAT,
            buffer // Pass the buffer directly instead of null
        )
    }

    override fun applyUniform(
        ctx: GL3,
        data: Uniform
    ) {
        ctx.glUseProgram(program)
        loadPoints(ctx, data)
        ctx.applyTexture(pointTextureLocation, 0, pointMapTexture)
        ctx.glUniform1i(totalPointsLocation, data.points.entries.sumOf { it.value.size })

        val matrixBuffer = BufferHelper.allocateDirectFloatBuffer(4 * 4)
        data.mvpMatrix.get(matrixBuffer)
        matrixBuffer.flip()
        ctx.glUniformMatrix4fv(mvpLocation, 1, false, matrixBuffer)

    }

    private fun GL3.applyTexture(uniformLocation: Int, textureUnit: Int, textureId: Int) {
        glActiveTexture(GL.GL_TEXTURE0 + textureUnit)
        glBindTexture(GL3.GL_TEXTURE_2D, textureId)
        glUniform1i(uniformLocation, textureUnit)
    }

    data class Uniform(
        val mvpMatrix: Matrix4f,
        val points: Map<PlayerTeam, List<Vector2f>>
    )

}