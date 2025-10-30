package ua.valeriishymchuk.lobmapeditor.domain.player

data class Player(
    val team: PlayerTeam,
    val ammo: Int,
    val baseAmmo: Int
)