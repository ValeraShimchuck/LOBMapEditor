package ua.valeriishymchuk.lobmapeditor.shared.editor

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ProjectRef(
    val path: String
) {
    val dirFile: File get() = File(path)
    val projectFile: File get() = File(path, "project.json")
}
