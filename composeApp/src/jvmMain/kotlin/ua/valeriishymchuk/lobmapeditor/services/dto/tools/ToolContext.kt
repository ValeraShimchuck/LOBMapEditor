package ua.valeriishymchuk.lobmapeditor.services.dto.tools

import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType

class ToolContext(
    val height: Int,
    val terrain: TerrainType
)