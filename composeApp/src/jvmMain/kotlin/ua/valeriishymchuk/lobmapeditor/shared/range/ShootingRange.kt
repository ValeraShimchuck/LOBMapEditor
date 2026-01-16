package ua.valeriishymchuk.lobmapeditor.shared.range

import androidx.compose.ui.graphics.Color
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitFormation
import ua.valeriishymchuk.lobmapeditor.shared.range.ShootingRange

sealed interface ShootingRange {

    val ranges: Ranges

    companion object {
        val DEFAULT_RANGE_COLOR = Color(1f, 1f, 1f)
        val CLOSE_RANGE_COLOR = Color(0.918f, 0.667f, 0.173f, 1.0f)
        val LICORNE_RANGE_COLOR = Color(.36f, 1f, .55f, 1.0f)
    }

    data class Ranges(val ranges: List<Pair<Color, Int>>) {
        class Context {
            val list: MutableList<Pair<Color, Int>> = arrayListOf()

            fun closeRange(range: Int) {
                list.add(CLOSE_RANGE_COLOR to range)
            }

            fun defaultRange(range: Int) {
                list.add(DEFAULT_RANGE_COLOR to range)
            }

            fun licorneRange(range: Int) {
                list.add(LICORNE_RANGE_COLOR to range)
            }

        }

        companion object {
            fun getRanges(provider: Context.() -> Unit): Ranges {
                val ctx = Context()
                ctx.provider()
                return Ranges(ctx.list.reversed())
            }
        }
    }

    data class Default(
        val angle: Float, // In degrees
        // ranges, first element the least priority and the last one have the most priority in rendering
        override val ranges: Ranges
    ): ShootingRange

    data class Formation(
        val angles: Map<UnitFormation, Float>, // angles are in degrees
        override val ranges: Ranges
    ): ShootingRange

}

