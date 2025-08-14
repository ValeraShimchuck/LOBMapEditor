package ua.valeriishymchuk.lobmapeditor

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import lobmapeditor.composeapp.generated.resources.Res
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.*
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarStyle
import ua.valeriishymchuk.lobmapeditor.ui.App
import ua.valeriishymchuk.lobmapeditor.ui.TitleBarView
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@OptIn(ExperimentalLayoutApi::class)
fun main() {
//    System.setProperty("jogl.debug", "true")
//    System.setProperty("nativewindow.debug", "all")
//    System.setProperty("jogl.verbose", "true")
    loadJogsLibs()
    println("Successfully loaded libs")

    application {
        val textStyle = JewelTheme.createDefaultTextStyle()
        val editorStyle = JewelTheme.createEditorTextStyle()

        IntUiTheme(
            theme = JewelTheme.darkThemeDefinition(
                defaultTextStyle = textStyle,
                editorTextStyle = editorStyle
            ),
            styling =
                ComponentStyling.default()
                    .decoratedWindow(
                        titleBarStyle = TitleBarStyle.dark()
                    ),
        ) {
            DecoratedWindow(
                onCloseRequest = { exitApplication() },
                title = "LOBMapEditor",
                style = DecoratedWindowStyle.dark(),
                content = {
                    TitleBarView()
                    App()
                },
            )
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
        val newFile = File(folder, zipEntry!!.name)
        if (zipEntry!!.isDirectory) {
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
        val os = System.getProperty("os.name").lowercase()
            .let { if(it.startsWith("win")) "windows" else it  }
        val arch = System.getProperty("os.arch").lowercase()
        val key = "${os}-${arch}".lowercase()

        println("Trying to find libs for $key...")
        val folder = File("natives/${key}/")
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false")
        //System.setProperty("nativewindow.debug", "all")
        if (!folder.exists() || folder.listFiles().isEmpty()) {
            unzip("files/natives/${key}/libs.zip", folder)
        }
//        folder.listFiles().filter { it.isFile }.forEach {
//            println("Loading ${it.name}")
//            System.load(it.absolutePath)
//        }
    }
}