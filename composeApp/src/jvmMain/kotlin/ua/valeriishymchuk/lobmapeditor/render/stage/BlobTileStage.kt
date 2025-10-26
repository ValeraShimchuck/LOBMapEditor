package ua.valeriishymchuk.lobmapeditor.render.stage

import com.jogamp.opengl.GL.GL_TRIANGLES
import org.joml.Matrix4f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.BlobProcessorProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource

class BlobTileStage(
    ctx: CurrentGL,
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
        val maxTerrain: Int = heightMap.maxHeight
        val minTerrain: Int = heightMap.minHeight
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
                    BlobProcessorProgram.Uniform.Mask(
                        textureStorage.heightMaskBlobTexture,
                        getTintByHeight(heightTile)
                    )
                )
            )
            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6)
        }
    }

    private fun RenderContext.getTintByHeight(height: Int): Vector4f {
        val maxHeight = Terrain.MAX_TERRAIN_HEIGHT
//        val basicTint = Vector4f(Vector3f(0.1f, 0.2f, 0.1f),0.2f)
//        val maxTint = Vector4f(Vector3f(0.55f, 0.4f, 0.3f), 0.2f)
        val basicTint = debugInfo.firstHeightColor
        val maxTint = debugInfo.secondHeightColor
        val step = 1f / maxHeight
        return basicTint.lerp(maxTint, step * height, Vector4f())
    }

}