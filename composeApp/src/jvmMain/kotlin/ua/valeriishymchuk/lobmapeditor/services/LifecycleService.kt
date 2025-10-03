package ua.valeriishymchuk.lobmapeditor.services

import org.kodein.di.DI
import org.kodein.di.DIAware

class LifecycleService(override val di: DI) : DIAware {

    var onClose: () -> Unit = {}


}