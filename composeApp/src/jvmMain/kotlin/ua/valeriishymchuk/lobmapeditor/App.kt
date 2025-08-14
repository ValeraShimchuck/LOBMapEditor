package ua.valeriishymchuk.lobmapeditor


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.awt.GLJPanel
import com.jogamp.opengl.util.FPSAnimator
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Dimension


@Composable
@Preview
fun App() {
    var canvasRef by remember { mutableStateOf<GLCanvas?>(null) }
    MaterialTheme {
        Column {
            Button(
                onClick = { canvasRef?.repaint() }
            ) {
                Text("Repaint")
            }
            joglCanvas { canvasRef = it }
        }
    }
}

@Composable
fun joglCanvas(canvasRefSet: (GLCanvas) -> Unit ) = SwingPanel(
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

            addGLEventListener(object: GLEventListener {
                override fun init(drawable: GLAutoDrawable) {
                    println("OpenGL initialized")
                    val gl = drawable.gl
                    gl.glClearColor(0.5f, 0.2f, 0.5f, 1.0f)
//                            gl.glEnable(GL.GL_DEPTH_TEST)
                }

                override fun dispose(drawable: GLAutoDrawable) {
//                            animator.stop()
                }

                override fun display(drawable: GLAutoDrawable) {
                    println("Trying to render Frame")
                    val gl = drawable.gl
                    gl.glClear(GL_COLOR_BUFFER_BIT)

                    drawable.swapBuffers()
                    println("Frame rendered")

                }

                override fun reshape(
                    drawable: GLAutoDrawable,
                    x: Int,
                    y: Int,
                    width: Int,
                    height: Int
                ) {
                    val gl = drawable.gl
                    gl.glViewport(0,0,width, height)
                }

            })

            addComponentListener(object : java.awt.event.ComponentAdapter() {
                override fun componentResized(e: java.awt.event.ComponentEvent?) {
                    println("Component resized: ${size.width}x${size.height}")
                }

                override fun componentShown(e: java.awt.event.ComponentEvent?) {
                    println("Component shown")
                    repaint()
                }
            })

            isVisible = true
            canvasRefSet(this)

            javax.swing.Timer(1000) {
                println("Forcing initial repaint")
                repaint()
            }.start()
        }
    },
    modifier = Modifier.fillMaxSize()

)