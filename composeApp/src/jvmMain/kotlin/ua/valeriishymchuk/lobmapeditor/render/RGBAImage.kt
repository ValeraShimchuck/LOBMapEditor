package ua.valeriishymchuk.lobmapeditor.render

import java.nio.Buffer
import java.nio.ByteBuffer

data class RGBAImage(
    val image: ByteBuffer,
    val width: Int,
    val height: Int
)