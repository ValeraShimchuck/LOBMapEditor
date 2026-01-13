package ua.valeriishymchuk.lobmapeditor.render.resource

import kotlinx.coroutines.runBlocking
import lobmapeditor.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.MissingResourceException

object ResourceLoader {
    fun loadResource(path: String): ByteArray? {
        return runBlocking {
            try {
                return@runBlocking Res.readBytes(path)
            } catch (e: MissingResourceException) {
                return@runBlocking null
            }

        }
    }

    fun loadShaderSource(path: String): String {
        return loadResource("files/shaders/desktop/${path}.glsl")?.decodeToString()
            ?: throw IllegalArgumentException("Can't find $path")
    }
}