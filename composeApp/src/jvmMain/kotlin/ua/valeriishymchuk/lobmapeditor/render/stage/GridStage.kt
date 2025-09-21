package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector3f
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.GridProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource
import kotlin.math.max

class GridStage(
    ctx: GL3,
    private val mapVertices: FloatArray
) : RenderStage {

    private val program: GridProgram = GridProgram(
        ctx,
        loadShaderSource("vgrid"),
        loadShaderSource("fgrid")
    )

    override fun RenderContext.draw0() {
        val model = Matrix4f().identity()
        val mvpMatrix = getMvp(model)

        glCtx.glUseProgram(program.program)
        glCtx.glBindVertexArray(program.vao)
        glCtx.glBindVBO(program.vbo)

        program.setUpVBO(glCtx, mapVertices)
        program.setUpVAO(glCtx)

        val thicknessScale = max((2f / viewMatrix.getScale(Vector3f()).x), 1f)


        program.applyUniform(glCtx, GridProgram.Uniform(
            gridContext.offset,
            gridContext.gridSize,
            gridContext.gridThickness * thicknessScale,
            gridContext.color,
            mvpMatrix
        ))

        glCtx.glDrawArrays(GL3.GL_TRIANGLES, 0, 6)

    }
}