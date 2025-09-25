package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import java.io.File

fun <T: GameScenario<T>> setupProjectScopeDiModule(
    ref: ProjectRef
) = DI.Module("project scope module") {
    bindEagerSingleton {
        EditorService<GameScenario.Preset>(
            di
        )
    }
    bindSingleton { ToolService(di) }
    bindInstance<ProjectRef> { ref }
    bindEagerSingleton<ProjectData> {
        runBlocking { directDI.instance<ProjectsService>().loadProject(ref) }
    }
    bindSingleton { TextureStorage() }
}