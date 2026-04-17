package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.theme.defaultButtonStyle

@Composable
fun BlueButton(text: String, onClick: () -> Unit) {
    DefaultButton(
        onClick = {
            onClick()
        }
    ) {
        Text(text)
    }
}

@Composable
fun RedButton(text: String, onClick: () -> Unit) {
    DefaultButton(
        style = JewelTheme.defaultButtonStyle.let { style ->
            val color = Color(196, 27, 27, 255)
            val color2 = Color(182, 25, 25, 255)
            val color3 = Color(165, 21, 21, 255)
            ButtonStyle(
                colors = ButtonColors(
//                        style.colors.background,
                    Brush.linearGradient(listOf(color, color)),
                    style.colors.backgroundDisabled,
                    Brush.linearGradient(listOf(color, color)),
                    Brush.linearGradient(listOf(color3, color3)),
                    Brush.linearGradient(listOf(color2, color2)),
                    style.colors.content,
                    style.colors.contentDisabled,
                    style.colors.contentFocused,
                    style.colors.contentPressed,
                    style.colors.contentHovered,
                    style.colors.border,
                    style.colors.borderDisabled,
                    style.colors.borderFocused,
                    style.colors.borderPressed,
                    style.colors.borderHovered
                ),
                metrics = style.metrics,
                focusOutlineAlignment = style.focusOutlineAlignment
            )
        },
        onClick = {
            onClick()
        },
    ) {
        Text(text)
    }
}