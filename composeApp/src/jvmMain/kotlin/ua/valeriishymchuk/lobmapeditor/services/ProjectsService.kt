package ua.valeriishymchuk.lobmapeditor.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import ua.valeriishymchuk.lobmapeditor.services.dto.CreateProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import java.io.File

class ProjectsService(override val di: DI) : DIAware {
    suspend fun loadProjects(): Map<ProjectRef, ProjectData> = withContext(Dispatchers.IO) {
        runCatching {
            Json.decodeFromString<List<ProjectRef>>(File("./projects.json").readText())
                .associateWith { Json.decodeFromString<ProjectData>(File(it.path, "project.json").readText()) }
        }.getOrDefault(emptyMap())
    }

    suspend fun addExistingProject(ref: ProjectRef) {

    }

    suspend fun createProject(dto: CreateProjectData) = withContext(Dispatchers.IO) {

    }

}