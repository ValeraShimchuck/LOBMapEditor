package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import org.joml.Matrix4f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.ColorProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class ColorStage(
    ctx: CurrentGL,
    private val frameVertices: FloatArray,
    private val color: Vector4f
): RenderStage {

    private val colorProgram = ColorProgram(
        ctx,
        loadShaderSource("vcolor"),
        loadShaderSource("fcolor")
    )

    override fun RenderContext.draw0() {
        glCtx.glUseProgram(colorProgram.program)
        val model = Matrix4f().identity()
        val mvpMatrix = getMvp(model)
        glCtx.glBindVertexArray(colorProgram.vao)
        glCtx.glBindVBO(colorProgram.vbo)


        colorProgram.setUpVBO(glCtx, ColorProgram.Data(frameVertices))
        colorProgram.setUpVAO(glCtx)
        colorProgram.applyUniform(
            glCtx, ColorProgram.Uniform(
                color,
                mvpMatrix
            )
        )
        glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
    }
}