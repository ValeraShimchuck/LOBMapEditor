package ua.valeriishymchuk.lobmapeditor.services

import org.kodein.di.DI
import org.kodein.di.bindSingleton

val servicesModule by DI.Module {
    bindSingleton { ProjectsService(di) }
}