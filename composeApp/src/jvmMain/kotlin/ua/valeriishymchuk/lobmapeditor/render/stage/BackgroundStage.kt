package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.BackgroundProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class BackgroundStage(
    ctx: GL3
): RenderStage {

    companion object {
        private val BACKGROUND_VERTICES = floatArrayOf(

            // clip space vertices           // supposed to be texCords, but not actually used
            -1f, -1f, 0f, 0f,
            1f, -1f,  1f, 0f,
            1f, 1f,   1f, 1f,


            -1f, -1f, 0f, 0f,
            1f, 1f,   1f, 1f,
            -1f, 1f,  0f, 1f
        )
    }


    private val backgroundProgram = BackgroundProgram(
        ctx,
        loadShaderSource("vbackground"),
        loadShaderSource("fbackground")
    ).also {
        it.setUpVBO(ctx, BACKGROUND_VERTICES)
        it.setUpVAO(ctx)
    }

    override fun RenderContext.draw0() {
        glCtx.glUseProgram(backgroundProgram.program)
        glCtx.glBindVertexArray(backgroundProgram.vao)
        glCtx.glBindVBO(backgroundProgram.vbo)
        val viewProjectionMatrix = projectionMatrix.mul(viewMatrix, Matrix4f())
        val invertedMatrix = viewProjectionMatrix.invert(Matrix4f())
        backgroundProgram.applyUniform(
            glCtx, BackgroundProgram.Uniform(
                textureStorage.backgroundImage,
                invertedMatrix
            )
        )
        glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
    }
}