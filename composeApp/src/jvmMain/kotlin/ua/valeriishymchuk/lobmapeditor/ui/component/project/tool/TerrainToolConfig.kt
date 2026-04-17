package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.Terrain
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.TerrainTool
import kotlin.getValue

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun TerrainToolConfig() {

    val currentTerrain by TerrainTool.terrain.collectAsState()


    val editorService by rememberInstance<EditorService<*>>();
    val scenario by editorService.scenario.collectAsState()

    if (scenario == null) return

    val popupManager = remember { PopupManager() }

    var confirmFlow by remember(scenario) { mutableStateOf(false) }


    var scenarioDimensionX by remember {
        mutableStateOf(
            TextFieldValue(
                text = scenario!!.map.widthPixels.toString(),
                selection = TextRange(scenario!!.map.widthPixels.toString().length)
            )
        )
    }
    var scenarioDimensionY by remember {
        mutableStateOf(
            TextFieldValue(
                text = scenario!!.map.heightPixels.toString(),
                selection = TextRange(scenario!!.map.heightPixels.toString().length)
            )
        )
    }

    BrushToolConfig(TerrainTool)

    ComboBox(
        labelText = currentTerrain.name, popupManager = popupManager, popupContent = {
            VerticallyScrollableContainer {
                Column {
                    TerrainType.entries.sortedByDescending {
                        it.dominance
                    }.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                TerrainTool.terrain.value = item
                                popupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = item.name,
                            )
                        }

                    }
                }
            }
        })


    Spacer(Modifier.height(50.dp))

    Text("Map dimensions")
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = scenarioDimensionX,
            onValueChange = { newValue ->
                scenarioDimensionX = newValue
                confirmFlow = false
            },
            modifier = Modifier.widthIn(max = 80.dp).onFocusChanged { focus ->
                if (!focus.isFocused) {
                    scenarioDimensionX = scenarioDimensionX.copy(
                        text = scenarioDimensionX.text
                            .replace(Regex("[^0-9]"), "").let { str ->
                                val value = str.toIntOrNull() ?: 0
                                val coercedValue = value.coerceIn(
                                    Terrain.MIN_TERRAIN_PIXELS_MAP_X..Terrain.MAX_TERRAIN_PIXELS_MAP_X
                                )
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )
                }
            },
            leadingIcon = { Text("X") }
        )
        Spacer(Modifier.height(4.dp))


        TextField(
            value = scenarioDimensionY,
            onValueChange = { newValue ->
                scenarioDimensionY = newValue
                confirmFlow = false
            },
            modifier = Modifier.widthIn(max = 80.dp).onFocusChanged { focus ->
                if (!focus.isFocused) {
                    scenarioDimensionY = scenarioDimensionY.copy(
                        text = scenarioDimensionY.text
                            .replace(Regex("[^0-9]"), "").let { str ->
                                val value = str.toIntOrNull() ?: 0
                                val coercedValue = value.coerceIn(
                                    Terrain.MIN_TERRAIN_PIXELS_MAP_Y..Terrain.MAX_TERRAIN_PIXELS_MAP_Y
                                )
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )
                }
            },
            leadingIcon = { Text("Y") }
        )
    }
    Spacer(Modifier.height(4.dp))



    if (confirmFlow) {
        Text("It is an irreversible action, are you sure?")
        Row {
            DefaultButton(
                onClick = {
                    val newX = scenarioDimensionX.text.toInt()
                    val newY = scenarioDimensionY.text.toInt()

                    val newScenario = scenario!!.withCommonData(
                        scenario!!.commonData.copy(
                            map = scenario!!.commonData.map.resize(newX, newY)
                        )
                    )
                    val method = editorService.javaClass.getMethod("importScenario", GameScenario::class.java)
                    method.invoke(editorService, newScenario)
                    confirmFlow = false

                },
            ) {
                Text("Confirm")
            }

            Spacer(Modifier.width(10.dp))

            OutlinedButton(
                onClick = {
                    confirmFlow = false
                },
            ) {
                Text("Cancel")
            }
        }


    } else {
        DefaultButton(
            onClick = {
                confirmFlow = true
            },
        ) {
            Text("Resize map")
        }
    }


}