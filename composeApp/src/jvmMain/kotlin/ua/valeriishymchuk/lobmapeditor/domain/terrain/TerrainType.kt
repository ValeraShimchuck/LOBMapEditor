package ua.valeriishymchuk.lobmapeditor.domain.terrain

enum class TerrainType(val id: Int) {
    GRASS(0),
    FOREST(1),
    BUILDING(2),
    ROAD(3),
    SHALLOW_WATER(4),
    DEEP_WATER(5),
    CLIFF(6),
    BRIDGE(7),
    SNOW(8),
    DIRT(9),
    SAND(10),
    FARM(11),
    CITY(12),
    FOREST_WINTER(13),
    CLIFF_WINTER(14),
    ROAD_WINTER(15),
    ICE(16),
    FARM_UNPLANTED(17),
    FARM_GROWING(18),
    MUD(19),
    SUNKEN_ROAD(20),
    TRENCH(21),
    REDOUBT(22),;

    companion object {
        val DEFAULT: TerrainType = GRASS

        fun fromId(id: Int): TerrainType {
            return TerrainType.entries.firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Can't find terrain with id $id")
        }
    }
}