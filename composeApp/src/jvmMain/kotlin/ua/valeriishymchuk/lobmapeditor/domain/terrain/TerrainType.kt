package ua.valeriishymchuk.lobmapeditor.domain.terrain

import org.joml.Vector2i
import org.joml.Vector4f


enum class TerrainType(
    val id: Int,
    val textureLocation: String,
    val dominance: Float,
    val mainTerrain: TerrainType? = null,
    val overlay: OverlayInfo? = null,
    val isFarm: Boolean = false,
    val colorTint: Vector4f = Vector4f(1f),
    val blobCompatibility: Lazy<List<TerrainType>> = lazy { listOf() }
) {
    GRASS(
        0, "terrain/grass",
        dominance = 21f, colorTint = Vector4f(0.8f, 0.85f, 0.8f, 1f)
    ),
    FOREST(
        1, "terrain/forest-ground", dominance = 19f,
        overlay = OverlayInfo(
            "trees",
            randomRange = 0.4f,
            2
        ),
    ),
    BUILDING(
        2, "terrain/city1", dominance = 15f, overlay = OverlayInfo(
            "buildings",
        )
    ),
    ROAD(3, "blending/road", dominance = 10f, GRASS, blobCompatibility = lazy {
        listOf(
            BRIDGE,
            SUNKEN_ROAD,
            BUILDING,
            CITY,
            SNOW,
            RAILWAY_ROAD
        )
    }),
    SHALLOW_WATER(4, "terrain/shallow-water", dominance = 4f),
    DEEP_WATER(5, "terrain/deep-water", dominance = 5f),
    CLIFF(6, "blending/cliff", dominance = 1f, GRASS),
    BRIDGE(7, "blending/bridge", dominance = 3f, SHALLOW_WATER, blobCompatibility = lazy {
        listOf(
            ROAD,
            ROAD_WINTER,
            SUNKEN_ROAD,
            RAILWAY_ROAD,
            RAILWAY
        )
    }),
    SNOW(8, "terrain/snow", dominance = 22f),
    DIRT(9, "terrain/dirt", dominance = 17f),
    SAND(10, "terrain/sand", dominance = 14f),
    FARM(11, "terrain/farm", dominance = 6f, isFarm = true),
    CITY(
        12, BUILDING.textureLocation, dominance = 16f, overlay = OverlayInfo(
            "city",
            scale = 1.31f,
            offset = -0.15f,
            elementSize = Vector2i(42)
        )
    ),
    FOREST_WINTER(
        13, "terrain/dirt-winter", dominance = 18f, overlay = OverlayInfo(
            "tree-winter",
            randomRange = 0.4f,
            2,
        ), colorTint = Vector4f(1.35f, 1.2f, 1.2f, 1f)
    ),
    CLIFF_WINTER(14, "blending/cliff-winter", dominance = 0f, SNOW),
    ROAD_WINTER(15, "blending/road-winter", dominance = 9f, SNOW, blobCompatibility = lazy {
        listOf(
            BRIDGE,
            BUILDING,
            CITY,
            SNOW
        )
    }),
    ICE(16, "terrain/ice", dominance = 2f),
    FARM_UNPLANTED(17, "terrain/farm-unplanted", dominance = 7f, isFarm = true),
    FARM_GROWING(18, "terrain/farm-growing", dominance = 8f, isFarm = true),
    MUD(19, "terrain/mud", dominance = 20f),
    SUNKEN_ROAD(20, "blending/sunken-road", dominance = 11f, GRASS, blobCompatibility = lazy {
        listOf(
            ROAD,
            BRIDGE
        )
    }),
    TRENCH(21, "blending/trench", dominance = 12f, DIRT),
    REDOUBT(22, "blending/redoubt", dominance = 13f, DIRT),
    CAMP(
        25, BUILDING.textureLocation, dominance = 14.5f, overlay = OverlayInfo(
            "camp"
        )
    ),
    RAILWAY(23, "blending/railway", dominance = 10.5f, GRASS, blobCompatibility = lazy {
        listOf(
            RAILWAY_ROAD,
            BRIDGE
        )
    }),
    RAILWAY_ROAD(24, "blending/railway-road", dominance = 10.3f, GRASS, blobCompatibility = lazy {
        listOf(
            ROAD,
            ROAD_WINTER,
            RAILWAY,
            BRIDGE
        )
    })
    ;

    val isTerrain = textureLocation.startsWith("terrain/")
    val isBlob = textureLocation.startsWith("blending/")

//    val overlay: String? = if (isBlob) null else additionalLocation ?: mainTerrain?.let { textureLocation }


    data class OverlayInfo(
        val textureLocation: String,
        val randomRange: Float = 0f, // maximum allowed offset
        val overlayAmount: Int = 1, // how much overlays will be rendered per tile
        val scale: Float = 1f, // 1 - is 1 tile fit
        val offset: Float = 0f,
        val elementSize: Vector2i = Vector2i(32)
    )

    companion object {
        val DEFAULT: TerrainType = GRASS

        val FARM_BORDERS_LOCATION: String = "tilesets/borderblending/farm-borders"

        val MAIN_TERRAIN: List<TerrainType> =
            TerrainType.entries.filter { it.isTerrain }.sortedByDescending { it.dominance }
        val BLOB_TERRAIN: List<TerrainType> =
            TerrainType.entries.filter { it.isBlob }.sortedByDescending { it.dominance }

        fun fromId(id: Int): TerrainType {
            return TerrainType.entries.firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Can't find terrain with id $id")
        }
    }
}