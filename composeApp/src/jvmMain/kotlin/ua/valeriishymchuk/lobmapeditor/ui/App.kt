package ua.valeriishymchuk.lobmapeditor.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.jogamp.opengl.GL
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
import ua.valeriishymchuk.lobmapeditor.ui.screen.HomeScreen
import java.awt.Dimension


@Composable
@Preview
fun App() {
    var canvasRef by remember { mutableStateOf<GLCanvas?>(null) }

    Navigator(HomeScreen) { nav ->
        CurrentScreen()
    }

}

@Composable
fun JoglCanvas(canvasRefSet: (GLCanvas) -> Unit ) = SwingPanel(
    factory = {
        println("Initializing factory")
        val profile = GLProfile.get(GLProfile.GL4)
        val capabilities = GLCapabilities(profile)
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


//                    val animator = FPSAnimator(this, 60).apply {
//                        start()
//                    }

            addGLEventListener(object : GLEventListener {
                override fun init(drawable: GLAutoDrawable) {
                    println("GL initialized")
                    // Тут шейдери, VBO, VAO
                }

                override fun display(drawable: GLAutoDrawable) {
                    val gl = drawable.gl.gL3
                    gl.glClearColor(0.5f, 0f, 0.5f, 1f)
                    gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
                    // Тут рендер сцени
                }

                override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {}

                override fun dispose(drawable: GLAutoDrawable) {}
            })

            isVisible = true
            canvasRefSet(this)


        }
    },
    modifier = Modifier.fillMaxSize()

)