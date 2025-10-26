package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import org.joml.Matrix4f
import org.joml.Vector2i
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.TileMapProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class TerrainMapStage(
    ctx: CurrentGL,
    private val tileMapVertices: FloatArray
): RenderStage {

    private val tileMapProgram = TileMapProgram(
        ctx,
        loadShaderSource("vtilemap"),
        loadShaderSource("ftilemap")
    )

    override fun RenderContext.draw0() {
        glCtx.glUseProgram(tileMapProgram.program)
        glCtx.glBindVertexArray(tileMapProgram.vao)
        glCtx.glBindVBO(tileMapProgram.vbo)

        val model = Matrix4f().identity()
        val mvpMatrix = getMvp(model)

        TerrainType.entries.sortedBy { it.dominance }.forEach { terrain ->
            tileMapProgram.setUpVBO(glCtx, tileMapVertices)
            tileMapProgram.setUpVAO(glCtx)
            val shouldRender = tileMapProgram.loadMap(glCtx, scenario.map.terrainMap, terrain)
            if (!shouldRender) return@forEach
            val terrainToRender = terrain.mainTerrain ?: terrain
            tileMapProgram.applyUniform(
                glCtx, TileMapProgram.Uniform(
                    mvpMatrix,
                    textureStorage.terrainMaskTexture,
                    textureStorage.farmOverlayTexture,
                    textureStorage.getTerrainTile(terrainToRender),
                    Vector2i(scenario.map.widthTiles, scenario.map.heightTiles),
                    Vector2i(4, 4),
                    Vector2i(scenario.map.widthPixels, scenario.map.heightPixels),
                    terrainToRender.colorTint,
                    windowDimensions
                )
            )
            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }
    }

}