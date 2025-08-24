package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.runBlocking
import org.jetbrains.jewel.ui.component.*
import org.kodein.di.*
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.setupProjectScopeDiModule
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.ui.BaleriiDebugShitInformation
import ua.valeriishymchuk.lobmapeditor.ui.JoglCanvas

class ProjectScreen(
    private val ref: ProjectRef,
): Screen {

    @Composable
    override fun Content() {

        org.kodein.di.compose.subDI(diBuilder = {
            import(setupProjectScopeDiModule(ref))
        }, content = {
            val terrain by BaleriiDebugShitInformation.currentTerrain.collectAsState()
            val height by BaleriiDebugShitInformation.currentHeight.collectAsState()
            val mode by BaleriiDebugShitInformation.setTerrainHeight.collectAsState()

            HorizontalSplitLayout(
                first = {
                    EditorPanel()
                },
                second = { JoglCanvas {  } },
                modifier = Modifier.fillMaxSize(),
                firstPaneMinWidth = 400.dp,
                secondPaneMinWidth = 800.dp,
                state = rememberSplitLayoutState(0.25f)
            )
        })
    }

    @Composable
    private fun EditorPanel() {
        Column {

        }
    }
}