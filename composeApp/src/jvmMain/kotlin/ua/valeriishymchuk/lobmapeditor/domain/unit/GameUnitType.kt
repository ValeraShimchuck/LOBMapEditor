package ua.valeriishymchuk.lobmapeditor.domain.unit

import ua.valeriishymchuk.lobmapeditor.shared.map.generateEnumMap
import ua.valeriishymchuk.lobmapeditor.shared.range.ShootingRange

// 90f
val INFANTRY_SHOOTING_RANGE = ShootingRange.Ranges.getRanges {
    closeRange(24)
    closeRange(44)
    closeRange(64)
    closeRange(90)
}

val INFANTRY_FORMATION_RANGES = ShootingRange.Formation(
    generateEnumMap(UnitFormation::class) {
        when (it) {
            UnitFormation.MASS -> 90f
            UnitFormation.COLUMN -> 90f
            UnitFormation.LINE -> 100f
            UnitFormation.SQUARE -> 360f
        }
    },
    INFANTRY_SHOOTING_RANGE
)

const val ARTILLERY_ANGLE = 90f

val SIX_LB_CANNON_RANGE = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
    closeRange(69)
    closeRange(120)
    defaultRange(220)
    defaultRange(400)
})

enum class GameUnitType(
    val category: GameUnitCategory,
    val id: Int,
    val texture: UnitTypeTexture,
    val shootingRange: ShootingRange? = null,
    val defaultHealth: Int,
    val defaultOrganization: Int,
    val defaultStamina: Int?
) {
    // Infantry
    LINE_INFANTRY(
        GameUnitCategory.INFANTRY,
        1,
        texture = UnitTypeTexture.Formation.baseIntoFormation("infantry"),
        shootingRange = INFANTRY_FORMATION_RANGES,
        defaultHealth = 800,
        defaultOrganization = 650,
        defaultStamina = 1700
    ),
    LIGHT_INFANTRY(
        GameUnitCategory.INFANTRY,
        7,
        texture = UnitTypeTexture.Formation.baseIntoFormation("chasseurs"),
        shootingRange = INFANTRY_FORMATION_RANGES,
        defaultHealth = 800,
        defaultOrganization = 700,
        defaultStamina = 1950
    ),
    GRENADIERS(
        GameUnitCategory.INFANTRY,
        14,
        texture = UnitTypeTexture.Formation.baseIntoFormation("grenadiers"),
        shootingRange = INFANTRY_FORMATION_RANGES,
        defaultHealth = 800,
        defaultOrganization = 725,
        defaultStamina = 1800
    ),
    GUARDS_INFANTRY(
        GameUnitCategory.INFANTRY,
        4,
        texture = UnitTypeTexture.Formation.baseIntoFormation("guards"),
        shootingRange = INFANTRY_FORMATION_RANGES,
        defaultHealth = 1000,
        defaultOrganization = 775,
        defaultStamina = 1900
    ),
    MILITIA(
        GameUnitCategory.INFANTRY,
        10,
        texture = UnitTypeTexture.Formation(
            generateEnumMap(UnitFormation::class) {
                val name = it.name.lowercase()
                UnitTypeTexture.MaskAndOverlay(
                    "militia-mask-$name",
                    "militia-$name"
                )
            }
        ),
        shootingRange = INFANTRY_FORMATION_RANGES,
        defaultHealth = 600,
        defaultOrganization = 600,
        defaultStamina = 1300
    ),

    // Cavalry
    HUSSARS(
        GameUnitCategory.CAVALRY,
        11,
        texture = UnitTypeTexture.MaskAndOverlay("cavalry", "cavalry1"),
        defaultHealth = 400,
        defaultOrganization = 775,
        defaultStamina = 1800
    ),
    LANCERS(
        GameUnitCategory.CAVALRY,
        5,
        texture = UnitTypeTexture.MaskAndOverlay("cavalry", "lancers"),
        defaultHealth = 500,
        defaultOrganization = 900,
        defaultStamina = 1700
    ),
    DRAGOONS(
        GameUnitCategory.CAVALRY,
        2,
        texture = UnitTypeTexture.MaskAndOverlay("cavalry", "dragoons"),
        defaultHealth = 550,
        defaultOrganization = 825,
        defaultStamina = 1500
    ),
    CUIRASSIERS(
        GameUnitCategory.CAVALRY,
        8,
        texture = UnitTypeTexture.MaskAndOverlay("cavalry", "cuirassiers"),
        defaultHealth = 600,
        defaultOrganization = 900,
        defaultStamina = 1400
    ),
    HORSE_ARCHERS(
        GameUnitCategory.CAVALRY,
        13,
        texture = UnitTypeTexture.MaskAndOverlay("cavalry", "horse-archers"),
        shootingRange = ShootingRange.Default(360f, ShootingRange.Ranges.getRanges {
            closeRange(20)
            closeRange(40)
            closeRange(60)
        }),
        defaultHealth = 400,
        defaultOrganization = 750,
        defaultStamina = 1650
    ),

    // Artillery

    FOUR_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        15,
        texture = UnitTypeTexture.MaskOnly("4lb-artillery"),
        shootingRange = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
            closeRange(69)
            closeRange(110)
            defaultRange(200)
            defaultRange(375)
        }),
        defaultHealth = 800,
        defaultOrganization = 300,
        defaultStamina = null
    ),
    SIX_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        18,
        UnitTypeTexture.MaskOnly("6lb-artillery"),
        shootingRange = SIX_LB_CANNON_RANGE,
        defaultHealth = 800,
        defaultOrganization = 300,
        defaultStamina = null
    ),
    SIX_LB_HORSE_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        6,
        UnitTypeTexture.MaskAndOverlay("horse-artillery", "horse-artillery1"),
        shootingRange = SIX_LB_CANNON_RANGE,
        defaultHealth = 800,
        defaultOrganization = 300,
        defaultStamina = null
    ),
    EIGHT_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        3,
        UnitTypeTexture.MaskOnly("artillery"),
        shootingRange = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
            closeRange(69)
            closeRange(135)
            defaultRange(240)
            defaultRange(435)
        }),
        defaultHealth = 800,
        defaultOrganization = 300,
        defaultStamina = null
    ),
    TEN_LB_LICORNE(
        GameUnitCategory.ARTILLERY,
        24,
        UnitTypeTexture.MaskOnly("10lb-licorne"),
        shootingRange = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
            closeRange(69)
            closeRange(105)
            defaultRange(250)
            licorneRange(425)
        }),
        defaultHealth = 600,
        defaultOrganization = 300,
        defaultStamina = null
    ),
    TWELVE_LB_FOOT_ARTILLERY(
        GameUnitCategory.ARTILLERY,
        12,
        UnitTypeTexture.MaskOnly("heavy-artillery"),
        shootingRange = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
            closeRange(69)
            closeRange(145)
            defaultRange(250)
            defaultRange(500)
        }),
        defaultHealth = 800,
        defaultOrganization = 300,
        defaultStamina = null
    ),

    EIGHTEEN_LB_LICORNE(
        GameUnitCategory.ARTILLERY,
        25,
        UnitTypeTexture.MaskOnly("18lb-licorne"),
        shootingRange = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
            closeRange(69)
            closeRange(110)
            defaultRange(310)
            licorneRange(550)
        }),
        defaultHealth = 600,
        defaultOrganization = 300,
        defaultStamina = null
    ),
    SIX_IN_HOWITZER(
        GameUnitCategory.ARTILLERY,
        9,
        UnitTypeTexture.MaskOnly("howitzers"),
        shootingRange = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
            closeRange(69)
            defaultRange(200)
            defaultRange(340)
        }),
        defaultHealth = 600,
        defaultOrganization = 300,
        defaultStamina = null
    ),
    ROCKETS(
        GameUnitCategory.ARTILLERY,
        19,
        UnitTypeTexture.MaskAndOverlay(
            "rockets",
            "rockets1"
        ),
        shootingRange = ShootingRange.Default(ARTILLERY_ANGLE, ShootingRange.Ranges.getRanges {
            closeRange(150)
            closeRange(270)
            closeRange(450)
        }),
        defaultHealth = 600,
        defaultOrganization = 300,
        defaultStamina = null
    ),

    // Skirmishers
    SKIRMISHERS(
        GameUnitCategory.SKIRMISHERS,
        16,
        UnitTypeTexture.MaskAndOverlay(
            "skirmishers",
            "skirmishers1"
        ),
        shootingRange = ShootingRange.Default(360f, ShootingRange.Ranges.getRanges {
            closeRange(35)
            closeRange(65)
            closeRange(90)
        }),
        defaultHealth = 150,
        defaultOrganization = 700,
        defaultStamina = 2100
    ),
    RIFLES(
        GameUnitCategory.SKIRMISHERS,
        17,
        UnitTypeTexture.MaskAndOverlay(
            "skirmishers",
            "rifles"
        ),
        shootingRange = ShootingRange.Default(360f, ShootingRange.Ranges.getRanges {
//            closeRange(45) // probably a bug, but I'll comment it until it resolved
            closeRange(65)
            closeRange(90)
        }),
        defaultHealth = 150,
        defaultOrganization = 700,
        defaultStamina = 2250
    );

//    val maskTexture: String = "units/$maskTexture"
//    val overlayTexture: String? = overlayTexture?.let { textureLoc -> "units/$textureLoc" }

    val hasFormation: Boolean = texture is UnitTypeTexture.Formation

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