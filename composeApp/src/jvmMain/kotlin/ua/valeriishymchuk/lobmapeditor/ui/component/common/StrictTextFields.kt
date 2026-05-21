package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@OptIn(ExperimentalJewelApi::class)
@Composable
fun <T> IntTextField(
    rememberSubject: T,
    subjectToInt: (T) -> Int,
    onUpdate: (Int) -> Unit,
    onFocusLoss: () -> Unit = {},
    adjustValue: (Int) -> Int = { it }, // for coercing usually
    leadingText: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    StrictTextField(
        rememberSubject,
        {
            subjectToInt(rememberSubject).toString()
        },
        { str ->
            str.replace(Regex("[^0-9]"), "").let { str ->
                val value = str.toIntOrNull() ?: return@let str
                val adjustedValue = adjustValue(value)
                if (adjustedValue == value) return@let str
                adjustedValue.toString()
            }
        },
        {
            onUpdate(it.toIntOrNull() ?: 0)
        },
        onFocusLoss = onFocusLoss,
        leadingText = leadingText,
        placeholder = placeholder,
        modifier
    )
}

@OptIn(ExperimentalJewelApi::class)
@Composable
fun <T> FloatTextField(
    rememberSubject: T,
    subjectToFloat: (T) -> Float?,
    onUpdate: (Float) -> Unit,
    onFocusLoss: () -> Unit = {},
    adjustValue: (Float) -> Float = { it }, // for coercing usually
    leadingText: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    StrictTextField(
        rememberSubject,
        {
            subjectToFloat(rememberSubject)?.toString() ?: ""
        },
        { str ->
            str.replace(Regex("[^0-9.]"), "").let { str ->
                val value = str.toFloatOrNull() ?: return@let str
                val adjustedValue = adjustValue(value)
                if (adjustedValue == value) return@let str
                adjustedValue.toString()
            }
        },
        {
            onUpdate(it.toFloatOrNull() ?: 0f)
        },
        onFocusLoss = onFocusLoss,
        leadingText = leadingText,
        placeholder = placeholder,
        modifier
    )
}

@OptIn(ExperimentalJewelApi::class)
@Composable
fun <T> StrictTextField(
    rememberSubject: T,
    subjectToText: (T) -> String,
    filterText: (String) -> String,
    onUpdate: (String) -> Unit,
    onFocusLoss: () -> Unit = {},
    leadingText: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var textValue by remember(rememberSubject) {
        val text = subjectToText(rememberSubject)
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = TextRange(text.length)
            )
        )
    }
    TextField(
        value = textValue,
        modifier = modifier.onFocusChanged { focus ->
            if (!focus.isFocused) {
                onFocusLoss()
            }
        },
        onValueChange = { newValue ->
            // Simply update the state with the complete new value
            textValue = newValue
            textValue = textValue.copy(
                text = filterText(
                    newValue.text
                )
            )

            onUpdate(textValue.text)
        },

        leadingIcon = leadingText?.let { str ->
            {
                Row {
                    Text(str, color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        },
        placeholder = {
            if (placeholder != null) {
                Text(placeholder)
            }

        }
    )
}