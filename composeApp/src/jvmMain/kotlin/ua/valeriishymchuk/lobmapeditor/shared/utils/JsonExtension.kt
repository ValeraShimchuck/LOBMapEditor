package ua.valeriishymchuk.lobmapeditor.shared.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.getOrNull(str: String): JsonElement? {
    return if (this.has(str)) {
        this.get(str)
    } else null
}