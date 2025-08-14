package ua.valeriishymchuk.lobmapeditor


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.awt.SwingPanel
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.util.FPSAnimator
import org.jetbrains.compose.ui.tooling.preview.Preview

import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JPanel

@Composable
@Preview
fun App() {
    MaterialTheme {
        SwingPanel(
            factory = {
                val panel = JFrame()
                val profile = GLProfile.get(GLProfile.GL4)
                val capabilities = GLCapabilities(profile)
                capabilities.apply {
                    doubleBuffered = true
                    depthBits = 24
                }

                val glCanvas = GLCanvas(capabilities).apply {
                    setSize(800, 600)

                    val animator = FPSAnimator(this, 60).apply {
                        start()
                    }

                    addGLEventListener(object: GLEventListener {
                        override fun init(drawable: GLAutoDrawable) {
                            val gl = drawable.gl.gL4
                            gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
                            gl.glEnable(GL.GL_DEPTH_TEST)
                        }

                        override fun dispose(drawable: GLAutoDrawable) {
                            animator.stop()
                        }

                        override fun display(drawable: GLAutoDrawable) {
                            val gl = drawable.gl.gL4
                            gl.glClear(GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)

                        }

                        override fun reshape(
                            drawable: GLAutoDrawable,
                            x: Int,
                            y: Int,
                            width: Int,
                            height: Int
                        ) {
                            val gl = drawable.gl.gL4
                            gl.glViewport(0,0,width, height)
                        }

                    })
                }
                panel.contentPane.add(glCanvas)
                panel.size = panel.contentPane.preferredSize
                panel.isVisible = true
                panel
            }
        )
    }
}