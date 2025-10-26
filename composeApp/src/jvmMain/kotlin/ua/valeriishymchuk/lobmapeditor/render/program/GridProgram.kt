package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.opengl.GL
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData

class GridProgram(
    ctx: CurrentGL,
    vertexSource: String,
    fragmentSource: String,
) : Program<FloatArray, GridProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val mvpLocation = ctx.glGetUniformLocation(program, "uMVP")
    val offsetLocation = ctx.glGetUniformLocation(program, "uOffset")
    val gridSizeLocation = ctx.glGetUniformLocation(program, "uGridSize")
    val gridThicknessLocation = ctx.glGetUniformLocation(program, "uGridThickness")
    val colorLocation = ctx.glGetUniformLocation(program, "uGridColor")

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

        val matrixBuffer = BufferHelper.allocateDirectFloatBuffer(4 * 4)
        data.mvpMatrix.get(matrixBuffer)
        matrixBuffer.flip()
        ctx.glUniformMatrix4fv(mvpLocation, 1, false, matrixBuffer)

        val vectorBuffer = BufferHelper.allocateDirectFloatBuffer(2)
        data.offset.get(vectorBuffer)
        vectorBuffer.flip()
        ctx.glUniform2fv(offsetLocation, 1, vectorBuffer)

        data.gridSize.get(vectorBuffer)
        vectorBuffer.flip()
        ctx.glUniform2fv(gridSizeLocation, 1, vectorBuffer)

        ctx.glUniform1f(gridThicknessLocation, data.thickness)

        val colorBuffer = BufferHelper.allocateDirectFloatBuffer(4)
        data.color.get(colorBuffer)
        colorBuffer.flip()
        ctx.glUniform4fv(colorLocation, 1, colorBuffer)

    }

    data class Uniform(
        val offset: Vector2f,
        val gridSize: Vector2f,
        val thickness: Float,
        val color: Vector4f,
        val mvpMatrix: Matrix4f
    )

}