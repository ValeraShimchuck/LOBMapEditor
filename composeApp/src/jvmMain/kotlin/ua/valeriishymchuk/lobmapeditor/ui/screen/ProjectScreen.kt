package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.runBlocking
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
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
import ua.valeriishymchuk.lobmapeditor.ui.JoglCanvas
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolBar
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolConfig

class ProjectScreen(
    private val ref: ProjectRef,
): Screen {

    @Composable
    override fun Content() {

        org.kodein.di.compose.subDI(diBuilder = {
            import(setupProjectScopeDiModule(ref))
        }, content = {

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
        Column(
            Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolBar(
                Modifier.padding(vertical = 4.dp)
            )

            ToolConfig()




        }
    }
}