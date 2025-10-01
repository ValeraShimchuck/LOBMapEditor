package ua.valeriishymchuk.lobmapeditor.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.services.dto.CreateProjectData
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import java.io.File
import javax.imageio.ImageIO

class ProjectsService(override val di: DI) : DIAware {

    private val scenarioIO: ScenarioIOService by instance()

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

    fun saveProjectData(projectRef: ProjectRef, projectData: ProjectData) {
        val projectJson = Json.encodeToString(projectData)
        projectRef.projectFile.writeText(projectJson)
    }

    fun importReference(projectRef: ProjectRef, referenceFile: File) {
        ImageIO.write(ImageIO.read(referenceFile), "PNG", projectRef.referenceFile)
    }

    fun clearReference(projectRef: ProjectRef) {
        projectRef.referenceFile.delete()
    }

    suspend fun createProject(dto: CreateProjectData): ProjectRef = withContext(Dispatchers.IO) {
        val dir = requireNotNull(dto.dir) { "CreateProjectData.dir must be provided" }
        val ref = ProjectRef(dir.path)

        val projectJson = Json.encodeToString(ProjectData(dto.name))
        File(dir, PROJECT_FILE_NAME).writeText(projectJson)

        scenarioIO.save(
            GameScenario.Preset(
                GameScenario.CommonData(
                    name = dto.name,
                    description = "Map created by LobMapEditor",
                    map = Terrain.ofCells(dto.widthPx / GameConstants.TILE_SIZE, dto.heightPx / GameConstants.TILE_SIZE),
                    objectives = emptyList(),
                    triggers = emptyList()
                ),
                units = emptyList(),
                players = listOf(
                    Player(PlayerTeam.RED, 500),
                    Player(PlayerTeam.BLUE, 500),
                ),
            ),
            File(dir, "map.json")
        )

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
