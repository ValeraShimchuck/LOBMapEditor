package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef

fun setupProjectScopeDiModule(ref: ProjectRef) = DI.Module("project scope module") {
    bindEagerSingleton {
        EditorService(
            GameScenario.Preset(
                GameScenario.CommonData(
                    "test",
                    "description",
                    Terrain.ofCells().apply {
//                        val random = Random(12313)
//                        for (x in 0..<widthTiles)
//                            for (y in 0..<widthTiles) {
//                                terrainMap.set(x, y, TerrainType.GRASS)
////                                if (random.nextDouble() > 0.7) terrainMap.set(x, y, TerrainType.ROAD)
//                            }
                        terrainMap.set(10, 10, TerrainType.FOREST)


                    },
                    emptyList(),
                    emptyList()
                ),
                emptyList(),
                listOf(
                    Player(PlayerTeam.RED),
                    Player(PlayerTeam.BLUE)
                )
            )
        )
    }
    bindSingleton { ToolService(di) }
    bindInstance<ProjectRef> { ref }
    bindEagerSingleton<ProjectData> {
        runBlocking { directDI.instance<ProjectsService>().loadProject(ref) }
    }
}