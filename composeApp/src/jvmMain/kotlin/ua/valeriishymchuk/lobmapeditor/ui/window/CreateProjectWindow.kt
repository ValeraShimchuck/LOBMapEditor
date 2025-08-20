package ua.valeriishymchuk.lobmapeditor.ui.window

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.*
import ua.valeriishymchuk.lobmapeditor.services.dto.CreateProjectData
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView


@OptIn(ExperimentalJewelApi::class)
@Composable
fun CreateProjectWindow() {
    var form by remember { mutableStateOf(CreateProjectData()) }

    fun validate() {

    }

    Column(
        Modifier.padding(8.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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

        Row {
            val widthState = remember { TextFieldState(form.widthPx.toString()) }
            LaunchedEffect(widthState.text) {
                val newWidth = nameState.text.toString().toIntOrNull()
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
                val newHeight = nameState.text.toString().toIntOrNull()
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

        Spacer(Modifier.weight(1f))

        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DefaultButton(onClick = { validate() }) { Text("Create") }
            DefaultButton(onClick = { pickFolderAWT() }) { Text("333") }
        }


    }
}

fun pickFolderAWT(): File? {

    val dialog = FileDialog(null as Frame?, "Choose directory", FileDialog.LOAD)
    dialog.isVisible = true
    val file = dialog.file
    return if (file != null) File(dialog.directory, file) else null
}