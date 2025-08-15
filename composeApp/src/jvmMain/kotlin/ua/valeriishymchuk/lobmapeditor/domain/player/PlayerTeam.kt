package ua.valeriishymchuk.lobmapeditor.domain.player

import androidx.compose.ui.graphics.Color

enum class PlayerTeam(
    val id: Int,
    val color: Color
) {
    BLUE(1, Color(0, 0, 255)),
    RED(2, Color(255, 0, 0))
}