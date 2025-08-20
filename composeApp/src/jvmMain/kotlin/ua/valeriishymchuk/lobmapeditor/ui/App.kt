package ua.valeriishymchuk.lobmapeditor.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
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


    HorizontalSplitLayout(
        first = {
            Column(
                modifier = Modifier.background(
                    JewelTheme.globalColors.panelBackground
                ).fillMaxSize()
            ) {
                Text("First")
            }
        },
        firstPaneMinWidth = 200.dp,
        secondPaneMinWidth = 300.dp,
        second = {
            VerticalSplitLayout(
                first = {
                    JoglCanvas { canvasRef = it }
                },
                firstPaneMinWidth = 250.dp,
                secondPaneMinWidth = 200.dp,
                second = {
                    Column(
                        modifier = Modifier.background(
                            JewelTheme.globalColors.panelBackground
                        ).fillMaxSize()
                    ) {
                        Text("Second")
                    }
                }
            )
        }
    )

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
                    Terrain.ofCells().apply {
                        terrainMap.set(1, 1, TerrainType.SNOW)
                        terrainMap.set(1, 2, TerrainType.SNOW)
                        terrainMap.set(2, 1, TerrainType.SNOW)
                        terrainMap.set(2, 2, TerrainType.SNOW)
                    },
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