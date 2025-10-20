package ua.valeriishymchuk.lobmapeditor.services.dto

import io.konform.validation.Validation
import io.konform.validation.constraints.*
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
                minimum(992)
                maximum(3200 * 10)
            }

            CreateProjectData::heightPx {
                minimum(896)
                maximum(2512 * 10)
            }
        }

    }
}

