package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import com.jogamp.opengl.GL3
import org.joml.Vector2f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.SelectionProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class SelectionStage(
    ctx: GL3
) : RenderStage {

    private val selectionProgram = SelectionProgram(
        ctx,
        loadShaderSource("vselection"),
        loadShaderSource("fselection")
    )

    override fun RenderContext.draw0() {
        if (selection.enabled) {
            glCtx.glUseProgram(selectionProgram.program)
            glCtx.glBindVertexArray(selectionProgram.vao)
            glCtx.glBindVBO(selectionProgram.vbo)

            val min = selection.selectionStart.min(selection.selectionEnd, Vector2f())
            val max = selection.selectionStart.max(selection.selectionEnd, Vector2f())

            selectionProgram.setUpVBO(
                glCtx, floatArrayOf(
                    min.x, max.y,
                    min.x, min.y,
                    max.x, max.y,

                    min.x, min.y,
                    max.x, max.y,
                    max.x, min.y,
                )
            )


            selectionProgram.setUpVAO(glCtx)

            val borderSizePx = 4

            val thickness = Vector2f(
                1f / windowDimensions.x * borderSizePx,
                1f / windowDimensions.y * borderSizePx
            )

            selectionProgram.applyUniform(
                glCtx, SelectionProgram.Uniform(
                    Vector4f(0f, 1f, 0f, 1f),
                    min,
                    max,
                    thickness.y,
                    thickness.x
                )
            )

            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)

        }
    }


}