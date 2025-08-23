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
import ua.valeriishymchuk.lobmapeditor.command.CommandDispatcher
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.render.EditorRenderer
import java.awt.Dimension
import kotlin.random.Random


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


            val glListener = EditorRenderer(CommandDispatcher(GameScenario.Preset(
                GameScenario.CommonData(
                    "test",
                    "description",
                    Terrain.ofCells().apply {
//                        val random = Random(12313)
//                        for (x in 0..<widthTiles)
//                            for (y in 0..<widthTiles) {
//                                terrainMap.set(x, y, TerrainType.GRASS)
////                                if (random.nextDouble() > 0.7) terrainMap.set(x, y, TerrainType.ROAD)
//                            }
                        terrainMap.set(10, 10, TerrainType.SHALLOW_WATER)
                        terrainMap.set(10, 11, TerrainType.BRIDGE)
                        terrainMap.set(9, 11, TerrainType.ROAD)
                        terrainMap.set(11, 11, TerrainType.ROAD)
                        terrainMap.set(10, 12, TerrainType.SHALLOW_WATER)


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
            addKeyListener(glListener.KeyPressListener())

            isVisible = true
            canvasRefSet(this)


        }
    },
    modifier = Modifier.fillMaxSize()

)