package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.joml.Vector4f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import kotlin.getValue
import kotlin.let

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun MiscToolConfig() {
    val toolService by rememberInstance<ToolService<*>>()
    val editorService by rememberInstance<EditorService<*>>()
    val scenario by editorService.scenario.collectAsState()
    val commonData = scenario?.commonData ?: return
    val debugInfo by toolService.miscTool.debugInfo.collectAsState()
    val controller = rememberColorPickerController()
    val controller2 = rememberColorPickerController()


    var descriptionTextFieldValue by remember(commonData) {
        mutableStateOf(
            TextFieldValue(
                text = commonData.description,
                selection = TextRange(commonData.description.length)
            )
        )
    }

    var textFieldValue by remember(commonData) {
        mutableStateOf(
            TextFieldValue(
                text = commonData.name,
                selection = TextRange(commonData.name.length)
            )
        )
    }


    Column {

        Spacer(Modifier.height(4.dp))

        Text("Name:")
        TextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue

                val finalText: String = newValue.text
                editorService.updateCommonData {
                    it.copy(
                        name = finalText
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                if (!focus.isFocused) {
                    editorService.flushCompound()
                }
            },
            placeholder = { Text("Empty") }
        )

        Spacer(Modifier.height(4.dp))

        Text("Description:")
        TextField(
            value = descriptionTextFieldValue,
            onValueChange = { newValue ->
                descriptionTextFieldValue = newValue

                val finalText: String = newValue.text
                editorService.updateCommonData {
                    it.copy(
                        description = finalText
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                if (!focus.isFocused) {
                    editorService.flushCompound()
                }
            },
            placeholder = { Text("Empty") }
        )

        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text("Measure render performance GPU")
            Spacer(Modifier.width(4.dp))
            Checkbox(debugInfo.measurePerformanceGPU, onCheckedChange = {
                toolService.miscTool.debugInfo.value = toolService.miscTool.debugInfo.value.copy(
                    measurePerformanceGPU = it
                )
            })
        }


        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text("Measure render performance CPU")
            Spacer(Modifier.width(4.dp))
            Checkbox(debugInfo.measurePerformanceCPU, onCheckedChange = {
                toolService.miscTool.debugInfo.value = toolService.miscTool.debugInfo.value.copy(
                    measurePerformanceCPU = it
                )
            })
        }

        Spacer(Modifier.height(4.dp))
        let {
            Text("First height color:")
            Text(
                "Value: " +
                        "${debugInfo.firstHeightColor.x} " +
                        "${debugInfo.firstHeightColor.y} " +
                        "${debugInfo.firstHeightColor.z} " +
                        "${debugInfo.firstHeightColor.w} "
            )
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(10.dp),
                controller = controller,

                initialColor = debugInfo.firstHeightColor.let {
                    Color(
                        it.x,
                        it.y,
                        it.z,
                        it.w
                    )
                },

                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    toolService.miscTool.debugInfo.value = toolService.miscTool.debugInfo.value.copy(
                        firstHeightColor = Vector4f(
                            colorEnvelope.color.red,
                            colorEnvelope.color.green,
                            colorEnvelope.color.blue,
                            colorEnvelope.color.alpha
                        )
                    )
                }
            )

            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                controller = controller,
                initialColor = debugInfo.firstHeightColor.let {
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
                initialColor = debugInfo.firstHeightColor.let {
                    Color(
                        it.x,
                        it.y,
                        it.z,
                        it.w
                    )
                }
            )
        }

        let {
            Text("Second height color:")
            Text(
                "Value: " +
                        "${debugInfo.secondHeightColor.x} " +
                        "${debugInfo.secondHeightColor.y} " +
                        "${debugInfo.secondHeightColor.z} " +
                        "${debugInfo.secondHeightColor.w} "
            )
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(10.dp),
                controller = controller2,

                initialColor = debugInfo.secondHeightColor.let {
                    Color(
                        it.x,
                        it.y,
                        it.z,
                        it.w
                    )
                },

                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    toolService.miscTool.debugInfo.value = toolService.miscTool.debugInfo.value.copy(
                        secondHeightColor = Vector4f(
                            colorEnvelope.color.red,
                            colorEnvelope.color.green,
                            colorEnvelope.color.blue,
                            colorEnvelope.color.alpha
                        )
                    )
                }
            )

            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                controller = controller2,
                initialColor = debugInfo.secondHeightColor.let {
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
                controller = controller2,
                initialColor = debugInfo.secondHeightColor.let {
                    Color(
                        it.x,
                        it.y,
                        it.z,
                        it.w
                    )
                }
            )
        }

    }
}