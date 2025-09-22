package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.TextureProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class ReferenceOverlayStage(
    ctx: GL3,
    tileMapVertices: FloatArray
) : RenderStage {

    private val program = TextureProgram(
        ctx,
        loadShaderSource("vtexture"),
        loadShaderSource("ftexture")
    )

    private fun applyYFlip(number: Float): Float {
        return 1f - number
    }

    private val textureVertices = floatArrayOf(
        tileMapVertices[0], tileMapVertices[1], 0f, applyYFlip(0f),
        tileMapVertices[2], tileMapVertices[3], 1f, applyYFlip(1f),
        tileMapVertices[4], tileMapVertices[5], 0f, applyYFlip(1f),


        tileMapVertices[6], tileMapVertices[7],   0f, applyYFlip(0f),
        tileMapVertices[8], tileMapVertices[9],   1f, applyYFlip(0f),
        tileMapVertices[10], tileMapVertices[11], 1f, applyYFlip(1f),
    )

    override fun RenderContext.draw0() {
        val textureId = textureStorage.refenceOverlayTexture
        if (textureId < 0) return
        glCtx.glUseProgram(program.program)
        glCtx.glBindVertexArray(program.vao)
        glCtx.glBindVBO(program.vbo)

//            .rotationZ(Math.toRadians(90.0).toFloat())

        val mvp = getMvp(Matrix4f().identity())

        program.setUpVBO(glCtx, textureVertices)
        program.setUpVAO(glCtx)

        program.applyUniform(glCtx, TextureProgram.Uniform(
            mvp,
            overlayReferenceContext.positionMatrix,
            overlayReferenceContext.colorTint,
            textureId
        ))

        glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)

    }
}