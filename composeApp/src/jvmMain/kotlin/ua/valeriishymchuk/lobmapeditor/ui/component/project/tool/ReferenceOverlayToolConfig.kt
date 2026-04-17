package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.joml.Vector2f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage
import ua.valeriishymchuk.lobmapeditor.services.ProjectsService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.ui.component.AngleDial
import kotlin.getValue

@Composable
@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
fun ReferenceOverlayToolConfig() {
    val editorService by rememberInstance<EditorService<*>>()
    val toolService by rememberInstance<ToolService<*>>()

    val projectService by rememberInstance<ProjectsService>()
    val projectRef by rememberInstance<ProjectRef>()
    val textureStorage by rememberInstance<TextureStorage>()

    val enabled by toolService.refenceOverlayTool.enabled.collectAsState();
    val hideSprites by toolService.refenceOverlayTool.hideSprites.collectAsState();
    val hideRange by toolService.refenceOverlayTool.hideRange.collectAsState();
    val scale by toolService.refenceOverlayTool.scale.collectAsState()
    val offset by toolService.refenceOverlayTool.offset.collectAsState()
    val transparency by toolService.refenceOverlayTool.transparency.collectAsState()
    val rotation by toolService.refenceOverlayTool.rotation.collectAsState()

    val scaleXTextFieldState = rememberTextFieldState(scale.x.toString())
    val scaleYTextFieldState = rememberTextFieldState(scale.y.toString())

    val offsetXTextFieldState = rememberTextFieldState(offset.x.toString())
    val offsetYTextFieldState = rememberTextFieldState(offset.y.toString())


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            enabled,
            onCheckedChange = {
                toolService.refenceOverlayTool.enabled.value = it
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Show reference")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            hideSprites,
            onCheckedChange = {
                toolService.refenceOverlayTool.hideSprites.value = it
                editorService.selectedObjects.value = emptySet()
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Hide sprites")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            hideRange,
            onCheckedChange = {
                toolService.refenceOverlayTool.hideRange.value = it
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Hide range")
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scaleXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.scale.value
                toolService.refenceOverlayTool.scale.value = Vector2f(it, vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scaleYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.scale.value
                toolService.refenceOverlayTool.scale.value = Vector2f(vec.x, it)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.offset.value
                toolService.refenceOverlayTool.offset.value = Vector2f(it.coerceIn(-1f..1f), vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.offset.value
                toolService.refenceOverlayTool.offset.value = Vector2f(vec.x, it.coerceIn(-1f..1f))
            }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            scaleXTextFieldState,
            leadingIcon = {
                Row {
                    Text("X", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        TextField(
            scaleYTextFieldState,
            leadingIcon = {
                Row {
                    Text("Y", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Size")
    }

    Spacer(Modifier.height(4.dp))


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            offsetXTextFieldState,
            leadingIcon = {
                Row {
                    Text("X", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        TextField(
            offsetYTextFieldState,
            leadingIcon = {
                Row {
                    Text("Y", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Offset")
    }


    Spacer(Modifier.height(4.dp))
    Text("Transparency")
    Spacer(Modifier.height(4.dp))
    Slider(
        transparency,
        onValueChange = {
            toolService.refenceOverlayTool.transparency.value = it
        },
        valueRange = 0f..1f
    )

    Spacer(Modifier.height(4.dp))
    Text("Rotation")

    Spacer(Modifier.height(4.dp))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AngleDial(
            rotation,
            color = Color.Green,
            modifier = Modifier.size(200.dp)
        )

        Slider(
            value = rotation,
            onValueChange = {
                toolService.refenceOverlayTool.rotation.value = it
            },
            valueRange = 0f..(2 * Math.PI).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(Modifier.height(4.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
//        Text("Import reference")
//        Spacer(Modifier.width(4.dp))

        DefaultButton(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val pickResult = FileKit.openFilePicker(FileKitType.Image) ?: return@launch
                    projectService.importReference(projectRef, pickResult.file)
                    textureStorage.referenceFile = projectRef.referenceFile

                }
            },
        ) {
            Text("Import reference")
        }

        OutlinedButton(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                projectService.clearReference(projectRef)
                textureStorage.referenceFile = null
            }
        }) {
            Text("Clear reference")
        }


    }


}