package ua.valeriishymchuk.lobmapeditor.render.geometry

import org.joml.Vector2f

data class RectanglePoints(
    val firstTriangle: TrianglePoints, // left top
    val secondTriangle: TrianglePoints // bottom right
) {

    val list: List<TrianglePoints> = listOf(firstTriangle, secondTriangle)
    companion object {
        const val SIZE: Int = TrianglePoints.SIZE * 2

        fun fromPoints(firstPoint: Vector2f, secondPoint: Vector2f): RectanglePoints {
            return RectanglePoints(
                TrianglePoints(
                    Vector2f(firstPoint.x, secondPoint.y), // top left
                    Vector2f(secondPoint.x, firstPoint.y), // bottom-right
                    Vector2f(firstPoint.x, firstPoint.y), // bottom-left
                ),
                TrianglePoints(
                    Vector2f(firstPoint.x, secondPoint.y),
                    Vector2f(secondPoint.x, secondPoint.y),
                    Vector2f(secondPoint.x, firstPoint.y),
                )
            )
        }

        val TEXTURE_CORDS: RectanglePoints = fromPoints(Vector2f(0f, 0f), Vector2f(1f, 1f))

    }


}