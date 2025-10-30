package ua.valeriishymchuk.lobmapeditor.services.dto

import io.konform.validation.Validation
import io.konform.validation.constraints.*
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import java.io.File

data class CreateProjectData(
    val name: String = "",
    val dir: File? = null,
    val widthPx: Int = 992,
    val heightPx: Int = 896
) {
    companion object {
        val validator = Validation {
            CreateProjectData::name {
                notBlank()
                maxLength(48)
            }

            CreateProjectData::dir required {}

            CreateProjectData::widthPx {
                minimum(Terrain.MIN_TERRAIN_MAP_X)
                maximum(Terrain.MAX_TERRAIN_MAP_X)
            }

            CreateProjectData::heightPx {
                minimum(Terrain.MIN_TERRAIN_MAP_Y)
                maximum(Terrain.MAX_TERRAIN_MAP_Y)
            }
        }

    }
}

