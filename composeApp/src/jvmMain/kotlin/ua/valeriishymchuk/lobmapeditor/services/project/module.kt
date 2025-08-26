package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.runBlocking
import org.kodein.di.*
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.random.Random

fun setupProjectScopeDiModule(ref: ProjectRef) = DI.Module("project scope module") {
    bindEagerSingleton {
        EditorService(
            di,
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
                (0..<100).map {
                    val random = Random(it)
                    val owner = Reference<Int, Player>(it % 2)
                    val unitType = GameUnitType.entries[it % GameUnitType.entries.size]
//                    val x = random.nextDouble(0.0, 1504.0).toFloat()
//                    val y = random.nextDouble(0.0, 1312.0).toFloat()
                    val x = 1504f / 50 * (it % 50) + 20
                    val y = 1312f / 2 * (it / 50) + 50
                    val rotation = random.nextFloat() * Math.PI.toFloat() * 2
                    GameUnit(
                        null,
                        owner,
                        Position(x, y),
                        rotation,
                        unitType
                    )
                },
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