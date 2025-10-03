package ua.valeriishymchuk.lobmapeditor.shared.dimension

import java.util.Arrays


open class ArrayMap2d<T>(
    protected val _map: Array<Array<T>>
) {
//
    val originalMap: List<List<T>> get() {
        return _map.map { it.toList() }
    }


    val map: List<List<T>> get() {
        val original = _map.map { it.toList() }
        return if (original.isEmpty()) emptyList() else {
            List(original[0].size) { col ->
                List(original.size) { row ->
                    original[row][col]
                }
            }
        }
    }

    val sizeX = _map.size
    val sizeY = let {
        if (sizeX == 0) return@let 0
        if (_map.map { it.size }.distinct().size != 1)
            throw IllegalStateException(
                "All arrays in _map should be the same in size, but got ${_map.map { it.size }.distinct()}"
            )
        _map.first().size
    }
//    val sizeY = _map.size
//    val sizeX = let {
//        if (sizeY == 0) return@let 0
//        if (_map.map { it.size }.distinct().size != 1)
//            throw IllegalStateException(
//                "All arrays in _map should be the same in size, but got ${_map.map { it.size }.distinct()}"
//            )
//        _map.first().size
//    }


    fun set(x: Int, y: Int, value: T): T? {
        val row = _map.getOrNull(x) ?: return null
        if (y >= row.size ) return null
        val prevValue = row[y]
        if (prevValue == value) return null
        row[y] = value
        return prevValue
    }

    fun get(x: Int, y: Int): T? {
        val row = _map.getOrNull(x) ?: return null
        if (y >= row.size ) return null
        return row.getOrNull(y)
    }
//    fun set(x: Int, y: Int, value: T): T? {
//        val row = _map.getOrNull(y) ?: return null
//        if (x >= row.size ) return null
//        val prevValue = row[x]
//        if (prevValue == value) return null
//        row[x] = value
//        return prevValue
//    }
//
//    fun get(x: Int, y: Int): T? {
//        val row = _map.getOrNull(y) ?: return null
//        if (x >= row.size ) return null
//        return row.getOrNull(x)
//    }

    open fun clone(): ArrayMap2d<T> {
        return ArrayMap2d(_map.clone().also {
            it.forEachIndexed { index, row ->
                it[index] = row.clone()
            }
        })
    }

    override fun hashCode(): Int {
        return _map.contentDeepHashCode()
    }
    

}