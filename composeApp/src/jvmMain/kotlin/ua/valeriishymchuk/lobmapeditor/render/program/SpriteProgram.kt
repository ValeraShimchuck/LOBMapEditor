package ua.valeriishymchuk.lobmapeditor.render.program

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.helper.BufferHelper
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenBuffer
import ua.valeriishymchuk.lobmapeditor.render.helper.glGenVAO
import ua.valeriishymchuk.lobmapeditor.render.helper.glVBOData
import ua.valeriishymchuk.lobmapeditor.render.program.SpriteProgram.Uniform
import java.nio.FloatBuffer

class SpriteProgram(
    ctx: GL3,
    vertexSource: String,
    fragmentSource: String
): Program<List<SpriteProgram.BufferData>, Uniform> {
    override val program: Int = Program.compileProgram(ctx, vertexSource, fragmentSource)
    override val vao: Int = ctx.glGenVAO()
    override val vbo: Int = ctx.glGenBuffer()

    val projectionLocation = ctx.glGetUniformLocation(program, "uProjection")
    val viewLocation = ctx.glGetUniformLocation(program, "uView")
    val drawMaskLocation = ctx.glGetUniformLocation(program, "uDrawMask")
    val drawOverlayLocation = ctx.glGetUniformLocation(program, "uDrawOverlay")
    val maskColorLocation = ctx.glGetUniformLocation(program, "uMaskColor")
    val maskLocation = ctx.glGetUniformLocation(program, "uMask")
    val overlayLocation = ctx.glGetUniformLocation(program, "uOverlay")

    override fun setUpVBO(ctx: GL3, data: List<BufferData>) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
//        val buffer = BufferHelper.allocateDirectFloatBuffer(data.size * BufferData.SIZE / 4)
        val buffer = Buffers.newDirectFloatBuffer(data.size * BufferData.SIZE / 4)
        data.forEach { bufferData ->
            fun setUpVertexData(posPoint: Vector2f, texCoord: Vector2f) {
                val bufPos = buffer.position()
                posPoint.get(buffer)
                buffer.position(buffer.position() + 2)
                texCoord.get(buffer)
                buffer.position(buffer.position() + 2)
                bufferData.modelMatrix.getColumn(0, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(1, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(2, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                bufferData.modelMatrix.getColumn(3, Vector4f()).get(buffer)
                buffer.position(buffer.position() + 4)
                val newPos = buffer.position()
                val array = FloatArray(newPos - bufPos)
                buffer.position(bufPos)
                buffer.get(array)
                buffer.position(newPos)
            }
            for (triangle in 0..1)
                for (pointIndex in 0..2) {
                    setUpVertexData(
                        bufferData.vertexPositions.list[triangle].list[pointIndex],
                        bufferData.textureCoordinates.list[triangle].list[pointIndex]
                    )
                }
        }
        buffer.flip()
        ctx.glVBOData(data.size * BufferData.SIZE, buffer)
    }

    override fun setUpVAO(ctx: GL3) {
        ctx.glBindVertexArray(vao)
        ctx.glBindVBO(vbo)
        ctx.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 0.toLong())
        ctx.glEnableVertexAttribArray(0)

        ctx.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 2L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(1)


        ctx.glVertexAttribPointer(2, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 4L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(2)


        ctx.glVertexAttribPointer(3, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 8L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(3)


        ctx.glVertexAttribPointer(4, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 12L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(4)


        ctx.glVertexAttribPointer(5, 4, GL.GL_FLOAT, false, BufferData.VERTEX_SIZE, 16L * Float.SIZE_BYTES)
        ctx.glEnableVertexAttribArray(5)
    }

    override fun applyUniform(
        ctx: GL3,
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

        ctx.glUniform1i(drawMaskLocation, if(data.drawMask) 1 else 0)
        ctx.glUniform1i(drawOverlayLocation, if(data.drawOverlay) 1 else 0)
        ctx.glUniform4fv(maskColorLocation, 1, BufferHelper.setupFloatBuffer(4) {
            data.maskColor.get(this)
        })

        ctx.glActiveTexture(GL.GL_TEXTURE0)
        ctx.glBindTexture(GL3.GL_TEXTURE_2D, data.maskTexture)
        ctx.glUniform1i(maskLocation, 0)


        ctx.glActiveTexture(GL.GL_TEXTURE0 + 1)
        ctx.glBindTexture(GL3.GL_TEXTURE_2D, data.overlayTexture)
        ctx.glUniform1i(overlayLocation, 1)
    }

    data class TrianglePoints(
        val point1: Vector2f,
        val point2: Vector2f,
        val point3: Vector2f
    ) {

        val list: List<Vector2f> = listOf(point1, point2, point3)

        companion object {
            const val SIZE: Int = 4 * 2 * 3
        }
    }

    data class RectanglePoints(
        val firstTriangle: TrianglePoints, // left top
        val secondTriangle: TrianglePoints // bottom right
    ) {

        val list: List<TrianglePoints> = listOf(firstTriangle, secondTriangle)
        companion object {
            const val SIZE: Int = TrianglePoints.SIZE * 2

            fun fromPoints(firstPoint: Vector2f, secondPoint: Vector2f): RectanglePoints {
                return RectanglePoints(
                    TrianglePoints(
                        Vector2f(firstPoint.x, secondPoint.y), // top left
                        Vector2f(secondPoint.x, firstPoint.y), // bottom-right
                        Vector2f(firstPoint.x, firstPoint.y), // bottom-left
                    ),
                    TrianglePoints(
                        Vector2f(firstPoint.x, secondPoint.y),
                        Vector2f(secondPoint.x, secondPoint.y),
                        Vector2f(secondPoint.x, firstPoint.y),
                    )
                )
            }

            val TEXTURE_CORDS: RectanglePoints = fromPoints(Vector2f(0f, 0f), Vector2f(1f, 1f))

        }


    }

    data class BufferData(
        val vertexPositions: RectanglePoints,
        val textureCoordinates: RectanglePoints,
        val modelMatrix: Matrix4f
    ) {
        companion object {
            const val VERTEX_SIZE = (2 * 2 + 4 * 4) * 4
            const val MODEL_MATRIX_SIZE = 4 * 4 * 4 // bytes in float * floats in vec4 * amount of vec4 in matrix4
            const val SIZE: Int = RectanglePoints.SIZE * 2 + MODEL_MATRIX_SIZE * 6
        }
    }

    data class Uniform(
        val projection: Matrix4f,
        val view: Matrix4f,
        val drawMask: Boolean,
        val drawOverlay: Boolean,
        val maskColor: Vector4f,
        val maskTexture: Int,
        val overlayTexture: Int
    ) {


    }

}
