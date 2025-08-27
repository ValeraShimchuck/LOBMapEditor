package ua.valeriishymchuk.lobmapeditor.services

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware

class ErrorService(override val di: DI) : DIAware {


    data class AppError(
        val severity: Severity,
        val message: String? = null,
        val cause: Throwable? = null,
        val onDismiss: () -> Boolean = { true }
    ) {
        enum class Severity {
            Error, Warning
        }
    }

    val error = MutableStateFlow<AppError?>(null)
}