package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.GL
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData
import java.nio.FloatBuffer

class ManyColorProgram(
    ctx: CurrentGL,
    vertexSource: String,
    fragmentSource: String
): Program<List<ManyColorProgram.BufferData>, ManyColorProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val projectionLocation = ctx.glGetUniformLocation(program, "uProjection")
    val viewLocation = ctx.glGetUniformLocation(program, "uView")

    override fun setUpVBO(ctx: CurrentGL, data: List<BufferData>) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        val buffer = Buffers.newDirectFloatBuffer(data.size * BufferData.SIZE / 4)
        data.forEach { bufferData ->
            fun setUpVertexData(posPoint: Vector2f) {
                posPoint.get(buffer)
                buffer.position(buffer.position() + 2)
                bufferData.color.get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(0, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(1, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(2, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(3, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)

            }
            for (triangle in 0..1)
                for (pointIndex in 0..2) {
                    setUpVertexData(
                        bufferData.vertexPositions.list[triangle].list[pointIndex]
                    )
                }
        }
        buffer.flip()
        ctx.glVBOData(data.size * BufferData.SIZE, buffer)
    }

    override fun setUpVAO(ctx: CurrentGL) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 0.toLong())
        ctx.glEnableVertexAttribArray(0)

        ctx.glVertexAttribPointer(1, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 2L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(1)


        ctx.glVertexAttribPointer(2, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 6L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(2)


        ctx.glVertexAttribPointer(3, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 10L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(3)


        ctx.glVertexAttribPointer(4, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 14L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(4)


        ctx.glVertexAttribPointer(5, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 18L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(5)
    }

    override fun applyUniform(
        ctx: CurrentGL,
        data: Uniform
    ) {
        ctx.glUseProgram(program)
//        println("Setting view projection: $viewProjectionLocation")
        ctx.glUniformMatrix4fv(projectionLocation, 1, false, BufferHelper.setupFloatBuffer(16) {
            data.projection.get(this)
        })

        ctx.glUniformMatrix4fv(viewLocation, 1, false, BufferHelper.setupFloatBuffer(16) {
            data.view.get(this)
        })
    }





    data class BufferData(
        val vertexPositions: RectanglePoints,
        val color: Vector4f,
        val modelMatrix: Matrix4f
    ) {
        companion object {
            const val VERTEX_SIZE = (2 + 4 + 4 * 4) * 4
            const val MODEL_MATRIX_SIZE = 4 * 4 * 4 // bytes in float * floats in vec4 * amount of vec4 in matrix4
            const val SIZE: Int = RectanglePoints.SIZE + 4 * 4 * 6 + MODEL_MATRIX_SIZE * 6
        }
    }

    data class Uniform(
        val projection: Matrix4f,
        val view: Matrix4f,
    ) {


    }

}
