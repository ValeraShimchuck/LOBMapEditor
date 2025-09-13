package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2i
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.OverlayTileProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class OverlayTileStage(
    ctx: GL3,
    private val tileMapVertices: FloatArray
): RenderStage {

    private val overlayTileProgram = OverlayTileProgram(
        ctx,
        loadShaderSource("voverlaytile"),
        loadShaderSource("foverlaytile")
    )

    override fun RenderContext.draw0() {
        val model = Matrix4f().identity()
        val mvpMatrix = getMvp(model)
        glCtx.glUseProgram(overlayTileProgram.program)
        glCtx.glBindVertexArray(overlayTileProgram.vao)
        glCtx.glBindVBO(overlayTileProgram.vbo)

        TerrainType.entries.sortedBy { it.dominance }.filter { it.overlay != null }.forEach { terrain ->
            val overlay = terrain.overlay!!
            overlayTileProgram.setUpVBO(glCtx, tileMapVertices)
            overlayTileProgram.setUpVAO(glCtx)
            overlayTileProgram.loadMap(glCtx, scenario.map.terrainMap, terrain)
            overlayTileProgram.applyUniform(
                glCtx, OverlayTileProgram.Uniform(
                    mvpMatrix,
                    textureStorage.getTerrainOverlay(terrain),
                    Vector2i(scenario.map.widthTiles, scenario.map.heightTiles),
                    Vector2i(scenario.map.widthPixels, scenario.map.heightPixels),
                    Vector4f(1f),
                    overlay
                )
            )
            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }
    }
}