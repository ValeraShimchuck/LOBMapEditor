package ua.valeriishymchuk.lobmapeditor.shared.refence

class Reference<K,V>(
    val key: K
) {

    fun getValueOrNull(getter: (K) -> V?): V? {
        return getter(key)
    }

    fun getValueOrNull(map: Map<K, V>): V? {
        return getValueOrNull { map[it] }
    }

    fun getValue(getter: (K) -> V): V {
        return getter(key)
    }

    fun getValue(map: Map<K, V>): V {
        return getValueOrNull(map) ?: throw NullPointerException("Can't find value by key: $key")
    }

    fun isValid(getter: (K) -> V?): Boolean {
        return getValueOrNull(getter) != null
    }

    fun isValid(map: Map<K, V>): Boolean {
        return getValueOrNull(map) != null
    }

}