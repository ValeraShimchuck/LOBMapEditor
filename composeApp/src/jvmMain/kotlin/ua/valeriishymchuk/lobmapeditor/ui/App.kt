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
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
import ua.valeriishymchuk.lobmapeditor.ui.screen.HomeScreen
import ua.valeriishymchuk.lobmapeditor.command.CommandDispatcher
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.OpenGLListener
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


            val glListener = OpenGLListener(CommandDispatcher(GameScenario.Preset(
                GameScenario.CommonData(
                    "test",
                    "description",
                    Terrain.ofCells(),
                    emptyList(),
                    emptyList()
                ),
                emptyList(),
                listOf(
                    Player(PlayerTeam.RED),
                    Player(PlayerTeam.BLUE)
                )
            )))

            addGLEventListener(glListener)
            addMouseMotionListener(glListener.MouseMotionListener(this::repaint))
            val mouseListener = glListener.MouseListener(this::repaint)
            addMouseListener(mouseListener)
            addMouseWheelListener(mouseListener)

            isVisible = true
            canvasRefSet(this)


        }
    },
    modifier = Modifier.fillMaxSize()

)