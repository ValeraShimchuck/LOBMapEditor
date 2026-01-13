package ua.valeriishymchuk.lobmapeditor.domain.unit

import org.joml.Vector2f
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit.Companion.UNIT_DIMENSIONS

enum class UnitFormation(val dimensions: Vector2f) {
    MASS(UNIT_DIMENSIONS),
    COLUMN(Vector2f(2f, 1f).mul(16f).mul(0.75f)),
    LINE(Vector2f(.83f, 3.32f).mul(16f).mul(0.75f)),
    SQUARE((Vector2f(1.66f, 1.66f).mul(16f).mul(0.75f)))
}