package ua.valeriishymchuk.lobmapeditor.render.helper

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


object BufferHelper {
    fun allocateDirectFloatBuffer(size: Int): FloatBuffer {
        return ByteBuffer.allocateDirect(size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }

    fun wrapDirect(floatArray: FloatArray): FloatBuffer {
        val buffer = allocateDirectFloatBuffer(floatArray.size)
        buffer.put(floatArray)
        buffer.flip()
        return buffer
    }

}
