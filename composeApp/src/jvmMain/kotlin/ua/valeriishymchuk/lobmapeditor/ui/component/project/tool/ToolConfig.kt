package ua.valeriishymchuk.lobmapeditor.ui.component.project.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
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
import com.jogamp.opengl.awt.GLCanvas
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.joml.Vector2f
import org.joml.Vector4f
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.terrain.TerrainType
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.tools.GridTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.HeightTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceObjectiveTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.PlaceUnitTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.ReferenceOverlayTool
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainTool
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.AngleDial
import kotlin.getValue
import kotlin.math.roundToInt

@Composable
fun ToolConfig(modifier: Modifier = Modifier) {
    val toolService by rememberInstance<ToolService>()
    val currentTool by toolService.currentTool.collectAsState()


    val content: @Composable (() -> Unit)? = when (currentTool) {
        is TerrainTool -> {
            { TerrainToolConfig() }
        }

        is HeightTool -> {
            { HeightToolConfig() }
        }

        is PlaceUnitTool -> {
            { PlaceUnitToolConfig() }
        }

        is PlaceObjectiveTool -> {
            { PlaceObjectiveToolConfig() }
        }

        is GridTool -> {
            { GridToolConfig() }
        }

        is ReferenceOverlayTool -> {
            { ReferenceOverlayToolConfig() }
        }

        else -> null
    }

    if (content != null) {
        content()
    }
}

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
private fun TerrainToolConfig() {

    val currentTerrain by TerrainTool.terrain.collectAsState()

    val popupManager = remember { PopupManager() }

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

}

@Composable
private fun HeightToolConfig() {
    val currentHeight by HeightTool.height.collectAsState()

    var value by remember { mutableStateOf(currentHeight.toFloat()) }

    LaunchedEffect(value) {
        HeightTool.height.value = value.roundToInt()
    }


    BrushToolConfig(HeightTool)

    Text("Height: $currentHeight")
    Slider(
        value = value, // Float
        onValueChange = { newValue ->
            value = newValue // просто оновлюємо
        }, valueRange = 0f..10f, // будь-яке значення між 1 та 10
        steps = 0, // без кроків
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalJewelApi::class)
@Composable
private fun PlaceUnitToolConfig() {
    val currentUnit by PlaceUnitTool.currentUnit.collectAsState()

    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val scenario by editorService.scenario.collectAsState()


    val playerIndex = currentUnit.owner.key
    val teamPopupManager = remember { PopupManager() }
    val unitPopupManager = remember { PopupManager() }



    ComboBox(
        labelText = "$playerIndex ${scenario!!.players[playerIndex].team}",
        popupManager = teamPopupManager,
        popupContent = {
            VerticallyScrollableContainer {
                Column {
                    scenario!!.players.withIndex().sortedByDescending {
                        it.index
                    }.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

                                PlaceUnitTool.currentUnit.value = currentUnit.copy(
                                    owner = Reference(item.index)
                                )
                                teamPopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = "${item.index} ${item.value.team}",
                            )
                        }

                    }
                }
            }
        })


    val nameFieldState by remember { mutableStateOf(TextFieldState(currentUnit.name ?: "")) }

    LaunchedEffect(Unit) {
        snapshotFlow { nameFieldState.text.toString() }
            .collect { text ->
                PlaceUnitTool.currentUnit.value = currentUnit.copy(
                    name = text.takeIf { it.isNotBlank() }
                )
            }
    }


    Spacer(Modifier.height(4.dp))

    TextField(
        nameFieldState, Modifier.fillMaxWidth(), placeholder = { Text("Unit name... (Blank - default name)") })

    Spacer(Modifier.height(4.dp))


    ComboBox(
        labelText = currentUnit.type.name, popupManager = unitPopupManager, popupContent = {
            VerticallyScrollableContainer {
                Column {
                    GameUnitType.entries.sortedByDescending {
                        it.ordinal
                    }.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                PlaceUnitTool.currentUnit.value = PlaceUnitTool.currentUnit.value.copy(
                                    type = item
                                )
                                unitPopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = item.name,
                            )
                        }

                    }
                }
            }
        })

    var angle by remember { mutableStateOf(0f) }

    LaunchedEffect(angle) {
        PlaceUnitTool.currentUnit.value = currentUnit.copy(
            rotationRadians = angle
        )
    }

    Spacer(Modifier.height(4.dp))


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AngleDial(
            angle,
            color = scenario!!.players[playerIndex].team.color,
            modifier = Modifier.size(200.dp)
        )

        Slider(
            value = angle,
            onValueChange = { angle = it },
            valueRange = 0f..(2 * Math.PI).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }

}

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
private fun PlaceObjectiveToolConfig() {
    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()


    val currentObjective by PlaceObjectiveTool.currentObjective.collectAsState()
    val playerIndex = currentObjective.owner?.key
    val scenario by editorService.scenario.collectAsState()

    val playerTeamPopupManager = remember { PopupManager() }

    val objectiveNameTextFieldState = rememberTextFieldState("")

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ComboBox(labelText = playerIndex?.let { "$playerIndex ${scenario!!.players[playerIndex].team}" } ?: "No one",
            popupManager = playerTeamPopupManager,
            popupContent = {
                VerticallyScrollableContainer {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                                    owner = null
                                )
                                playerTeamPopupManager.setPopupVisible(false)
                            }) {
                            Text(
                                text = "No one",
                            )
                        }

                        scenario!!.players.withIndex().sortedByDescending {
                            it.index
                        }.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

                                    PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
                                        owner = Reference(item.index)
                                    )
                                    playerTeamPopupManager.setPopupVisible(false)
                                }) {
                                Text(
                                    text = "${item.index} ${item.value.team}",
                                )
                            }

                        }
                    }
                }
            })


        TextField(
            objectiveNameTextFieldState,
            Modifier.fillMaxWidth(),
            placeholder = { Text("Objective name... (Blank - default name)") }
        )
    }

    LaunchedEffect(objectiveNameTextFieldState) {
        PlaceObjectiveTool.currentObjective.value = currentObjective.copy(
            name = objectiveNameTextFieldState.takeIf { it.text.isNotBlank() }?.text?.toString()
        )
    }
}

@Composable
@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
private fun GridToolConfig() {
    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val toolService by rememberInstance<ToolService>()
    val canvas by rememberInstance<GLCanvas>()


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
                canvas.repaint()
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
            println(it)
            canvas.repaint()
        },
        valueRange = 0f..GameConstants.TILE_SIZE.toFloat()
    )

    Spacer(Modifier.height(4.dp))


    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .padding(10.dp),
        controller = controller,

        initialColor =  color.let {
            Color(
                it.x ,
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


            canvas.repaint()
        }
    )

    BrightnessSlider(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            ,
        controller = controller,
        initialColor =  color.let {
            Color(
                it.x ,
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
        initialColor =  color.let {
            Color(
                it.x ,
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
                canvas.repaint()
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { sizeYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.size.value
                toolService.gridTool.size.value = Vector2f(vec.y, it)
                canvas.repaint()
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.offset.value
                toolService.gridTool.offset.value = Vector2f(it, vec.y)
                canvas.repaint()
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.gridTool.offset.value
                toolService.gridTool.offset.value = Vector2f(vec.y, it)
                canvas.repaint()
            }
    }


}

@Composable
@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
private fun ReferenceOverlayToolConfig() {
    val toolService by rememberInstance<ToolService>()
    val canvas by rememberInstance<GLCanvas>()

    val enabled by toolService.refenceOverlayTool.enabled.collectAsState();
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
                canvas.repaint()
            }
        )
        Spacer(Modifier.width(4.dp))
        Text("Show reference")
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scaleXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.scale.value
                toolService.refenceOverlayTool.scale.value = Vector2f(it, vec.y)
                canvas.repaint()
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { scaleYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.scale.value
                toolService.refenceOverlayTool.scale.value = Vector2f(vec.x, it)
                canvas.repaint()
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetXTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.offset.value
                toolService.refenceOverlayTool.offset.value = Vector2f(it.coerceIn(-1f..1f), vec.y)
                canvas.repaint()
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { offsetYTextFieldState.text.toString().toFloatOrNull() }
            .collect {
                if (it == null) return@collect
                val vec = toolService.refenceOverlayTool.offset.value
                toolService.refenceOverlayTool.offset.value = Vector2f(vec.x, it.coerceIn(-1f..1f))
                canvas.repaint()
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
            canvas.repaint()
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
                canvas.repaint()
            },
            valueRange = 0f..(2 * Math.PI).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}