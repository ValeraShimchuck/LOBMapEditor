package ua.valeriishymchuk.lobmapeditor.domain.terrain

enum class TerrainType(
    val id: Int,
    val textureLocation: String,
    val dominance: Int,
    val mainTerrain: TerrainType? = null,
    val additionalLocation: String? = null,
    val isFarm: Boolean = false) {
    GRASS(0, "terrain/grass", dominance = 21),
    FOREST(1, "terrain/forest-ground", dominance = 19, additionalLocation = "trees"),
    BUILDING(2, "terrain/city1", dominance = 15, additionalLocation = "buildings"),
    ROAD(3, "blending/road", dominance = 10, GRASS),
    SHALLOW_WATER(4, "terrain/shallow-water", dominance = 4),
    DEEP_WATER(5, "terrain/deep-water", dominance = 5),
    CLIFF(6, "blending/cliff", dominance = 1, GRASS),
    BRIDGE(7, "blending/bridge", dominance = 3, SHALLOW_WATER),
    SNOW(8, "terrain/snow", dominance = 22),
    DIRT(9, "terrain/dirt", dominance = 17),
    SAND(10, "terrain/sand", dominance = 14),
    FARM(11, "terrain/farm", dominance = 6, isFarm = true),
    CITY(12, "city", dominance = 16, mainTerrain = BUILDING),
    FOREST_WINTER(13, "tree-winter", dominance = 18, mainTerrain = FOREST),
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