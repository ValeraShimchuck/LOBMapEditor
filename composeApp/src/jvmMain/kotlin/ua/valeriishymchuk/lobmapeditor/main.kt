package ua.valeriishymchuk.lobmapeditor

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jogamp.common.os.Platform
import com.jogamp.opengl.GLProfile
import kotlinx.coroutines.runBlocking
import lobmapeditor.composeapp.generated.resources.Res
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.let

fun main() {
    loadJogsLibs()
    println("Successfully loaded libs")
    if (true) return
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "LOBMapEditor",
        ) {
            GLProfile.initSingleton()
            App()
        }
    }
}

suspend fun unzip(src: String, dst: File) {
    val bais = ByteArrayInputStream(Res.readBytes(src))
    val zis = ZipInputStream(bais)
    val buffer = ByteArray(1024)
    val folder = dst
    folder.mkdirs()
    var zipEntry: ZipEntry?
    while (zis.nextEntry.also { zipEntry = it  } != null) {
        zipEntry = zipEntry!!
        val newFile = File(folder, zipEntry.name)
        if (zipEntry.isDirectory) {
            newFile.mkdirs()
        } else {
            File(newFile.parent).mkdirs()
            FileOutputStream(newFile).use { fos ->
                var length: Int?
                while (zis.read(buffer).also { length = it } > 0) {
                    fos.write(buffer, 0, length!!)
                }
            }
        }
    }
}

fun loadJogsLibs() {
    runBlocking {
        val os = Platform.OS
        val arch = Platform.ARCH
        val key = "${os}-${arch}".lowercase()
        println("Trying to find libs for $key...")
        val folder = File("libs/${key}/")
        unzip("files/natives/${key}/libs.zip", folder)
        folder.listFiles().filter { it.isFile }.forEach {

            System.load(it.absolutePath)
        }
    }
}