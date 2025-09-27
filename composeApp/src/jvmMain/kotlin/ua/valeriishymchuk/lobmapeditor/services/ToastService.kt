package ua.valeriishymchuk.lobmapeditor.services

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ToastService(override val di: DI) : DIAware {
    var toasts: MutableStateFlow<List<@Composable ColumnScope.() -> Unit>> = MutableStateFlow(emptyList())
        private set;

    fun toast(time: Duration = 5.seconds, content: @Composable ColumnScope.() -> Unit) {
        toasts.value += content
        CoroutineScope(Dispatchers.Default).launch {
            delay(time)
            if(!toasts.value.contains(content))return@launch
            toasts.value -= content
        }
    }



}