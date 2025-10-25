package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.toVector2f
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.RangeProgram
import ua.valeriishymchuk.lobmapeditor.render.program.SpriteProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class RangeStage(
    ctx: GL3
) : RenderStage {

    private val rangeProgram = RangeProgram(
        ctx,
        loadShaderSource("vrange"),
        loadShaderSource("frange")
    )

    override fun RenderContext.draw0() {
        glCtx.glUseProgram(rangeProgram.program)
        glCtx.glBindVertexArray(rangeProgram.vao)
        glCtx.glBindVBO(rangeProgram.vbo)



        val rangesToRender: List<GameUnit> = selectedUnits.filter { it.type.shootingRange != null }.toList()

        val vbo: List<RangeProgram.VertexBuffer> = rangesToRender.flatMap { unit ->
            val positionMatrix = Matrix4f()
            positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
            positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)

            val range = unit.type.shootingRange!!

            val shootingAngleRadians = Math.toRadians(range.angle)

            range.ranges.map { (color, radius) ->
                val adjustedRadius = radius + 8f
                RangeProgram.VertexBuffer(
                    RectanglePoints.fromPoints(
                        Vector2f().sub(Vector2f(adjustedRadius)),
                        Vector2f().add(Vector2f(adjustedRadius))
                    ),
                    positionMatrix,
                    Vector4f(color.red, color.green, color.blue, color.alpha),
                    adjustedRadius,
                    adjustedRadius.toFloat() - 2,
                    Math.PI_f * 2 -shootingAngleRadians / 2,
                    shootingAngleRadians / 2,
//                    unit.position.toVector2f()
                    Vector2f(0f, 0f)

                )
            }
        }


        rangeProgram.setUpVAO(glCtx)
        rangeProgram.setUpVBO(glCtx, vbo)

        rangeProgram.applyUniform(glCtx, RangeProgram.Uniform(
            projectionMatrix,
            viewMatrix
        ))

        glCtx.glDrawArrays(GL.GL_TRIANGLES, 0, 6 * vbo.size)

    }
}