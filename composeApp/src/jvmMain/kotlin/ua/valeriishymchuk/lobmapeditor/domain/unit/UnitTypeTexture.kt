package ua.valeriishymchuk.lobmapeditor.domain.unit

import ua.valeriishymchuk.lobmapeditor.shared.map.generateEnumMap

sealed interface UnitTypeTexture {

    class MaskOnly(maskTexture: String): UnitTypeTexture {
        val maskTexture: String = "units/$maskTexture"
    }

    class MaskAndOverlay(
        maskTexture: String,
        overlayTexture: String
    ): UnitTypeTexture {
        val maskTexture: String = "units/$maskTexture"
        val overlayTexture: String = "units/$overlayTexture"
    }

    data class Formation(
        val map: Map<UnitFormation, MaskAndOverlay>
    ): UnitTypeTexture {
        companion object {
            fun baseIntoFormation(unitKey: String): Formation {
                return Formation(generateEnumMap(UnitFormation::class) {
                    val name = it.name.lowercase()
                    val formationMask = "formation"
                    MaskAndOverlay(
                        "${formationMask}-${name}",
                        "${unitKey}-${name}"
                    )
                })
            }
        }
    }

}