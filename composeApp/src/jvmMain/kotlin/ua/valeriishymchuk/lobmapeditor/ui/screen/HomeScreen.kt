package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberDI
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.services.ToastService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectData
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.ui.composable.WindowController
import ua.valeriishymchuk.lobmapeditor.ui.composable.rememberWindowController
import ua.valeriishymchuk.lobmapeditor.ui.window.CreateProjectWindow

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        MockProjectsList()
    }

    @OptIn(ExperimentalJewelApi::class)
    @Composable
    private fun MockProjectsList() {

        val projectsService: ProjectsService by rememberDI { instance() }

        val projects by produceState<Map<ProjectRef, ProjectData>?>(null) {
            while (true) {
                value = projectsService.loadProjects()
                delay(5000)
            }
        }

        val createProjectWindow = rememberWindowController(
            "Create project", rememberWindowState(
                width = 600.dp,
                height = 500.dp,
            )
        ) {
            CreateProjectWindow()
        }

        createProjectWindow.Render()

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    state = remember { TextFieldState("") },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Search")
                    }
                )
                NewProjectButton(createProjectWindow)
                OpenProjectButton()
//                ImportMapButton()


            }
            if (projects == null) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else if (projects!!.isEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("No projects", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.height(8.dp))
                    NewProjectButton(createProjectWindow)
                }
            } else {
                VerticallyScrollableContainer(
                    modifier = Modifier.fillMaxSize(), // контейнер заповнює весь екран
                    scrollState = rememberScrollState()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical =  8.dp)
                    ) {
                        projects!!.forEach { (ref, data) ->
                            ProjectCard(
                                name = data.name,
                                ref = ref,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

            }


        }
    }


    @Composable
    private fun NewProjectButton(windowController: WindowController) {
        DefaultButton(onClick = {
            windowController.open()
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AllIconsKeys.General.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("New project")
            }
        }
    }

    @Composable
    private fun OpenProjectButton() {
        val toastService by rememberInstance<ToastService>()
        val projectService by rememberInstance<ProjectsService>()
        val currentNavigator = LocalNavigator.currentOrThrow
        OutlinedButton(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val folder = FileKit.openDirectoryPicker("Choose existing project") ?: return@launch
                val folderFile = folder.file
                val ref = ProjectRef(folderFile.absolutePath)
                if (!ref.projectFile.exists() && !ref.mapFile.exists()) {
                    toastService.toast {
                        ErrorInlineBanner(
                            "Can't import project from: ${folder.file.absoluteFile}",
                        )
                    }
                } else {
                    val project = projectService.loadProject(ref)
                    currentNavigator.push(ProjectScreen(ref))
                    toastService.toast {
                        SuccessInlineBanner(
                            "Project ${project.name} imported from: ${folder.file.absoluteFile}",
                        )
                    }
                }


            }
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AllIconsKeys.Nodes.Folder, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Open project")
            }
        }
    }

    @Composable
    private fun ImportMapButton() {
        OutlinedButton(onClick = { }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AllIconsKeys.Json.Object, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Import map")
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ProjectCard(name: String, ref: ProjectRef, modifier: Modifier = Modifier) {
        val nav = LocalNavigator.currentOrThrow
        val projectsService: ProjectsService by rememberDI { instance() }


        Row(
            Modifier.onClick {
                nav.push(ProjectScreen(ref))
            }
        ) {
            Column(
                Modifier.size(32.dp).padding(4.dp).background(
                    brush = Brush.horizontalGradient(
                        colors = List(2) {
                            Color.hsl(
                                (0..360).random().toFloat(),
                                0.67f,
                                0.40f
                            )
                        },
                    ),
                    shape = RoundedCornerShape(4.dp)
                ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(name.take(2))
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier) {
                Text(name)
                Spacer(Modifier.height(4.dp))
                Text(ref.path, color = JewelTheme.globalColors.text.info)
            }
        }
    }

}