package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2i
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.BlobProcessorProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class BlobTileStage(
    ctx: GL3,
    private val tileMapVertices: FloatArray
): RenderStage {

    private val blobProcessorProgram: BlobProcessorProgram = BlobProcessorProgram(
        ctx,
        loadShaderSource("vblobprocessor"),
        loadShaderSource("fblobprocessor")
    )


    override fun RenderContext.draw0() {
        val model = Matrix4f().identity()
        val mvpMatrix = getMvp(model)

        glCtx.glUseProgram(blobProcessorProgram.program)
        glCtx.glBindVertexArray(blobProcessorProgram.vao)
        glCtx.glBindVBO(blobProcessorProgram.vbo)

        TerrainType.BLOB_TERRAIN.reversed().forEach { terrain ->
            blobProcessorProgram.setUpVBO(glCtx, tileMapVertices)
            blobProcessorProgram.setUpVAO(glCtx)
            blobProcessorProgram.loadMap(glCtx, scenario.map.terrainMap, terrain)
            blobProcessorProgram.applyUniform(
                glCtx, BlobProcessorProgram.Uniform(
                    mvpMatrix,
                    textureStorage.getTerrainTile(terrain),
                    Vector2i(scenario.map.widthTiles, scenario.map.heightTiles),
                    Vector2i(scenario.map.widthPixels, scenario.map.heightPixels),
                    Vector4f(1f),
                )
            )
            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }


        val heightMap = scenario.map.terrainHeight
        val maxTerrain: Int = heightMap.map.flatMap { it }.distinct().max()
        val minTerrain: Int = heightMap.map.flatMap { it }.distinct().min() + 1

        for (heightTile in minTerrain..maxTerrain) {
            blobProcessorProgram.setUpVBO(glCtx, tileMapVertices)
            blobProcessorProgram.setUpVAO(glCtx)
            blobProcessorProgram.loadHeight(glCtx, scenario.map.terrainHeight, heightTile)
            blobProcessorProgram.applyUniform(
                glCtx, BlobProcessorProgram.Uniform(
                    mvpMatrix,
                    textureStorage.heightBlobTexture,
                    Vector2i(scenario.map.widthTiles, scenario.map.heightTiles),
                    Vector2i(scenario.map.widthPixels, scenario.map.heightPixels),
                    Vector4f(1f),
                )
            )
            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }
    }
}