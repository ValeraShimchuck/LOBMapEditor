package ua.valeriishymchuk.lobmapeditor.domain.terrain

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.jogamp.common.nio.Buffers
import ua.valeriishymchuk.lobmapeditor.domain.terrain.HeightMap.BlobHeightCachedRenderData
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainMap.BlobCachedRenderData
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.dimension.ArrayMap2d
import java.nio.IntBuffer
import java.util.concurrent.ConcurrentHashMap

data class Terrain(
    val terrainMap: TerrainMap,
    val terrainHeight: HeightMap
) {

    init {
        if (terrainMap.sizeX != terrainHeight.sizeX) throw IllegalStateException("Terrain map should match height")
        if (terrainMap.sizeY != terrainHeight.sizeY) throw IllegalStateException("Terrain map should match height")
    }

    val widthPixels: Int get() = widthTiles * GameConstants.TILE_SIZE
    val heightPixels: Int get() = heightTiles * GameConstants.TILE_SIZE

    val widthTiles: Int get() = terrainHeight.sizeX
    val heightTiles: Int get() = terrainHeight.sizeY

    fun resize(newPixelX: Int, newPixelY: Int): Terrain {
        val newTerrain = ofPixels(newPixelX, newPixelY)
        terrainMap.forEach { pos, terrain ->
            newTerrain.terrainMap.set(pos.x, pos.y, terrain)
        }
        terrainHeight.forEach { pos, height ->
            newTerrain.terrainHeight.set(pos.x, pos.y, height)
        }
        return newTerrain
    }

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

        const val MAX_TERRAIN_HEIGHT = 7;
        const val MAX_TERRAIN_MAP_X = 3200 * 10;
        const val MIN_TERRAIN_MAP_X = 992;
        const val MAX_TERRAIN_MAP_Y = 2512 * 10;
        const val MIN_TERRAIN_MAP_Y = 896;

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
                terrainHeight = HeightMap(heightMap2D)
            )
        }

        fun ofPixels(pixelSizeX: Int = 1504, pixelSizeY: Int = 1312): Terrain {
            return Terrain(
                TerrainMap.ofPixels(pixelSizeX, pixelSizeY),
                HeightMap(Array<Array<Int>>(pixelSizeX / GameConstants.TILE_SIZE) {
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

class HeightMap(
    map: Array<Array<Int>>
) : ArrayMap2d<Int>(map) {

    data class BlobHeightCachedRenderData(
        val buffer: IntBuffer
    )

    private var cachedMaxHeight: Int? = null
    private var cachedMinHeight: Int? = null

    val maxHeight: Int get() {
        var height = cachedMaxHeight
        if (height == null) {
            height = map.flatMap { it }.distinct().max()
            cachedMaxHeight = height
        }
        return height
    }

    val minHeight: Int get() {
        var height = cachedMinHeight
        if (height == null) {
            height = map.flatMap { it }.distinct().min()
            cachedMinHeight = height
        }
        return height
    }

    private val cachedBlobRenderHeights = ConcurrentHashMap<Int, BlobHeightCachedRenderData>()

    fun getHeightBlobMap(height: Int): BlobHeightCachedRenderData {
        return cachedBlobRenderHeights.computeIfAbsent(height) { _ ->
            val buffer = Buffers.newDirectIntBuffer(sizeX * sizeY)
            map.flatMap { it }.forEach {
                if (height <= it) {
                    buffer.put(1)
                } else buffer.put(0)
            }
            buffer.flip()
            BlobHeightCachedRenderData(buffer)
        }
    }

    override fun set(x: Int, y: Int, value: Int): Int? {
        val oldHeight = super.set(x, y, value) ?: return null
        cachedBlobRenderHeights.clear()
        cachedMaxHeight = null
        cachedMinHeight = null
        return oldHeight
    }


}

class TerrainMap(
    map: Array<Array<TerrainType>>
) : ArrayMap2d<TerrainType>(map) {

    private val cachedTerrainRenderTypes = ConcurrentHashMap<TerrainType, TerrainCachedRenderData>()
    private val cachedBlobRenderTypes = ConcurrentHashMap<TerrainType, BlobCachedRenderData>()
    private val cachedOverlayRenderTypes = ConcurrentHashMap<TerrainType, OverlayCachedRenderData>()

    data class TerrainCachedRenderData(
        val buffer: IntBuffer,
        val shouldRender: Boolean
    )

    data class BlobCachedRenderData(
        val buffer: IntBuffer
    )

    data class OverlayCachedRenderData(
        val buffer: IntBuffer
    )

    fun getOverlayRenderMap(type: TerrainType): OverlayCachedRenderData {
        return cachedOverlayRenderTypes.computeIfAbsent(type) { _ ->
            val buffer = Buffers.newDirectIntBuffer(sizeX * sizeY)
            map.flatMap { it }.forEach {
                if (type == it) {
                    buffer.put(1)
                } else buffer.put(0)
            }
            buffer.flip()
            OverlayCachedRenderData(buffer)
        }
    }

    fun getBlobRenderMap(type: TerrainType): BlobCachedRenderData {
        return cachedBlobRenderTypes.computeIfAbsent(type) { _ ->
            val buffer = Buffers.newDirectIntBuffer(sizeX * sizeY)

            map.flatMap { it }.forEach {
                if (type == it) {
                    buffer.put(1)
                    return@forEach
                }
                if (it == TerrainType.BRIDGE && (type == TerrainType.ROAD || type == TerrainType.ROAD_WINTER || type == TerrainType.SUNKEN_ROAD)) {
                    buffer.put(2)
                    return@forEach
                }
                if (type == TerrainType.BRIDGE && (it == TerrainType.ROAD || it == TerrainType.ROAD_WINTER || it == TerrainType.SUNKEN_ROAD)) {
                    buffer.put(2)
                    return@forEach
                }
                if (it == TerrainType.SUNKEN_ROAD && type == TerrainType.ROAD || it == TerrainType.ROAD && type == TerrainType.SUNKEN_ROAD ) {
                    buffer.put(2)
                    return@forEach
                }
                buffer.put(0)
            }

            buffer.flip()

            BlobCachedRenderData(buffer)
        }
    }

    fun getRenderMap(type: TerrainType): TerrainCachedRenderData {
        return cachedTerrainRenderTypes.computeIfAbsent(type) { _ ->
            var hasSomethingToRender = false

            val buffer = Buffers.newDirectIntBuffer(sizeX * sizeY)
            map.flatMap { it }.forEach {
                if (type == it) {
                    hasSomethingToRender = true
                    buffer.put(1)
                    return@forEach
                }
                if (it.isFarm) {
                    hasSomethingToRender = true
                    buffer.put(2)
                    return@forEach
                }
                buffer.put(0)
            }
            buffer.flip()
            TerrainCachedRenderData(
                buffer,
                hasSomethingToRender
            )

        }
    }

    override fun set(x: Int, y: Int, value: TerrainType): TerrainType? {
        val oldTerrainType = super.set(x, y, value) ?: return null
//        val isFarm = value.isFarm || oldTerrainType.isFarm
//        cachedTerrainRenderTypes.keys.toSet().forEach { type ->
//            if (isFarm && type.isFarm) cachedTerrainRenderTypes.remove(type)
//            if (value == type || oldTerrainType == value) cachedTerrainRenderTypes.remove(type)
//        }
//        val roads = setOf(TerrainType.ROAD, TerrainType.ROAD_WINTER, TerrainType.SUNKEN_ROAD)
//        val isRoad = roads.contains(value) || roads.contains(oldTerrainType)
//        cachedBlobRenderTypes.keys.toSet().forEach { type ->
//            if (isRoad && roads.contains(type)) cachedBlobRenderTypes.remove(type)
//            if (value == type || oldTerrainType == value) cachedBlobRenderTypes.remove(type)
//        }
//        cachedOverlayRenderTypes.keys.forEach { type ->
//            if (value == type || oldTerrainType == value) cachedOverlayRenderTypes.remove(type)
//        }

        cachedTerrainRenderTypes.clear()
        cachedBlobRenderTypes.clear()
        cachedOverlayRenderTypes.clear()

        return oldTerrainType
    }

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
