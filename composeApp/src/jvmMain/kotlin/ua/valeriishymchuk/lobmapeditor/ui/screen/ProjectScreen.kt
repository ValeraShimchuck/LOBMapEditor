package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jogamp.opengl.awt.GLCanvas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.TitleBarScope
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.compose.withDI
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.ErrorService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.ScenarioIOService
import ua.valeriishymchuk.lobmapeditor.services.ToastService
import ua.valeriishymchuk.lobmapeditor.services.project.setupProjectScopeDiModule
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.ui.JoglCanvas
import ua.valeriishymchuk.lobmapeditor.ui.component.DockContainer
import ua.valeriishymchuk.lobmapeditor.ui.component.project.objective.ObjectivePropertiesConfig
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolConfig
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolDock
import ua.valeriishymchuk.lobmapeditor.ui.component.project.unit.UnitsConfigDock
import ua.valeriishymchuk.lobmapeditor.ui.screen.project.ProjectTitleScreenProvider
import java.awt.Desktop
import java.io.File

class ProjectScreen(
    private val ref: ProjectRef,
) : TitleBarScreen() {


    @Composable
    override fun Content() {
        val scenarioIO by rememberInstance<ScenarioIOService>()
        val errorService by rememberInstance<ErrorService>()
        val nav = LocalNavigator.currentOrThrow


        org.kodein.di.compose.subDI(diBuilder = {
            import(setupProjectScopeDiModule(ref))
        }, content = {

            ProjectTitleScreenProvider()


            val editorService by rememberInstance<EditorService<GameScenario.Preset>>()

            val scenario by produceState<GameScenario.Preset?>(null) {
                val mapFile = File(ref.dirFile, "map.json")

                if (!mapFile.exists()) {
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

                if (scenario is GameScenario.Hybrid) {
                    errorService.error.value = ErrorService.AppError(
                        ErrorService.AppError.Severity.Warning,
                        "Hybrid scenario is not supported yet"
                    )
                    nav.push(HomeScreen)
                    return@produceState
                }
                editorService.scenario.value = scenario as GameScenario.Preset;
                value = scenario
            }

            if (scenario != null) {
                var canvas by remember { mutableStateOf<GLCanvas?>(null) }

                HorizontalSplitLayout(
                    first = {
                        subDI(diBuilder =  {
                            bindProvider { canvas!! }
                        }, content = {
                            EditorPanel()
                        })
                    },
                    second = { JoglCanvas { canvas = it } },
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
        /*Column(
            Modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

        }*/

        VerticalSplitLayout(
            first = {
                ToolDock()
            },
            second = {
                UnitsConfigDock()
            },
            state = rememberSplitLayoutState(0.45f),
            firstPaneMinWidth = 200.dp,
            secondPaneMinWidth = 300.dp
        )
    }
}