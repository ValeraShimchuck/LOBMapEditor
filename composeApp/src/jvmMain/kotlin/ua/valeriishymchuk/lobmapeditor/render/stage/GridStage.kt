package ua.valeriishymchuk.lobmapeditor.render.stage

import org.joml.Matrix4f
import org.joml.Vector3f
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.GridProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource
import kotlin.math.max

class GridStage(
    ctx: CurrentGL,
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
        val zoomScale = viewMatrix.getScale(Vector3f()).x

        val thicknessScale = max(((1f / gridContext.gridThickness) / zoomScale), 1f) // old


        program.applyUniform(glCtx, GridProgram.Uniform(
            gridContext.offset,
            gridContext.gridSize,
            gridContext.gridThickness * thicknessScale, // used here
            gridContext.color,
            mvpMatrix
        ))

        glCtx.glDrawArrays(CurrentGL.GL_TRIANGLES, 0, 6)

    }
}