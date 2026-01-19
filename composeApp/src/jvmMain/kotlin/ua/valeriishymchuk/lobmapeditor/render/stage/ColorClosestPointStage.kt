package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import org.joml.Matrix4f
import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.render.context.PresetRenderContext
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.ColorClosestPointProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class ColorClosestPointStage(
    ctx: CurrentGL,
    private val mapVertices: FloatArray
): RenderStage {

    private val program: ColorClosestPointProgram = ColorClosestPointProgram(
        ctx,
        loadShaderSource("vcolorclosestpoint"),
                loadShaderSource("fcolorclosestpoint")
    )

    override fun RenderContext<*>.draw0() {
        if (this !is PresetRenderContext) return
        val model = Matrix4f().identity()
        val mvpMatrix = getMvp(model)

        glCtx.glUseProgram(program.program)
        glCtx.glBindVertexArray(program.vao)
        glCtx.glBindVBO(program.vbo)

        program.setUpVBO(glCtx, mapVertices)
        program.setUpVAO(glCtx)
        program.applyUniform(glCtx, ColorClosestPointProgram.Uniform(
            mvpMatrix,
            scenario.units.groupBy {
                it.owner.getValue(scenario.players::get).team
            }.mapValues { entry -> entry.value.map { Vector2f(it.position.x, it.position.y) } }
        ))

        glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)

    }

}