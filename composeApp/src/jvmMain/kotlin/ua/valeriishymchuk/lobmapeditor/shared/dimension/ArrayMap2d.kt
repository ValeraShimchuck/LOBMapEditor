package ua.valeriishymchuk.lobmapeditor.shared.dimension


open class ArrayMap2d<T>(
    protected val _map: Array<Array<T>>
) {
    
    val map: List<List<T>> get() {
        return _map.map { it.toList() }
    }

    val sizeY = _map.size
    val sizeX = let {
        if (sizeY == 0) return@let 0
        if (_map.map { it.size }.distinct().size != 1)
            throw IllegalStateException(
                "All arrays in _map should be the same in size, but got ${_map.map { it.size }.distinct()}"
            )
        _map.first().size
    }


    fun set(x: Int, y: Int, value: T): T? {
        val row = _map.getOrNull(y) ?: return null
        if (x >= row.size ) return null
        val prevValue = row[x]
        row[x] = value
        return prevValue
    }

    fun get(x: Int, y: Int): T? {
        val row = _map.getOrNull(y) ?: return null
        if (x >= row.size ) return null
        return row.getOrNull(x)
    }

    open fun clone(): ArrayMap2d<T> {
        return ArrayMap2d(_map.clone().also {
            it.forEachIndexed { index, row ->
                it[index] = row.clone()
            }
        })
    }
    
    

}