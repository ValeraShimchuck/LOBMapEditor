package ua.valeriishymchuk.lobmapeditor.render.geometry

import org.joml.Vector2f

data class TrianglePoints(
    val point1: Vector2f,
    val point2: Vector2f,
    val point3: Vector2f
) {

    val list: List<Vector2f> = listOf(point1, point2, point3)

    companion object {
        const val SIZE: Int = 4 * 2 * 3
    }
}