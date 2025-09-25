package ua.valeriishymchuk.lobmapeditor.services

import com.google.gson.Gson
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton

val servicesModule by DI.Module {
    bindSingleton { ProjectsService(di) }
    bindInstance { Gson() }
    bindEagerSingleton { ScenarioIOService(di) }

    bindEagerSingleton { ErrorService(di) }
    bindEagerSingleton { ToastService(di) }
}
