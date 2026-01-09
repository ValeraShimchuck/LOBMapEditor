package ua.valeriishymchuk.lobmapeditor.domain.unit

import ua.valeriishymchuk.lobmapeditor.shared.range.ShootingRange

val INFANTRY_SHOOTING_RANGE = ShootingRange.getRanges(90f) {
    closeRange(20)
    closeRange(45)
    closeRange(72)
}

val SIX_LB_CANNON_RANGE = ShootingRange.getRanges(60f) {
    closeRange(69)
    closeRange(120)
    defaultRange(220)
    defaultRange(400)
}

enum class GameUnitType(
    val category: GameUnitCategory,
    val id: Int,
    maskTexture: String,
    overlayTexture: String? = null,
    val shootingRange: ShootingRange? = null
) {
    // Infantry
    LINE_INFANTRY(
        GameUnitCategory.INFANTRY,
        1,
        "infantry",
        "infantry1",
        shootingRange = INFANTRY_SHOOTING_RANGE
    ),
    LIGHT_INFANTRY(
        GameUnitCategory.INFANTRY,
        7,
        "infantry",
        "chasseurs",
        shootingRange = INFANTRY_SHOOTING_RANGE
    ),
    GRENADIERS(
        GameUnitCategory.INFANTRY,
        14,
        "infantry",
        "grenadiers",
        shootingRange = INFANTRY_SHOOTING_RANGE
    ),
    GUARDS_INFANTRY(GameUnitCategory.INFANTRY, 4, "infantry", "guards"),
    MILITIA(GameUnitCategory.INFANTRY, 10, "militia", "militia1"),
    // Cavalry
    HUSSARS(
        GameUnitCategory.CAVALRY,
        11,
        "cavalry",
        "cavalry1"
    ),
    LANCERS(
        GameUnitCategory.CAVALRY,
        5,
        "cavalry",
        "lancers"
    ),
    DRAGOONS(GameUnitCategory.CAVALRY, 2, "cavalry", "dragoons"),
    CUIRASSIERS(GameUnitCategory.CAVALRY, 8, "cavalry", "cuirassiers"),
    HORSE_ARCHERS(
        GameUnitCategory.CAVALRY,
        13,
        "cavalry",
        "horse-archers",
        shootingRange = ShootingRange.getRanges(300f) {
            closeRange(20)
            closeRange(40)
            closeRange(60)
        }
    ),

    // Artillery

    FOUR_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        15,
        "4lb-artillery",
        shootingRange = ShootingRange.getRanges(60f) {
            closeRange(69)
            closeRange(110)
            defaultRange(200)
            defaultRange(375)
        }
    ),
    SIX_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        18,
        "6lb-artillery",
        shootingRange = SIX_LB_CANNON_RANGE
    ),
    SIX_LB_HORSE_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        6,
        "horse-artillery",
        "horse-artillery1",
        shootingRange = SIX_LB_CANNON_RANGE
    ),
    EIGHT_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        3,
        "artillery",
        shootingRange = ShootingRange.getRanges(60f) {
            closeRange(69)
            closeRange(135)
            defaultRange(240)
            defaultRange(435)
        }
    ),
    TEN_LB_LICORNE(
        GameUnitCategory.ARTILLERY,
        24,
        "10lb-licorne",
        shootingRange = ShootingRange.getRanges(60f) {
            closeRange(69)
            closeRange(105)
            defaultRange(250)
            defaultRange(425)
        }
    ),
    TWELVE_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        12,
        "heavy-artillery",
        shootingRange = ShootingRange.getRanges(60f) {
            closeRange(69)
            closeRange(145)
            defaultRange(250)
            defaultRange(500)
        }
    ),

    EIGHTEEN_LB_LICORNE(
        GameUnitCategory.ARTILLERY,
        25,
        "18lb-licorne",
        shootingRange = ShootingRange.getRanges(60f) {
            closeRange(69)
            closeRange(110)
            defaultRange(310)
            defaultRange(550)
        }
    ),
    SIX_IN_HOWITZER(
        GameUnitCategory.ARTILLERY,
        9,
        "howitzers",
        shootingRange = ShootingRange.getRanges(60f) {
            closeRange(69)
            defaultRange(200)
            defaultRange(340)

        }
        ),
    ROCKETS(
        GameUnitCategory.ARTILLERY,
        19,
        "rockets",
        "rockets1",
        shootingRange = ShootingRange.getRanges(60f) {
            closeRange(150)
            closeRange(270)
            closeRange(450)
        }
    ),

    // Skirmishers
    SKIRMISHERS(
        GameUnitCategory.SKIRMISHERS,
        16,
        "skirmishers",
        "skirmishers1",
        shootingRange = ShootingRange.getRanges(350f) {
            closeRange(45)
            closeRange(75)
            closeRange(120)
        }
    ),
    RIFLES(
        GameUnitCategory.SKIRMISHERS,
        17,
        "skirmishers",
        "rifles",
        shootingRange = ShootingRange.getRanges(350f) {
            closeRange(45)
            closeRange(75)
            closeRange(120)
        }
    );

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
    INFANTRY, SKIRMISHERS, ARTILLERY, CAVALRY
}