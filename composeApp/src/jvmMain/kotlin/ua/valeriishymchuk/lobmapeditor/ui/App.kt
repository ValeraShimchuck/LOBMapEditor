package ua.valeriishymchuk.lobmapeditor.ui


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import cafe.adriel.voyager.navigator.CurrentScreen
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
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
                        val random = Random(12313)
                        for (x in 0..<widthTiles)
                            for (y in 0..<widthTiles) {
                                terrainMap.set(x, y, TerrainType.GRASS)
//                                if (random.nextDouble() > 0.7) terrainMap.set(x, y, TerrainType.ROAD)
                            }

                        for (x in 10..<20)
                            for (y in 10..<20)
                                terrainHeight.set(x, y, 1)


                        for (x in 11..<19)
                            for (y in 11..<19)
                                terrainHeight.set(x, y, 2)

                        for (x in 12..<18)
                            for (y in 12..<18)
                                terrainHeight.set(x, y, 3)


                        for (x in 13..<17)
                            for (y in 13..<17)
                                terrainHeight.set(x, y, 4)
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