package ua.valeriishymchuk.lobmapeditor.services

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType

val servicesModule by DI.Module {
    bindSingleton { EditorService(
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
                    terrainMap.set(10, 10, TerrainType.SHALLOW_WATER)
                    terrainMap.set(10, 11, TerrainType.BRIDGE)
                    terrainMap.set(9, 11, TerrainType.ROAD)
                    terrainMap.set(11, 11, TerrainType.ROAD)
                    terrainMap.set(10, 12, TerrainType.SHALLOW_WATER)


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
    ) }
    bindSingleton { ProjectsService(di) }
    bindSingleton { ToolService(di) }
}
