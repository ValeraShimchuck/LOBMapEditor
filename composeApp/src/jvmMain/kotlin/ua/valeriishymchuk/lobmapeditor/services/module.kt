package ua.valeriishymchuk.lobmapeditor.services

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType

val servicesModule by DI.Module {
    bindSingleton { ProjectsService(di) }
}
