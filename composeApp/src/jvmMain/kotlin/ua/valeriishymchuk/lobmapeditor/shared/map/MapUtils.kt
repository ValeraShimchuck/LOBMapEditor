package ua.valeriishymchuk.lobmapeditor.shared.map

import java.util.EnumMap
import kotlin.reflect.KClass

fun <K: Enum<K>, V> generateEnumMap(clazz: KClass<K>, getter: (K) -> V): Map<K, V> {
    return clazz.java.enumConstants.associateWith(getter)
}