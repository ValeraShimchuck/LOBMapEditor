package ua.valeriishymchuk.lobmapeditor.services.dto

import io.konform.validation.Validation
import io.konform.validation.constraints.*

data class CreateProjectData(
    val name: String = "",
    val path: String = "",
    val widthPx: Int = 992,
    val heightPx: Int = 896
) {
    companion object {
        val validator = Validation<CreateProjectData> {
            CreateProjectData::name {
                notBlank()
                maxLength(48)
            }

            CreateProjectData::path {
                notBlank()
            }

            CreateProjectData::widthPx {
                minimum(992)
                maximum(3200)
            }

            CreateProjectData::heightPx {
                minimum(896)
                maximum(2512)
            }
        }

    }
}

