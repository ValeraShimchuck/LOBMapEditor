package ua.valeriishymchuk.lobmapeditor.domain.player

import androidx.compose.ui.graphics.Color

enum class PlayerTeam(
    val id: Int,
    val color: Color,
    val displayName: String
) {
    BLUE(1, Color(0, 0, 245), "Blue"),
    RED(2, Color(230, 0, 0), "Red");

    companion object {
        fun fromId(id: Int): PlayerTeam {
            return entries.firstOrNull { it.id == id }
                ?: throw NoSuchElementException("Can't find team with id $id")
        }
    }

}