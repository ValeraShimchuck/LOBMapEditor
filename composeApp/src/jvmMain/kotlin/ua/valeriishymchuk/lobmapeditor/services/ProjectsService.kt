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
            Json.decodeFromString<List<ProjectRef>>(File(PROJECTS_INDEX_PATH).readText())
                .associateWith { Json.decodeFromString<ProjectData>(it.projectFile.readText()) }
        }.getOrDefault(emptyMap())
    }

    suspend fun addExistingProject(ref: ProjectRef) = withContext(Dispatchers.IO) {
        val existingProjects = loadProjects()
        if (existingProjects.containsKey(ref)) return@withContext

        val updated = listOf(ref) + existingProjects.keys
        File(PROJECTS_INDEX_PATH).writeText(Json.encodeToString(updated))
    }

    suspend fun removeProject(ref: ProjectRef) = withContext(Dispatchers.IO) {
        val existingProjects = loadProjects()
        if (!existingProjects.containsKey(ref)) return@withContext

        val updated = existingProjects.keys.filter { it != ref }
        File(PROJECTS_INDEX_PATH).writeText(Json.encodeToString(updated))
    }

    suspend fun createProject(dto: CreateProjectData): ProjectRef = withContext(Dispatchers.IO) {
        val dir = requireNotNull(dto.dir) { "CreateProjectData.dir must be provided" }
        val ref = ProjectRef(dir.path)

        val projectJson = Json.encodeToString(ProjectData(dto.name))
        File(dir, PROJECT_FILE_NAME).writeText(projectJson)

        addExistingProject(ref)
        return@withContext ref
    }

    suspend fun loadProject(ref: ProjectRef): ProjectData = withContext(Dispatchers.IO) {
        return@withContext Json.decodeFromString(ref.projectFile.readText())
    }

    private companion object {
        private const val PROJECTS_INDEX_PATH = "./projects.json"
        private const val PROJECT_FILE_NAME = "project.json"
    }
}
