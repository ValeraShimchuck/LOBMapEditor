package ua.valeriishymchuk.lobmapeditor.render.helper

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import java.nio.Buffer

fun GL3.glGenBuffer(): Int {
    val pointer = IntPointer()
    glGenBuffers(1, pointer.array, 0)
    return pointer.value
}

fun GL3.glGenVAO(): Int {
    val pointer = IntPointer()
    glGenVertexArrays(1, pointer.array, 0)
    return pointer.value
}



fun GL3.glBindVBO(vbo: Int) {
    glBindBuffer(GL.GL_ARRAY_BUFFER, vbo)
}

fun GL3.glVBOData(sizeInBytes: Int, data: Buffer) {
    glBufferData(GL.GL_ARRAY_BUFFER, sizeInBytes.toLong(), data, GL.GL_STATIC_DRAW)
}