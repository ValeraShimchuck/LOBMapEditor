package ua.valeriishymchuk.lobmapeditor.domain.unit

import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType

enum class GameUnitType(
    val category: GameUnitCategory,
    val id: Int
) {
    // Infantry
    LINE_INFANTRY(GameUnitCategory.INFANTRY, 1),
    GUARDS_INFANTRY(GameUnitCategory.INFANTRY, 4),
    LIGHT_INFANTRY(GameUnitCategory.INFANTRY, 7),
    MILITIA(GameUnitCategory.INFANTRY, 10),
    GRENADIERS(GameUnitCategory.INFANTRY, 14),

    // Cavalry
    DRAGOONS(GameUnitCategory.CAVALRY, 2),
    LANCERS(GameUnitCategory.CAVALRY, 5),
    CUIRASSIERS(GameUnitCategory.CAVALRY, 8),
    HUSSARS(GameUnitCategory.CAVALRY, 11),
    HORSE_ARCHERS(GameUnitCategory.CAVALRY, 13),

    // Artillery

    EIGHT_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 3),
    SIX_LB_HORSE_ARTILLERY(GameUnitCategory.ARTILLERY, 6),
    SIX_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 18),
    SIX_IN_HOWITZER(GameUnitCategory.ARTILLERY, 9),
    TWELVE_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 12),
    FOUR_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 15),
    ROCKETS(GameUnitCategory.ARTILLERY, 19),

    // Skirmishers
    SKIRMISHERS(GameUnitCategory.SKIRMISHERS, 16),
    RIFLES(GameUnitCategory.SKIRMISHERS, 17);


    companion object {

        fun fromId(id: Int): GameUnitType {
            return entries.firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Can't find unit type with id $id")
        }
    }

}

enum class GameUnitCategory {
    INFANTRY,
    SKIRMISHERS,
    ARTILLERY,
    CAVALRY
}