package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.runBlocking
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.*
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.services.ErrorService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.services.ScenarioIOService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.setupProjectScopeDiModule
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.ui.JoglCanvas
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolBar
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolConfig
import java.io.File

class ProjectScreen(
    private val ref: ProjectRef,
): TitleBarScreen {



    @Composable
    override fun Content() {

        val scenarioIO by rememberInstance<ScenarioIOService>()
        val errorService by rememberInstance<ErrorService>()
        val nav = LocalNavigator.currentOrThrow


        org.kodein.di.compose.subDI(diBuilder = {
            import(setupProjectScopeDiModule(ref))
        }, content = {

            val editorService by rememberInstance<EditorService<GameScenario.Preset>>()

            val scenario by produceState<GameScenario.Preset?>(null) {
                val mapFile = File(ref.dirFile, "map.json")

                if(!mapFile.exists()) {
                    errorService.error.value = ErrorService.AppError(
                        ErrorService.AppError.Severity.Error,
                        "Project map file not found",
                    )
                    nav.push(HomeScreen)
                    return@produceState
                }

                val scenario = try {
                    scenarioIO.load(mapFile)
                } catch (e: Exception) {
                    errorService.error.value = ErrorService.AppError(
                        ErrorService.AppError.Severity.Error,
                        e.message,
                    )
                    nav.push(HomeScreen)
                    return@produceState
                }

                if(scenario is GameScenario.Hybrid) {
                    errorService.error.value = ErrorService.AppError(
                        ErrorService.AppError.Severity.Warning,
                        "Hybrid scenario is not supported yet"
                    )
                    nav.push(HomeScreen)
                    return@produceState
                }
                editorService.scenario = scenario as GameScenario.Preset;
                value = scenario
            }

            if(scenario != null) {
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
            }
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