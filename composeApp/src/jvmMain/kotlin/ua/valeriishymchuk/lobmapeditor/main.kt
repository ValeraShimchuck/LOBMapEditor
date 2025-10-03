package ua.valeriishymchuk.lobmapeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
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
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.services.ErrorService
import ua.valeriishymchuk.lobmapeditor.services.LifecycleService
import ua.valeriishymchuk.lobmapeditor.services.ToastService
import ua.valeriishymchuk.lobmapeditor.services.servicesModule
import ua.valeriishymchuk.lobmapeditor.ui.App
import ua.valeriishymchuk.lobmapeditor.ui.TitleBarView
import ua.valeriishymchuk.lobmapeditor.ui.screen.HomeScreen
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

val di = DI {
    import(servicesModule)
}

@OptIn(ExperimentalLayoutApi::class)
fun main() {
//    System.setProperty("jogl.debug", "true")
//    System.setProperty("nativewindow.debug", "all")
//    System.setProperty("jogl.verbose", "true")
//    System.setProperty("jogl.debug.GLDrawable.PerfStats", "true")
//    System.setProperty("compose.interop.blending", "true")
//    System.setProperty("compose.swing.render.on.graphics", "true")
    loadJogsLibs()
    println("Successfully loaded libs")
    Runtime.getRuntime().addShutdownHook(Thread {
        val lifecycleService by di.instance<LifecycleService>()
        println("Closing application")
        lifecycleService.onClose()
    })
    application {
        val textStyle = JewelTheme.createDefaultTextStyle()
        val editorStyle = JewelTheme.createEditorTextStyle()


        withDI(di) {
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
                Navigator(HomeScreen) {
                    DecoratedWindow(
                        onCloseRequest = { exitApplication() },
                        title = "LOBMapEditor",
                        style = DecoratedWindowStyle.dark(),
                        content = {
                            TitleBarView()
                            Box(modifier = Modifier.background(
                                JewelTheme.globalColors.panelBackground
                            ).fillMaxSize()) {
                                App()
                            }
                        },
                    )
                }
            }
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