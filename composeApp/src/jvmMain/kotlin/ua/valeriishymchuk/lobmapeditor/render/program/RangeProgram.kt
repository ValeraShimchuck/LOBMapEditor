package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData
import ua.valeriishymchuk.lobmapeditor.render.program.RangeProgram.VertexBuffer.Companion.VERTEX_SIZE_IN_BYTES
import ua.valeriishymchuk.lobmapeditor.render.program.RangeProgram.VertexBuffer.Companion.VERTEX_SIZE_IN_FLOATS
import java.lang.IllegalStateException
import java.util.Arrays

class RangeProgram(
    ctx: GL3,
    vertexSource: String,
    fragmentSource: String
): Program<List<RangeProgram.VertexBuffer>, RangeProgram.Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()
    val projectionLocation = ctx.glGetUniformLocation(program, "uProjection")
    val viewLocation = ctx.glGetUniformLocation(program, "uView")

    override fun setUpVBO(
        ctx: GL3,
        data: List<VertexBuffer>
    ) {


        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)

        val buffer = Buffers.newDirectFloatBuffer(data.size * VertexBuffer.SIZE_IN_BYTES / 4)


        data.forEach { bufferData ->
            fun setUpVertexData(posPoint: Vector2f) {
                val bufPos = buffer.position()
                posPoint.get(buffer)
                buffer.position(buffer.position() + 2)
                bufferData.modelMatrix.getColumn(0, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(1, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(2, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(3, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)

                bufferData.color.get(buffer)
                buffer.position(buffer.position() + 4)


                buffer.put(bufferData.radius)
                buffer.put(bufferData.innerRadius)
                buffer.put(bufferData.startAngle)
                buffer.put(bufferData.endAngle)

                bufferData.center.get(buffer)
                buffer.position(buffer.position() + 2)

                val newPos = buffer.position()
                val array = FloatArray(newPos - bufPos)
                buffer.position(bufPos)
                buffer.get(array)
                buffer.position(newPos)

                if (newPos - bufPos != VERTEX_SIZE_IN_FLOATS)
                    throw IllegalStateException(
                        "Buffer position misalignment." +
                                " Current data size: ${bufPos - newPos}" +
                                " expected $VERTEX_SIZE_IN_FLOATS"
                    )


            }
            for (triangle in 0..1)
                for (pointIndex in 0..2) {
                    setUpVertexData(
                        bufferData.vertexPositions.list[triangle].list[pointIndex]
                    )
                }
        }
        val expectedDataSize = data.size * VertexBuffer.SIZE_IN_BYTES / 4
        if (buffer.position() != expectedDataSize) throw IllegalStateException("Misalignment. Expected $expectedDataSize got ${buffer.position()}")
        buffer.flip()
        ctx.glVBOData(expectedDataSize * 4, buffer)

    }

    override fun setUpVAO(ctx: GL3) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)

        var currentPointerOffset = 0L
        var currentIndex = 0
        val totalAttributes = 11
        fun addAttribute(size: Int) {
            ctx.glVertexAttribPointer(currentIndex, size, GL.GL_FLOAT, false, VERTEX_SIZE_IN_BYTES, currentPointerOffset)
            ctx.glEnableVertexAttribArray(currentIndex)
            currentIndex++
            currentPointerOffset += size * Float.SIZE_BYTES
        }

        // aPos
        addAttribute(2)

        // matrix
        addAttribute(4)
        addAttribute(4)
        addAttribute(4)
        addAttribute(4)

        // color
        addAttribute(4)

        // radius, inner radius, startAngle, endAngle
        addAttribute(1)
        addAttribute(1)
        addAttribute(1)
        addAttribute(1)

        // center
        addAttribute(2)

        if (totalAttributes != currentIndex) throw IllegalStateException("Misalignment. Current index: $currentIndex expected $totalAttributes")
        if (currentPointerOffset.toInt() != VERTEX_SIZE_IN_BYTES) throw IllegalStateException("Misalignment. Current offset: $currentPointerOffset expected $VERTEX_SIZE_IN_BYTES ")


    }

    override fun applyUniform(
        ctx: GL3,
        data: Uniform
    ) {
        ctx.glUseProgram(program)

        ctx.glUniformMatrix4fv(projectionLocation, 1, false, BufferHelper.setupFloatBuffer(16) {
            data.projection.get(this)
            val array = FloatArray(16)
            this.get(array)
        })

        ctx.glUniformMatrix4fv(viewLocation, 1, false, BufferHelper.setupFloatBuffer(16) {
            data.view.get(this)
            val array = FloatArray(16)
            this.get(array)
        })

    }

    data class Uniform(
        val projection: Matrix4f,
        val view: Matrix4f
    )

    data class VertexBuffer(
        val vertexPositions: RectanglePoints,
        val modelMatrix: Matrix4f,
        val color: Vector4f,
        val radius: Float,
        val innerRadius: Float,
        val startAngle: Float,
        val endAngle: Float,
        val center: Vector2f
    ) {
        companion object {
            const val VERTEX_SIZE_IN_FLOATS = 2 + 16 + 4 + 4 + 2
            const val VERTEX_SIZE_IN_BYTES = VERTEX_SIZE_IN_FLOATS * 4
            const val SIZE_IN_BYTES = VERTEX_SIZE_IN_BYTES * 6


        }
    }

}