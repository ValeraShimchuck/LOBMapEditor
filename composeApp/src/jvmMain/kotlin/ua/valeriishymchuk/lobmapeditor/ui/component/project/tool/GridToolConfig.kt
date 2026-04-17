package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.joml.Vector2f
import org.joml.Vector4f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import kotlin.getValue
import kotlin.math.max

@Composable
@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
fun GridToolConfig() {

    val toolService by rememberInstance<ToolService<*>>()

    val size by toolService.gridTool.size.collectAsState()
    val offset by toolService.gridTool.offset.collectAsState()
    val thickness by toolService.gridTool.thickness.collectAsState()
    val color by toolService.gridTool.color.collectAsState()

    val sizeXTextFieldState = rememberTextFieldState(size.x.toString())
    val sizeYTextFieldState = rememberTextFieldState(size.y.toString())

    val offsetXTextFieldState = rememberTextFieldState(offset.x.toString())
    val offsetYTextFieldState = rememberTextFieldState(offset.y.toString())

    val enabled by toolService.gridTool.enabled.collectAsState();

    val controller = rememberColorPickerController()


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            enabled,
            onCheckedChange = {
                toolService.gridTool.enabled.value = it
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Show grid")
    }

    Spacer(Modifier.height(4.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            sizeXTextFieldState,
            leadingIcon = {
                Row {
                    Text("X", color = JewelTheme.globalColors.text.info)
                    Spacer(Modifier.width(4.dp))
                }
            }
        )
        Spacer(Modifier.width(4.dp))
        TextField(
            sizeYTextFieldState,
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

    Text("Thickness")
    Spacer(Modifier.height(4.dp))
    Slider(
        thickness,
        onValueChange = {
            toolService.gridTool.thickness.value = it
        },
        valueRange = 0f..max(toolService.gridTool.size.value.x, toolService.gridTool.size.value.y)
    )

    Spacer(Modifier.height(4.dp))


    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .padding(10.dp),
        controller = controller,

        initialColor = color.let {
            Color(
                it.x,
                it.y,
                it.z,
                it.w
            )
        },

        onColorChanged = { colorEnvelope: ColorEnvelope ->
            toolService.gridTool.color.value =
                Vector4f(
                    colorEnvelope.color.red,
                    colorEnvelope.color.green,
                    colorEnvelope.color.blue,
                    colorEnvelope.color.alpha
                )


        }
    )

    BrightnessSlider(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        controller = controller,
        initialColor = color.let {
            Color(
                it.x,
                it.y,
                it.z,
                it.w
            )
        }
    )
    Spacer(Modifier.height(10.dp))

    AlphaSlider(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        controller = controller,
        initialColor = color.let {
            Color(
                it.x,
                it.y,
                it.z,
                it.w
            )
        }
    )

    Spacer(Modifier.height(10.dp))


    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        AlphaTile(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(6.dp)),
            controller = controller
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { sizeXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.size.value
                toolService.gridTool.size.value = Vector2f(it, vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { sizeYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.size.value
                toolService.gridTool.size.value = Vector2f(vec.x, it)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.offset.value
                toolService.gridTool.offset.value = Vector2f(it, vec.y)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.offset.value
                toolService.gridTool.offset.value = Vector2f(vec.x, it)
            }
    }


}