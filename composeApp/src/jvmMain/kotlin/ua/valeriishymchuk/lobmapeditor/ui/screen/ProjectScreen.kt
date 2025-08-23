package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.runBlocking
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.kodein.di.*
import org.kodein.di.compose.rememberDI
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
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
            bindInstance<ProjectRef> { ref }
            bindEagerSingleton<ProjectData> {
                runBlocking { directDI.instance<ProjectsService>().loadProject(ref) }
            }
        }, content = {
            val terrain by BaleriiDebugShitInformation.currentTerrain.collectAsState()
            val height by BaleriiDebugShitInformation.currentHeight.collectAsState()
            val mode by BaleriiDebugShitInformation.setTerrainHeight.collectAsState()

            VerticalSplitLayout(
                first = { JoglCanvas {  } },
                second = {
                    Column {
                        Text("TERRAIN $terrain")
                        Text("HEIGHT $height")
                        Text("MODE: ${if(mode) "HEIGHT" else "TERRAIN"}")
                    }
                },
                modifier = Modifier.fillMaxSize(),
                firstPaneMinWidth = 800.dp,
                secondPaneMinWidth = 300.dp,
            )
        })
    }


}