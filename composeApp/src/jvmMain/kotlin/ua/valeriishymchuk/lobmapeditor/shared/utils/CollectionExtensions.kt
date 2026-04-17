package ua.valeriishymchuk.lobmapeditor.shared.utils

fun <T> List<T>.addImmutably(element: T): List<T> {
    val mutable = toMutableList()
    mutable.add(element)
    return mutable
}