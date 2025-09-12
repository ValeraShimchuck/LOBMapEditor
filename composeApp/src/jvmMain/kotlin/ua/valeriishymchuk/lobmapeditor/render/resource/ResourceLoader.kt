package ua.valeriishymchuk.lobmapeditor.render.resource

import kotlinx.coroutines.runBlocking
import lobmapeditor.composeapp.generated.resources.Res

object ResourceLoader {
    fun loadResource(path: String): ByteArray {
        return runBlocking {
            Res.readBytes(path)
        }
    }

    fun loadShaderSource(path: String): String {
        return loadResource("files/shaders/desktop/${path}.glsl").decodeToString()
    }
}