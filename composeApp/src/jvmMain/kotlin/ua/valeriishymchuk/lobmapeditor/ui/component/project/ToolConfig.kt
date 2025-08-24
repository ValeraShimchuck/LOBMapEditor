package ua.valeriishymchuk.lobmapeditor.ui.component.project

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool
import kotlin.math.roundToInt

@Composable
fun ToolConfig(modifier: Modifier = Modifier) {
    val toolService by rememberInstance<ToolService>()
    val currentTool by toolService.currentTool.collectAsState()

    Column(modifier) {
        GroupHeader(
            "Configuration for tool: ${currentTool.uiInfo.name}",
            startComponent = { Icon(AllIconsKeys.General.Settings, null) }
        )
        Spacer(Modifier.height(8.dp))
        when (currentTool) {
            is TerrainTool -> TerrainToolConfig()
            is HeightTool -> HeightToolConfig()
        }
    }
}

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
private fun TerrainToolConfig() {

    val currentTerrain by TerrainTool.terrain.collectAsState()

    val popupManager = remember { PopupManager() }

    ComboBox(
        labelText = currentTerrain.name,
        popupManager = popupManager,
        popupContent = {
            VerticallyScrollableContainer {
                Column {
                    TerrainType.entries.sortedByDescending {
                        it.dominance
                    }.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                                .onClick {
                                    TerrainTool.terrain.value = item
                                    popupManager.setPopupVisible(false)
                                }
                        ) {
                            Text(
                                text = item.name,
                            )
                        }

                    }
                }
            }
        }
    )

}

@Composable
private fun HeightToolConfig() {
    val currentHeight by HeightTool.height.collectAsState()

    var value by remember { mutableStateOf(currentHeight.toFloat()) }

    LaunchedEffect(value) {
        HeightTool.height.value = value.roundToInt()
    }


    Text("Height: $currentHeight")
    Slider(
        value = value, // Float
        onValueChange = { newValue ->
            value = newValue // просто оновлюємо
        },
        valueRange = 0f..10f, // будь-яке значення між 1 та 10
        steps = 0, // без кроків
        modifier = Modifier.fillMaxWidth()
    )
}