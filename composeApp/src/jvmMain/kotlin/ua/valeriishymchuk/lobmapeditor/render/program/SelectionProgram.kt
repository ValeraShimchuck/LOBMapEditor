package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData

class SelectionProgram(
    ctx: GL3,
    vertexShaderSource: String,
    fragmentShaderSource: String
): Program<FloatArray, SelectionProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexShaderSource, fragmentShaderSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val colorLocation: Int = ctx.glGetUniformLocation(program, "uColor")
    val selectionMinLocation: Int = ctx.glGetUniformLocation(program, "uSelectionMin")
    val selectionMaxLocation: Int = ctx.glGetUniformLocation(program, "uSelectionMax")
    val thicknessLocation: Int = ctx.glGetUniformLocation(program, "uThickness")



    override fun setUpVBO(
        ctx: GL3,
        data: FloatArray
    ) {

        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)

        println(data.joinToString())
        val buffer = BufferHelper.wrapDirect(data)
        ctx.glVBOData(buffer.capacity() * 4,buffer)
    }

    override fun setUpVAO(ctx: GL3) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 8, 0.toLong())
        ctx.glEnableVertexAttribArray(0)
    }

    override fun applyUniform(
        ctx: GL3,
        data: Uniform
    ) {
        ctx.glUseProgram(program)
        var buffer = BufferHelper.allocateDirectFloatBuffer(4)
        data.color.get(buffer)
        buffer.flip()
        ctx.glUniform4fv(colorLocation, 1, buffer)

        buffer = BufferHelper.allocateDirectFloatBuffer(2)
        data.selectionMin.get(buffer)
        buffer.flip()
        ctx.glUniform2fv(selectionMinLocation, 1, buffer)


        buffer = BufferHelper.allocateDirectFloatBuffer(2)
        data.selectionMax.get(buffer)
        buffer.flip()
        ctx.glUniform2fv(selectionMaxLocation, 1, buffer)

        ctx.glUniform1f(thicknessLocation, data.thickness)

    }

    data class Uniform (
        val color: Vector4f,
        val selectionMin: Vector2f,
        val selectionMax: Vector2f,
        val thickness: Float
    )

}