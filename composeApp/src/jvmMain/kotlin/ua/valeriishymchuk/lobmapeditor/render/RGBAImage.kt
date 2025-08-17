package ua.valeriishymchuk.lobmapeditor.render

import java.nio.Buffer

data class RGBAImage(
    val image: Buffer,
    val width: Int,
    val height: Int
)