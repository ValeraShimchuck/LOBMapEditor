package ua.valeriishymchuk.lobmapeditor.ui


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import cafe.adriel.voyager.navigator.CurrentScreen
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kodein.di.compose.localDI
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.EditorRenderer
import java.awt.Dimension


@Composable
@Preview
fun App() {
    var canvasRef by remember { mutableStateOf<GLCanvas?>(null) }

    CurrentScreen()

}

object BaleriiDebugShitInformation {
    var currentTerrain = MutableStateFlow(TerrainType.FARM)
    var currentHeight = MutableStateFlow(1)
    var setTerrainHeight = MutableStateFlow(false)
}

@Composable
fun JoglCanvas(canvasRefSet: (GLCanvas) -> Unit ) {
    val di = localDI()

    SwingPanel(
        factory = {
            println("Initializing factory")
            val profile = GLProfile.get(GLProfile.GL3)
            val capabilities = GLCapabilities(profile)
//        capabilities.isPBuffer = true
            capabilities.apply {
                doubleBuffered = true
                depthBits = 24
            }



            GLCanvas(capabilities).apply {
                name = "MainGLCanvas"  // For debugging
                setSize(800, 600)
                preferredSize = Dimension(800, 600)
                GLProfile.initSingleton()
                println("Initializing GLProfile singleton")


                val glListener = EditorRenderer(di)



                addGLEventListener(glListener)
                addMouseMotionListener(glListener.MouseMotionListener(this::repaint))
                val mouseListener = glListener.MouseListener(this::repaint)
                addMouseListener(mouseListener)
                addMouseWheelListener(mouseListener)
                addKeyListener(glListener.KeyPressListener(this::repaint))

                isVisible = true
                canvasRefSet(this)


            }
        },
        modifier = Modifier.fillMaxSize()

    )
}