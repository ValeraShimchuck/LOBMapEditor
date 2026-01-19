package ua.valeriishymchuk.lobmapeditor.render.stage

import androidx.compose.ui.util.lerp
import com.jogamp.opengl.GL.GL_TRIANGLES
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitStatus
import ua.valeriishymchuk.lobmapeditor.render.context.PresetRenderContext
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.ManyColorProgram
import ua.valeriishymchuk.lobmapeditor.render.program.SpriteProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class UnitBarsStage(
    ctx: CurrentGL
) : RenderStage {

    private val program = ManyColorProgram(
        ctx,
        loadShaderSource("vmanycolor"),
        loadShaderSource("fmanycolor")
    )

    override fun RenderContext<*>.draw0() {
        if (this !is PresetRenderContext) return
        glCtx.glUseProgram(program.program)
        glCtx.glBindVertexArray(program.vao)
        glCtx.glBindVBO(program.vbo)


        program.setUpVAO(glCtx)
        program.applyUniform(
            glCtx,
            ManyColorProgram.Uniform(
                projectionMatrix,
                viewMatrix
            )
        )

        val healthBars = this.scenario.units.filter { unit -> unit.health < unit.type.defaultHealth }

        val vbo: MutableList<ManyColorProgram.BufferData> = arrayListOf()

        this.scenario.units.forEach { unit ->
            var positionOffset = 0
            val bgDimensions = Vector2f(16f, 3f)
            val barDimensions = bgDimensions.sub(Vector2f(1.5f), Vector2f())
            val alpha = if (unit.status == UnitStatus.ROUTING) 0.5f else 1f
            if (unit.organization < unit.type.defaultOrganization) {
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y - 13, 0f))
                val progress = unit.organization.toFloat() / unit.type.defaultOrganization
                vbo.add(
                    ManyColorProgram.BufferData(
                        RectanglePoints.fromPoints(
                            bgDimensions.mul(-0.5f, -0.5f, Vector2f()),
                            bgDimensions.mul(0.5f, 0.5f, Vector2f()),
                        ),
                        Vector4f(0f, 0f, 0f, alpha),
                        positionMatrix
                    )
                )
                vbo.add(
                    ManyColorProgram.BufferData(
                        RectanglePoints.fromPoints(
                            barDimensions.mul(-0.5f, -0.5f, Vector2f()),
                            barDimensions.mul(lerp(-0.5f, 0.5f, progress), 0.5f, Vector2f()),
                        ),
                        Vector4f(0f, .74f, 1f, alpha),
                        positionMatrix
                    ),
                )
                positionOffset += 2
            }

            if (unit.health < unit.type.defaultHealth) {
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y - 13 - positionOffset, 0f))
                val progress = unit.health.toFloat() / unit.type.defaultHealth
                vbo.add(
                    ManyColorProgram.BufferData(
                        RectanglePoints.fromPoints(
                            bgDimensions.mul(-0.5f, -0.5f, Vector2f()),
                            bgDimensions.mul(0.5f, 0.5f, Vector2f()),
                        ),
                        Vector4f(0f, 0f, 0f, alpha),
                        positionMatrix
                    )
                )
                vbo.add(
                    ManyColorProgram.BufferData(
                        RectanglePoints.fromPoints(
                            barDimensions.mul(-0.5f, -0.5f, Vector2f()),
                            barDimensions.mul(lerp(-0.5f, 0.5f, progress), 0.5f, Vector2f()),
                        ),
                        Vector4f(0f, 1f, 0f, alpha),
                        positionMatrix
                    ),
                )
            }
            val stamina = unit.stamina
            val defaultStamina = unit.type.defaultStamina
            if (stamina != null && defaultStamina != null && stamina.toFloat() / defaultStamina < .86f) {
                val bgDimensions = Vector2f(2.5f, 5f)
                val barDimensions = bgDimensions.sub(Vector2f(1.5f), Vector2f())
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x-8, unit.position.y - 14, 0f))
                val progress = stamina.toFloat() / defaultStamina
                vbo.add(
                    ManyColorProgram.BufferData(
                        RectanglePoints.fromPoints(
                            bgDimensions.mul(-0.5f, -0.5f, Vector2f()),
                            bgDimensions.mul(0.5f, 0.5f, Vector2f()),
                        ),
                        Vector4f(0f, 0f, 0f, alpha),
                        positionMatrix
                    )
                )
                vbo.add(
                    ManyColorProgram.BufferData(
                        RectanglePoints.fromPoints(
                            barDimensions.mul(-0.5f, lerp(0.5f, -0.5f, progress), Vector2f()),
                            barDimensions.mul(0.5f,  0.5f, Vector2f()),
                        ),
                        Vector4f(.82f, .65f, .25f, alpha),
                        positionMatrix
                    ),
                )
            }
        }


        if (!vbo.isEmpty()) {
            program.setUpVBO(glCtx, vbo)
            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }


    }
}