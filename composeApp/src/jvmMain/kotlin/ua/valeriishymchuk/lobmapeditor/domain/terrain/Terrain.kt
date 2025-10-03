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

    init {
        if (terrainMap.sizeX != terrainHeight.sizeX) throw IllegalStateException("Terrain map should match height")
        if (terrainMap.sizeY != terrainHeight.sizeY) throw IllegalStateException("Terrain map should match height")
    }

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

            println("Saving map data: $sizeX $sizeY first array size: ${terrainMap.map.size}")

            // Serialize terrain types - convert from column-major to row-major for JSON
            add("terrains", JsonArray().apply {
                // For each row (y)
                for (y in 0 until sizeX) {
                    add(JsonArray().apply {
                        // For each column (x) in this row
                        for (x in 0 until sizeY) {
                            // Access terrainMap data in column-major order: [x][y]
                            val terrain = terrainMap.map[x][y]
//                            val terrain = terrainMap.map[y][x]
                            add(terrain.id) // or however you get the serialized ID
                        }
                    })
                }
            })

            // Serialize height map - convert from column-major to row-major for JSON
            add("heightMap", JsonArray().apply {
                // For each row (y)
                for (y in 0 until sizeX) {
                    add(JsonArray().apply {
                        // For each column (x) in this row
                        for (x in 0 until sizeY) {
                            // Access height data in column-major order: [x][y]
                            add(terrainHeight.map[x][y])
//                            add(terrainHeight.map[y][x])
                        }
                    })
                }
            })
        }
    }

    companion object {


        fun deserialize(json: JsonObject): Terrain {
            // Extract dimensions in pixels
            // Extract dimensions in pixels
            val widthPixels = json.getAsJsonPrimitive("width").asInt
            val heightPixels = json.getAsJsonPrimitive("height").asInt

            // Calculate dimensions in cells
            val cellsX = widthPixels / GameConstants.TILE_SIZE
            val cellsY = heightPixels / GameConstants.TILE_SIZE



            // Deserialize terrain types - column-major order
            val terrainsArray = json.getAsJsonArray("terrains")
            val terrainMap2D = Array(cellsX) { x ->
                try {
                    return@Array Array(cellsY) { y ->
                        val row = terrainsArray[x].asJsonArray // y-th row
                        val terrainId = row[y].asInt // x-th element in the row
                        TerrainType.fromId(terrainId)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    return@Array Array(cellsY) { y -> TerrainType.DEFAULT }
                }

            }

            // Deserialize height map - also column-major order
            val heightArray = json.getAsJsonArray("heightMap")
            val heightMap2D = Array(cellsX) { x ->
                try {
                    return@Array Array(cellsY) { y ->
                        val row = heightArray[x].asJsonArray // y-th row
                        row[y].asInt // x-th element in the row
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    return@Array Array(cellsY) { y -> 0 }
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
                ArrayMap2d(Array<Array<Int>>(pixelSizeX / GameConstants.TILE_SIZE) {
                    Array<Int>(pixelSizeY / GameConstants.TILE_SIZE) {
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
            val array = Array<Array<TerrainType>>(widthCells) {
                Array<TerrainType>(heightCells) {
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
