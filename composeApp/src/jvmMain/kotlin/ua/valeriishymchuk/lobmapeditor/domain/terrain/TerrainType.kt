package ua.valeriishymchuk.lobmapeditor.domain.terrain

import org.joml.Vector2i
import org.joml.Vector4f


enum class TerrainType(
    val id: Int,
    val textureLocation: String,
    val dominance: Int,
    val mainTerrain: TerrainType? = null,
    val overlay: OverlayInfo? = null,
    val isFarm: Boolean = false,
    val colorTint: Vector4f = Vector4f(1f),
) {
    GRASS(
        0, "terrain/grass",
        dominance = 21, colorTint = Vector4f(0.8f, 0.85f, 0.8f, 1f)
    ),
    FOREST(1, "terrain/forest-ground", dominance = 19, overlay = OverlayInfo(
        "trees",
        randomRange = 0.4f,
        2
    ),),
    BUILDING(2, "terrain/city1", dominance = 15, overlay = OverlayInfo(
        "buildings",
    )),
    ROAD(3, "blending/road", dominance = 10, GRASS),
    SHALLOW_WATER(4, "terrain/shallow-water", dominance = 4),
    DEEP_WATER(5, "terrain/deep-water", dominance = 5),
    CLIFF(6, "blending/cliff", dominance = 1, GRASS),
    BRIDGE(7, "blending/bridge", dominance = 3, SHALLOW_WATER),
    SNOW(8, "terrain/snow", dominance = 22),
    DIRT(9, "terrain/dirt", dominance = 17),
    SAND(10, "terrain/sand", dominance = 14),
    FARM(11, "terrain/farm", dominance = 6, isFarm = true),
    CITY(12, BUILDING.textureLocation, dominance = 16, overlay = OverlayInfo(
        "city",
        scale = 1.31f,
        offset = -0.15f,
        elementSize = Vector2i(42)
    )),
    FOREST_WINTER(13, "terrain/dirt-winter", dominance = 18,overlay = OverlayInfo(
        "tree-winter",
        randomRange = 0.4f,
        2,
    ), colorTint = Vector4f(1.35f, 1.2f, 1.2f, 1f)
    ),
    CLIFF_WINTER(14, "blending/cliff-winter", dominance = 0, SNOW),
    ROAD_WINTER(15, "blending/road-winter", dominance = 9, SNOW),
    ICE(16, "terrain/ice", dominance = 2),
    FARM_UNPLANTED(17, "terrain/farm-unplanted", dominance = 7, isFarm = true),
    FARM_GROWING(18, "terrain/farm-growing", dominance = 8, isFarm = true),
    MUD(19, "terrain/mud", dominance = 20),
    SUNKEN_ROAD(20, "blending/sunken-road", dominance = 11, GRASS),
    TRENCH(21, "blending/trench", dominance = 12, DIRT),
    REDOUBT(22, "blending/redoubt", dominance = 13, DIRT);

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

        val MAIN_TERRAIN: List<TerrainType> = TerrainType.entries.filter { it.isTerrain }.sortedByDescending { it.dominance }
        val BLOB_TERRAIN: List<TerrainType> = TerrainType.entries.filter { it.isBlob }.sortedByDescending { it.dominance }

        fun fromId(id: Int): TerrainType {
            return TerrainType.entries.firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Can't find terrain with id $id")
        }
    }
}