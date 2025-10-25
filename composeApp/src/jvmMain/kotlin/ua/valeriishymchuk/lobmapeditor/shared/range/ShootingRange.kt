package ua.valeriishymchuk.lobmapeditor.shared.range

import androidx.compose.ui.graphics.Color

data class ShootingRange(
    val angle: Float, // In degrees
    // ranges, first element the least priority and the last one have the most priority in rendering
    val ranges: List<Pair<Color, Int>>
) {

    class RangeContext {
        val list: MutableList<Pair<Color, Int>> = arrayListOf()

        fun closeRange(range: Int) {
            list.add(CLOSE_RANGE_COLOR to range)
        }

        fun defaultRange(range: Int) {
            list.add(DEFAULT_RANGE_COLOR to range)
        }

    }

    companion object {
        val DEFAULT_RANGE_COLOR = Color(1f, 1f, 1f)
        val CLOSE_RANGE_COLOR = Color(0.918f, 0.667f, 0.173f, 1.0f)



        fun getRanges(angle: Float, provider: RangeContext.() -> Unit): ShootingRange {
            val ctx = RangeContext()
            ctx.provider()
            return ShootingRange(angle, ctx.list.reversed())
        }

    }

}