package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.context.HybridRenderContext
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.ManyColorProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class DeploymentZoneStage(
    ctx: CurrentGL
) : RenderStage {

    private val program = ManyColorProgram(
        ctx,
        loadShaderSource("vmanycolor"),
        loadShaderSource("fmanycolor")
    )

    override fun RenderContext<*>.draw0() {
        if (this !is HybridRenderContext) return
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


        val vbo: MutableList<ManyColorProgram.BufferData> = arrayListOf()
        val selected = toolService.deploymentZoneTool.selected.value
        this.scenario.deploymentZones.forEachIndexed { index, zone ->
            val color = zone.team.color
            fun addToVbo(
                pos: Vector2f,
                dimensions: Vector2f,
                colorArg: Vector4f = Vector4f(color.red, color.green, color.blue, 0.3f
                ) ) {
                val modelMatrix = Matrix4f()
                modelMatrix.setTranslation(Vector3f(pos.x, pos.y, 0f))

                vbo += ManyColorProgram.BufferData(
                    RectanglePoints.fromPoints(
                        Vector2f(0f),
                        dimensions
                    ), colorArg,
                    modelMatrix
                )
            }

            addToVbo(
                Vector2f(zone.position.x, zone.position.y),
                Vector2f(zone.width, zone.height)
            )

            val lightDeploymentZoneOffset = 80f
            val lightDeploymentZoneWidth = 8f

            addToVbo(
                Vector2f( zone.position.x - lightDeploymentZoneOffset, zone.position.y - lightDeploymentZoneOffset),
                Vector2f(zone.width + lightDeploymentZoneOffset * 2, lightDeploymentZoneWidth)
            )

            addToVbo(
                Vector2f( zone.position.x - lightDeploymentZoneOffset, zone.position.y + zone.height + lightDeploymentZoneOffset),
                Vector2f(zone.width + lightDeploymentZoneOffset * 2, lightDeploymentZoneWidth)
            )

            addToVbo(
                Vector2f( zone.position.x - lightDeploymentZoneOffset, zone.position.y - lightDeploymentZoneOffset + lightDeploymentZoneWidth),
                Vector2f( lightDeploymentZoneWidth, zone.height + lightDeploymentZoneOffset * 2 - lightDeploymentZoneWidth)
            )

            addToVbo(
                Vector2f( zone.position.x + zone.width + lightDeploymentZoneOffset - lightDeploymentZoneWidth, zone.position.y - lightDeploymentZoneOffset+ lightDeploymentZoneWidth),
                Vector2f( lightDeploymentZoneWidth, zone.height + lightDeploymentZoneOffset * 2 - lightDeploymentZoneWidth)
            )

            if (selected?.key == index) {
                val outlineWidth = 2f
                val outlineColor = Vector4f(0f, 0f, 0f, 1f)
                addToVbo(
                    Vector2f( zone.position.x - lightDeploymentZoneOffset, zone.position.y - lightDeploymentZoneOffset),
                    Vector2f(zone.width + lightDeploymentZoneOffset * 2, outlineWidth),
                    outlineColor
                )

                addToVbo(
                    Vector2f( zone.position.x - lightDeploymentZoneOffset, zone.position.y + zone.height + lightDeploymentZoneOffset + lightDeploymentZoneWidth - outlineWidth),
                    Vector2f(zone.width + lightDeploymentZoneOffset * 2, outlineWidth),
                    outlineColor
                )

                addToVbo(
                    Vector2f( zone.position.x - lightDeploymentZoneOffset, zone.position.y - lightDeploymentZoneOffset + outlineWidth),
                    Vector2f( outlineWidth, zone.height + lightDeploymentZoneOffset * 2 + lightDeploymentZoneWidth - outlineWidth),
                    outlineColor
                )

                addToVbo(
                    Vector2f( zone.position.x + zone.width + lightDeploymentZoneOffset - outlineWidth, zone.position.y - lightDeploymentZoneOffset + outlineWidth),
                    Vector2f( outlineWidth, zone.height + lightDeploymentZoneOffset * 2 + lightDeploymentZoneWidth - outlineWidth),
                    outlineColor
                )
            }

        }

        if (!vbo.isEmpty()) {
            program.setUpVBO(glCtx, vbo)
            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

    }
}