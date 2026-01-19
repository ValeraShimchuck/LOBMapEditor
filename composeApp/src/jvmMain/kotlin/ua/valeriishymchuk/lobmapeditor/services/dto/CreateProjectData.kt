package ua.valeriishymchuk.lobmapeditor.services.dto

import io.konform.validation.Validation
import io.konform.validation.constraints.*
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import java.io.File

data class CreateProjectData(
    val name: String = "",
    val dir: File? = null,
    val widthTiles: Int = Terrain.MIN_TERRAIN_TILES_MAP_X,
    val heightTiles: Int = Terrain.MIN_TERRAIN_TILES_MAP_Y,
    val isHybrid: Boolean = false
) {
    companion object {
        val validator = Validation {
            CreateProjectData::name {
                notBlank()
                maxLength(48)
            }

            CreateProjectData::dir required {}

            CreateProjectData::widthTiles {
                minimum(Terrain.MIN_TERRAIN_TILES_MAP_X)
                maximum(Terrain.MAX_TERRAIN_TILES_MAP_X)
            }

            CreateProjectData::heightTiles {
                minimum(Terrain.MIN_TERRAIN_TILES_MAP_Y)
                maximum(Terrain.MAX_TERRAIN_TILES_MAP_Y)
            }
        }

    }
}

