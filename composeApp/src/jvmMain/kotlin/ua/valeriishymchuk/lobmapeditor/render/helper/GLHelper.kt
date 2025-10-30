package ua.valeriishymchuk.lobmapeditor.render.helper

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2ES3
import com.jogamp.opengl.GL3
import com.jogamp.opengl.GLProfile
import ua.valeriishymchuk.lobmapeditor.render.pointer.IntPointer
import java.nio.Buffer



typealias CurrentGL = GL3
const val CURRENT_GL_PROFILE = GLProfile.GL3


fun CurrentGL.glGenBuffer(): Int {
    val pointer = IntPointer()
    glGenBuffers(1, pointer.array, 0)
    return pointer.value
}

fun CurrentGL.glGenVAO(): Int {
    val pointer = IntPointer()
    glGenVertexArrays(1, pointer.array, 0)
    return pointer.value
}



fun CurrentGL.glBindVBO(vbo: Int) {
    glBindBuffer(GL.GL_ARRAY_BUFFER, vbo)
}

fun CurrentGL.glVBOData(sizeInBytes: Int, data: Buffer) {
    glBufferData(GL.GL_ARRAY_BUFFER, sizeInBytes.toLong(), data, GL.GL_STATIC_DRAW)
}

fun GL.currentGl(): CurrentGL {
    return gL3
}