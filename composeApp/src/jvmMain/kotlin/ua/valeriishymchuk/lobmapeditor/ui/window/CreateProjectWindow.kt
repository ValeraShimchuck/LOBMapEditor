package ua.valeriishymchuk.lobmapeditor.ui.window

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.*
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.rememberDI
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.services.dto.CreateProjectData
import ua.valeriishymchuk.lobmapeditor.ui.composable.WindowScope
import ua.valeriishymchuk.lobmapeditor.ui.screen.ProjectScreen


@OptIn(ExperimentalJewelApi::class)
@Composable
fun WindowScope.CreateProjectWindow() {

    val nav = LocalNavigator.currentOrThrow
    val projectsService: ProjectsService by rememberDI { instance() }

    var form by remember { mutableStateOf(CreateProjectData()) }
    val uiScope = rememberCoroutineScope()


    var isCreating by remember { mutableStateOf(false) }

    var errors: List<String> by remember { mutableStateOf(emptyList()) }

    fun create(): Boolean {
        println("Before validating $form")
        val validate = CreateProjectData.validator.validate(form)
        if(validate.isValid) {
            errors = emptyList()
            uiScope.launch {
                nav.push(ProjectScreen(projectsService.createProject(form)))
                delay(300)
                close()
            }
            return true

        } else {
            errors = validate.errors.map { it.dataPath.substringAfterLast('.') + ": " + it.message }
        }
        return false
    }

    Column(
        Modifier.padding(8.dp).fillMaxWidth()
    ) {
        val nameState = remember { TextFieldState(form.name) }
        LaunchedEffect(nameState.text) {
            if (form.name != nameState.text) {
                form = form.copy(name = nameState.text.toString())
            }
        }
        TextField(
            state = nameState,
            placeholder = { Text("Scenario name...") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))


        Row {
            val widthState = remember { TextFieldState(form.widthPx.toString()) }
            LaunchedEffect(widthState.text) {
//                println("Changed state of witdth ${}")
                val newWidth = widthState.text.toString().toIntOrNull()
                if (newWidth != null && form.widthPx != newWidth) {
                    form = form.copy(widthPx = newWidth)
                }
            }
            TextField(
                state = widthState,
                placeholder = { Text("Width") },
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.width(8.dp))

            val heightState = remember { TextFieldState(form.heightPx.toString()) }
            LaunchedEffect(heightState.text) {
                val newHeight = heightState.text.toString().toIntOrNull()
                if (newHeight != null && form.heightPx != newHeight) {
                    form = form.copy(heightPx = newHeight)
                }
            }
            TextField(
                state = heightState,
                placeholder = { Text("Height") },
                modifier = Modifier.weight(1f),
            )

        }

        Spacer(Modifier.height(8.dp))


        var folderDirState by remember { mutableStateOf(TextFieldState(form.dir?.absolutePath?.toString() ?: "")) }
        TextField(
            state = folderDirState,
            placeholder = { Text("Project directory") },
            enabled = false,
            trailingIcon = {
                IconActionButton(
                    AllIconsKeys.Nodes.Folder,
                    null,
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val pickResult = FileKit.openDirectoryPicker("Pick project folder") ?: return@launch
                            form = form.copy(dir = pickResult.file)
                            folderDirState = TextFieldState(form.dir?.absolutePath!!)
                        }
                    },
                    enabled = !isCreating
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("All project files will be saved in this directory", color = JewelTheme.globalColors.text.info)


        Spacer(Modifier.weight(1f))

        errors.forEach {
            Text(it, color = JewelTheme.globalColors.text.error)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DefaultButton(onClick = {
                if (create()) isCreating = true
            }, enabled = !isCreating) { Text("Create") }
        }


    }
}

