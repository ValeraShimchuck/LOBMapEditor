package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.HybridEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.HybridToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.PresetToolService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef

fun <T : GameScenario<T>> setupProjectScopeDiModule(
    ref: ProjectRef,
    isHybrid: Boolean
) = DI.Module("project scope module") {
    bindEagerSingleton {
        if (isHybrid) {
            HybridEditorService(di)
        } else {
            PresetEditorService(
                di
            )
        }

    }
    bindSingleton {
        if (isHybrid) {
            HybridToolService(di)
        } else {
            PresetToolService(di)
        }
    }
    bindInstance<ProjectRef> { ref }
    bindEagerSingleton<ProjectData> {
        runBlocking { directDI.instance<ProjectsService>().loadProject(ref) }
    }
    bindSingleton { TextureStorage() }
}