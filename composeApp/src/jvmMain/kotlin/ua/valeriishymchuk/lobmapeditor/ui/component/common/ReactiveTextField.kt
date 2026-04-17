package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun ReactiveTextField(subject: Any, initialText: String, onChange: (String) -> Unit, placeholder: String? = null, leadingText: String? = null, onFocusLoss: () -> Unit = {}) {
    val fieldState by remember { mutableStateOf(TextFieldState(initialText)) }
    LaunchedEffect(subject) {
        snapshotFlow { fieldState.text.toString() }
            .collect { text ->
                onChange(text)
            }
    }
    TextField(
        fieldState,
        Modifier.fillMaxWidth().onFocusChanged {
            onFocusLoss()
        },
        placeholder = placeholder?.let { str -> { Text(str) } },
        leadingIcon = leadingText?.let { str ->
            {
                Row {
                    Text(str, color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        }
    )
}