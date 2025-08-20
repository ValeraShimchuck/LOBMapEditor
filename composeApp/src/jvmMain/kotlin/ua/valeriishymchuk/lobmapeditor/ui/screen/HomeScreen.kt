package ua.valeriishymchuk.lobmapeditor.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.rememberDI
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
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

        val data by produceState<Map<ProjectRef, ProjectData>?>(null) {
            while (true) {
                value = projectsService.loadProjects()
                delay(5000)
            }
        }

        val createProjectWindow = rememberWindowController(
            "Create project", rememberWindowState(
                width = 400.dp,
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
                ImportMapButton()



            }
            if (data == null) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else if (data!!.isEmpty()) {
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
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        for (i in 1..50) { // багато елементів для прокрутки
//                        Text("Item #$i", modifier = Modifier.padding(4.dp))
                            ProjectCard(
                                "Project #$i",
                                "path/to/project#$i",
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
        OutlinedButton(onClick = { }) {
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

    @Composable
    private fun ProjectCard(name: String, path: String, modifier: Modifier = Modifier) {
        Column(modifier) {
            Text(name)
            Spacer(Modifier.height(4.dp))
            Text(path, color = JewelTheme.globalColors.text.info)
        }
    }

}