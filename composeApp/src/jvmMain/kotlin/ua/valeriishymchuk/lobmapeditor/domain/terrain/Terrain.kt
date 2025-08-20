package ua.valeriishymchuk.lobmapeditor.domain.terrain

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.dimension.ArrayMap2d

data class Terrain (
    val terrainMap: TerrainMap,
    val terrainHeight: ArrayMap2d<Int>
) {

    val widthPixels: Int get() = widthTiles * GameConstants.TILE_SIZE
    val heightPixels: Int get() = heightTiles * GameConstants.TILE_SIZE

    val widthTiles: Int get() = terrainHeight.sizeX
    val heightTiles: Int get() = terrainHeight.sizeY

    fun serialize(): JsonObject {
        return JsonObject().apply {
            val sizeX = terrainHeight.sizeX
            val sizeY = terrainHeight.sizeY
            add("width", JsonPrimitive(sizeX * GameConstants.TILE_SIZE))
            add("height", JsonPrimitive(sizeY * GameConstants.TILE_SIZE))

            add("terrains", JsonArray().apply {
                terrainMap.serialize().forEach { row ->
                    add(JsonArray().apply {
                        row.forEach { serializedTerrain ->
                            add(serializedTerrain)
                        }
                    })
                }
            })

            add("heightMap", JsonArray().apply {
                terrainHeight.map.forEach { row ->
                    add(JsonArray().apply {
                        row.forEach { height ->
                            add(height)
                        }
                    })
                }
            })

        }
    }

    companion object {


        fun deserialize(json: JsonObject): Terrain {
            // Extract dimensions in pixels
            val widthPixels = json.getAsJsonPrimitive("width").asInt
            val heightPixels = json.getAsJsonPrimitive("height").asInt

            // Calculate dimensions in cells
            val cellsX = widthPixels / GameConstants.TILE_SIZE
            val cellsY = heightPixels / GameConstants.TILE_SIZE

            // Deserialize terrain types
            val terrainsArray = json.getAsJsonArray("terrains")
            val terrainMap2D = Array(cellsY) { y ->
                val row = terrainsArray[y].asJsonArray
                Array(cellsX) { x ->
                    val terrainId = row[x].asInt
                    TerrainType.fromId(terrainId)
                }
            }

            // Deserialize height map
            val heightArray = json.getAsJsonArray("heightMap")
            val heightMap2D = Array(cellsY) { y ->
                val row = heightArray[y].asJsonArray
                Array(cellsX) { x ->
                    row[x].asInt
                }
            }

            return Terrain(
                terrainMap = TerrainMap(terrainMap2D),
                terrainHeight = ArrayMap2d(heightMap2D)
            )
        }

        fun ofPixels(pixelSizeX: Int = 1504, pixelSizeY: Int = 1312): Terrain {
            return Terrain(
                TerrainMap.ofPixels(pixelSizeX, pixelSizeY),
                ArrayMap2d(Array<Array<Int>>(pixelSizeY / GameConstants.TILE_SIZE) {
                    Array<Int>(pixelSizeX / GameConstants.TILE_SIZE) {
                        0
                    }
                })
            )
        }

        fun ofCells(sizeX: Int = 1504 / GameConstants.TILE_SIZE, sizeY: Int = 1312 / GameConstants.TILE_SIZE): Terrain {
            return ofPixels(sizeX * GameConstants.TILE_SIZE, sizeY * GameConstants.TILE_SIZE)
        }
    }

}

class TerrainMap (
    map: Array<Array<TerrainType>>
): ArrayMap2d<TerrainType>(map) {

    fun serialize(): Array<Array<Int>> {
        return _map.map {
            it.map { type -> type.id }.toTypedArray()
        }.toTypedArray()
    }

    override fun clone(): TerrainMap {
        return TerrainMap(Array(_map.size) {
            _map[it].clone()
        })
    }


    companion object {

        fun ofCells(widthCells: Int, heightCells: Int): TerrainMap {
            val array = Array<Array<TerrainType>>(heightCells) {
                Array<TerrainType>(widthCells) {
                    TerrainType.DEFAULT
                }
            }
            return TerrainMap(array)
        }

        fun ofPixels(widthPixels: Int, heightPixels: Int): TerrainMap {
            return ofCells(widthPixels / GameConstants.TILE_SIZE, heightPixels / GameConstants.TILE_SIZE)
        }

    }

}
