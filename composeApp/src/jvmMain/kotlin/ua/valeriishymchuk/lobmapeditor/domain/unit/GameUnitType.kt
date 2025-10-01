package ua.valeriishymchuk.lobmapeditor.domain.unit

import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType

enum class GameUnitType(
    val category: GameUnitCategory,
    val id: Int,
    maskTexture: String,
    overlayTexture: String? = null
) {
    // Infantry
    LINE_INFANTRY(GameUnitCategory.INFANTRY, 1, "infantry", "infantry1"),
    GUARDS_INFANTRY(GameUnitCategory.INFANTRY, 4, "infantry", "guards"),
    LIGHT_INFANTRY(GameUnitCategory.INFANTRY, 7, "infantry", "chasseurs"),
    MILITIA(GameUnitCategory.INFANTRY, 10, "militia", "militia1"),
    GRENADIERS(GameUnitCategory.INFANTRY, 14, "infantry", "grenadiers"),

    // Cavalry
    DRAGOONS(GameUnitCategory.CAVALRY, 2, "cavalry", "dragoons"),
    LANCERS(GameUnitCategory.CAVALRY, 5, "cavalry", "lancers"),
    CUIRASSIERS(GameUnitCategory.CAVALRY, 8, "cavalry", "cuirassiers"),
    HUSSARS(GameUnitCategory.CAVALRY, 11, "cavalry", "cavalry1"),
    HORSE_ARCHERS(GameUnitCategory.CAVALRY, 13, "cavalry", "horse-archers"),

    // Artillery

    EIGHT_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 3, "artillery"),
    SIX_LB_HORSE_ARTILLERY(GameUnitCategory.ARTILLERY, 6, "horse-artillery", "horse-artillery1"),
    SIX_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 18, "6lb-artillery"),
    SIX_IN_HOWITZER(GameUnitCategory.ARTILLERY, 9, "howitzers"),
    TWELVE_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 12, "heavy-artillery"),
    FOUR_LB_FOOT_ARTILLERY(GameUnitCategory.ARTILLERY, 15, "4lb-artillery"),
    ROCKETS(GameUnitCategory.ARTILLERY, 19, "rockets", "rockets1"),
    TEN_LB_LICORNE(GameUnitCategory.ARTILLERY, 24, "10lb-licorne"),
    EIGHTEEN_LB_LICORNE(GameUnitCategory.ARTILLERY, 25, "18lb-licorne"),

    // Skirmishers
    SKIRMISHERS(GameUnitCategory.SKIRMISHERS, 16, "skirmishers", "skirmishers1"),
    RIFLES(GameUnitCategory.SKIRMISHERS, 17, "skirmishers", "rifles");

    val maskTexture: String = "units/$maskTexture"
    val overlayTexture: String? = overlayTexture?.let { textureLoc -> "units/$textureLoc" }

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