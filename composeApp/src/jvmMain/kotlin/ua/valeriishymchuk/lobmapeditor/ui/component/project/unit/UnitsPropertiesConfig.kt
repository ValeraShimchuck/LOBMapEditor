package ua.valeriishymchuk.lobmapeditor.ui.component.project.unit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.theme.defaultButtonStyle
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitFormation
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitStatus
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService.Companion.deleteUnits
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.AngleDial
import kotlin.math.max

@OptIn(ExperimentalJewelApi::class, ExperimentalFoundationApi::class)
@Composable
fun UnitsPropertiesConfig() {

    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val scenario by editorService.scenario.collectAsState()
    val rawSelection by editorService.selectedUnits.collectAsState()


    if (rawSelection.isEmpty()) return

    val selection by derivedStateOf {
        rawSelection.mapNotNull { unit -> unit.getValueOrNull(scenario!!.units::getOrNull) }
    }

    if (selection.isEmpty()) return

    fun updateSelectedUnits(unitMap: (GameUnit) -> GameUnit) {
        val copyList = scenario!!.units.toMutableList()
        val rawSelectionReferences = rawSelection.map { it.key }

        val command = UpdateGameUnitListCommand(
            scenario!!.units,
            copyList.mapIndexed { index, unit ->
                if (!rawSelectionReferences.contains(index)) return@mapIndexed unit
                unitMap(unit)
            }
        )

        if (command.newGameUnits == command.oldGameUnits) return

        editorService.executeCompound(command)

    }

    val isNameMixed by derivedStateOf { selection.map { it.name }.distinct().size > 1 }
    val isOwnerMixed by derivedStateOf { selection.map { it.owner }.distinct().size > 1 }
    val isUnitTypeMixed by derivedStateOf { selection.map { it.type }.distinct().size > 1 }
    val isXPositionMixed by derivedStateOf { selection.map { it.position.x }.distinct().size > 1 }
    val isYPositionMixed by derivedStateOf { selection.map { it.position.y }.distinct().size > 1 }
    val isRotationMixed by derivedStateOf { selection.map { it.rotationRadians }.distinct().size > 1 }
    val isStatusMixed by derivedStateOf { selection.map { it.status }.distinct().size > 1 }
    val isFormationMixed by derivedStateOf { selection.map { it.formation }.distinct().size > 1 }
    val canFormationBeModified by derivedStateOf { selection.none { it.formation == null } }
    val isHealthMixed by derivedStateOf { selection.map { it.health }.distinct().size > 1 }
    val isOrganizationMixed by derivedStateOf { selection.map { it.organization }.distinct().size > 1 }
    val isStaminaMixed by derivedStateOf { selection.map { it.stamina }.distinct().size > 1 }
    val canStaminaBeModified by derivedStateOf { selection.none { it.stamina == null } }

    val ownerPopupManager = remember { PopupManager() }
    val unityTypePopupManager = remember { PopupManager() }
    val statusPopupManager = remember { PopupManager() }
    val formationPopupManager = remember { PopupManager() }

    var xPositionTextFieldValue by remember {
        mutableStateOf(
            Unit.let {
                val currentText = when {
                    selection.isEmpty() -> ""
                    isXPositionMixed -> ""
                    else -> selection.map { it.position.x }.distinct().firstOrNull()?.toString() ?: ""
                }
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // Or calculate appropriate position
                )
            }
        )
    }

    // xPosition Selection handler
    LaunchedEffect(selection) {
        val textValue = xPositionTextFieldValue.text.toFloatOrNull()
        val xValue = selection.map { it.position.x }.distinct().firstOrNull()
        if (textValue != xValue || (textValue != null && isXPositionMixed)) {
            val finalValue: String = if (xValue != null && !isXPositionMixed) xValue.toString()
            else ""
            xPositionTextFieldValue = xPositionTextFieldValue.copy(text = finalValue)
        }
    }


    var yPositionTextFieldValue by remember {
        mutableStateOf(
            Unit.let {
                val currentText = when {
                    selection.isEmpty() -> ""
                    isYPositionMixed -> ""
                    else -> selection.map { it.position.y }.distinct().firstOrNull()?.toString() ?: ""
                }
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // Or calculate appropriate position
                )
            }
        )
    }

    // yPosition Selection handler
    LaunchedEffect(selection) {

        val textValue = yPositionTextFieldValue.text.toFloatOrNull()
        val yValue = selection.map { it.position.y }.distinct().firstOrNull()
        if (textValue != yValue || (textValue != null && isYPositionMixed)) {
            val finalValue: String = if (yValue != null && !isYPositionMixed) yValue.toString()
            else ""
            yPositionTextFieldValue = yPositionTextFieldValue.copy(text = finalValue)
        }
    }

    var rotationTextFieldValue by remember {
        mutableStateOf(
            Unit.let {

                val currentText = when {
                    selection.isEmpty() -> ""
                    isRotationMixed -> ""
                    else -> selection.map {
                        org.joml.Math.toDegrees(it.rotationRadians)
                    }.distinct().firstOrNull()?.toString() ?: ""
                }

                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // Or calculate appropriate position
                )
            }
        )
    }

    // rotation Selection handler
    LaunchedEffect(selection) {

        val textValue = rotationTextFieldValue.text.toFloatOrNull()
        val rotation = selection.map {
            org.joml.Math.toDegrees(it.rotationRadians)
        }.distinct().firstOrNull()
        if (textValue != rotation || (textValue != null && isRotationMixed)) {
            val finalValue: String = if (rotation != null && !isRotationMixed) rotation.toString()
            else ""
            rotationTextFieldValue = rotationTextFieldValue.copy(text = finalValue)
        }
    }

    var healthTextFieldValue by remember {
        mutableStateOf(
            Unit.let {
                val currentText = when {
                    selection.isEmpty() -> ""
                    isHealthMixed -> ""
                    else -> selection.map { it.health }.distinct().firstOrNull()?.toString() ?: ""
                }
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length)
                )
            }
        )
    }

    // health Selection handler
    LaunchedEffect(selection) {

        val textValue = healthTextFieldValue.text.toIntOrNull()
        val value = selection.map { it.health }.distinct().firstOrNull()
        if (textValue != value || (textValue != null && isHealthMixed)) {
            val finalValue: String = if (value != null && !isHealthMixed) value.toString()
            else ""
            healthTextFieldValue = healthTextFieldValue.copy(text = finalValue)
        }
    }

    var organizationTextFieldValue by remember {
        mutableStateOf(
            Unit.let {
                val currentText = when {
                    selection.isEmpty() -> ""
                    isOrganizationMixed -> ""
                    else -> selection.map { it.organization }.distinct().firstOrNull()?.toString() ?: ""
                }
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length)
                )
            }
        )
    }

    // organization Selection handler
    LaunchedEffect(selection) {

        val textValue = organizationTextFieldValue.text.toIntOrNull()
        val value = selection.map { it.organization }.distinct().firstOrNull()
        if (textValue != value || (textValue != null && isOrganizationMixed)) {
            val finalValue: String = if (value != null && !isOrganizationMixed) value.toString()
            else ""
            organizationTextFieldValue = organizationTextFieldValue.copy(text = finalValue)
        }
    }

    var staminaTextFieldValue by remember {
        mutableStateOf(
            Unit.let {
                val currentText = when {
                    selection.isEmpty() -> ""
                    isStaminaMixed -> ""
                    else -> selection.map { it.stamina }.distinct().firstOrNull()?.toString() ?: ""
                }
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length)
                )
            }
        )
    }

    // stamina Selection handler
    LaunchedEffect(selection) {

        val textValue = staminaTextFieldValue.text.toIntOrNull()
        val value = selection.map { it.stamina }.distinct().firstOrNull()
        if (textValue != value || (textValue != null && isStaminaMixed)) {
            val finalValue: String = if (value != null && !isStaminaMixed) value.toString()
            else ""
            staminaTextFieldValue = staminaTextFieldValue.copy(text = finalValue)
        }
    }

    var textFieldValue by remember(rawSelection) {
        mutableStateOf(
            Unit.let {
                val currentText = when {
                    selection.isEmpty() -> ""
                    selection.map { it.name }.distinct().size > 1 -> ""
                    else -> selection.map { it.name }.distinct().firstOrNull() ?: ""
                }
                TextFieldValue(
                    text = currentText,
                    selection = TextRange(currentText.length) // Or calculate appropriate position
                )
            }
        )
    }

    VerticallyScrollableContainer {
        Column {
            // Name
            Spacer(Modifier.height(4.dp))
            Text("Name:")
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    textFieldValue = newValue

                    val finalText: String? = newValue.text.ifEmpty { null }
                    updateSelectedUnits { it.copy(name = finalText) }
                },
                modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        editorService.flushCompound()
                    }
                },
                placeholder = { Text(if (isNameMixed) "Mixed" else "Empty") }
            )

            Spacer(Modifier.height(10.dp))

            // Owner
            Text("Owner:")
            ComboBox(
                labelText = if (isOwnerMixed) "Mixed" else let {
                    val owner = selection.map { it.owner }.distinct().firstOrNull() ?: return@let ""
                    "${owner.key + 1} ${scenario!!.players[owner.key].team}"
                },
                popupManager = ownerPopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            scenario!!.players.withIndex().sortedByDescending {
                                it.index
                            }.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

//                                        PlaceUnitTool.currentUnit.value = currentUnit.copy(
//                                            owner = Reference(item.index)
//                                        )
                                        updateSelectedUnits { unit ->
                                            unit.copy(owner = Reference(item.index))
                                        }
                                        editorService.flushCompound()
                                        ownerPopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "${item.index + 1} ${item.value.team}",
                                    )
                                }

                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            // Type
            Text("Type:")
            ComboBox(
                labelText = if (isUnitTypeMixed) "Mixed" else let {
                    val unityType = selection.map { it.type }.distinct().firstOrNull() ?: return@let ""
                    "$unityType"
                },
                popupManager = unityTypePopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            GameUnitType.entries.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        updateSelectedUnits { unit ->
                                            unit.copy(
                                                type = item,
                                                formation = Unit.let {
                                                    if (item.hasFormation) {
                                                        unit.formation ?: UnitFormation.MASS
                                                    } else null
                                                },
                                                stamina = item.defaultStamina,
                                                health = item.defaultHealth,
                                                organization = item.defaultOrganization
                                            )
                                        }
                                        editorService.flushCompound()
                                        unityTypePopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "$item",
                                    )
                                }

                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            // Position
            Text("Position:")
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                TextField(
                    value = xPositionTextFieldValue,
                    onValueChange = { newValue ->
                        // Simply update the state with the complete new value
                        xPositionTextFieldValue = newValue
                        xPositionTextFieldValue = xPositionTextFieldValue.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, scenario!!.map.widthPixels.toFloat())
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = xPositionTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateSelectedUnits { it.copy(position = it.position.copy(x = finalText)) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    placeholder = { Text(if (isXPositionMixed) "Mixed" else "0") },

                    leadingIcon = {
                        Row {
                            Text("X", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )
                Spacer(Modifier.width(4.dp))

                TextField(
                    value = yPositionTextFieldValue,
                    onValueChange = { newValue ->
                        // Simply update the state with the complete new value
                        yPositionTextFieldValue = newValue
                        yPositionTextFieldValue = yPositionTextFieldValue.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, scenario!!.map.heightPixels.toFloat())
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = yPositionTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        updateSelectedUnits { it.copy(position = it.position.copy(y = finalText)) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    placeholder = { Text(if (isYPositionMixed) "Mixed" else "0") },

                    leadingIcon = {
                        Row {
                            Text("Y", color = JewelTheme.globalColors.text.info)
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                )
            }

            // Rotation
            Spacer(Modifier.height(10.dp))
            Text("Rotation:")
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                AngleDial(
                    org.joml.Math.toRadians(rotationTextFieldValue.text.toFloatOrNull() ?: 0f),
                    color = Color(230, 230, 230),
                    modifier = Modifier.size(200.dp)
                )

                if (isRotationMixed) {
                    Spacer(Modifier.height(4.dp))
                    Text("Mixed")
                    Spacer(Modifier.height(4.dp))
                }

                TextField(
                    value = rotationTextFieldValue,
                    onValueChange = { newValue ->
                        // Simply update the state with the complete new value
                        rotationTextFieldValue = newValue
                        rotationTextFieldValue = rotationTextFieldValue.copy(
                            text = newValue.text
                                .replace(Regex("[^0-9.]"), "").let { str ->
                                    val value = str.toFloatOrNull() ?: return@let str
                                    val coercedValue = value.coerceIn(0f, 359f)
                                    if (coercedValue == value) return@let str
                                    coercedValue.toString()
                                }
                        )


                        val finalText: Float = org.joml.Math.toRadians(
                            rotationTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f
                        )
                        updateSelectedUnits { it.copy(rotationRadians = finalText) }
                    },
                    modifier = Modifier.onFocusChanged { focus ->
                        if (!focus.isFocused) {
                            editorService.flushCompound()
                        }
                    },
                    placeholder = { Text(if (isRotationMixed) "Mixed" else "0") },

                )

                Slider(
                    value = org.joml.Math.toRadians(rotationTextFieldValue.text.toFloatOrNull() ?: 0f),
                    onValueChange = { newRotation ->

//                        rotationTextFieldValue = newRotation
                        rotationTextFieldValue = rotationTextFieldValue.copy(
                            text = org.joml.Math.toDegrees(newRotation).coerceIn(0f, 359f).toString()
                        )
                        updateSelectedUnits { it.copy(rotationRadians = org.joml.Math.toRadians(rotationTextFieldValue.text.ifEmpty { "0" }.toFloatOrNull() ?: 0f)) }
                    },
                    valueRange = 0f..(2 * Math.PI).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(10.dp))

            // Unit status
            Text("Status:")
            ComboBox(
                labelText = if (isStatusMixed) "Mixed" else let {
                    val status = selection.map { it.status }.distinct().firstOrNull() ?: return@let ""
                    "$status"
                },
                popupManager = statusPopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            UnitStatus.entries.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        updateSelectedUnits { unit ->
                                            unit.copy(status = item)
                                        }
                                        editorService.flushCompound()
                                        statusPopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "$item",
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            // Unit formation
            Text("Formation:")
            ComboBox(
                labelText = if (isFormationMixed) "Mixed" else let {
                    val formation = selection.map { it.formation }.distinct().firstOrNull() ?: return@let ""
                    "$formation"
                },
                enabled = canFormationBeModified,
                popupManager = formationPopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            UnitFormation.entries.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        updateSelectedUnits { unit ->
                                            unit.copy(formation = item)
                                        }
                                        editorService.flushCompound()
                                        formationPopupManager.setPopupVisible(false)
                                    }) {
                                    Text(
                                        text = "$item",
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(10.dp))
            Text("Health:")
            TextField(
                value = healthTextFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    healthTextFieldValue = newValue
                    healthTextFieldValue = healthTextFieldValue.copy(
                        text = newValue.text
                            .replace(Regex("[^0-9.]"), "").let { str ->
                                val value = str.toIntOrNull() ?: return@let str
                                val coercedValue = max(value, GameUnit.MIN_HEALTH)
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )


                    val finalText: Int = healthTextFieldValue.text.ifEmpty { GameUnit.MIN_HEALTH.toString() }.toIntOrNull() ?: GameUnit.MIN_HEALTH
                    updateSelectedUnits { it.copy(health = finalText) }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        editorService.flushCompound()
                    }
                },
                placeholder = { Text(if (isHealthMixed) "Mixed" else "0") }
            )

            Spacer(Modifier.height(10.dp))
            Text("Organization:")
            TextField(
                value = organizationTextFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    organizationTextFieldValue = newValue
                    organizationTextFieldValue = organizationTextFieldValue.copy(
                        text = newValue.text
                            .replace(Regex("[^0-9.]"), "").let { str ->
                                val value = str.toIntOrNull() ?: return@let str
                                val coercedValue = max(value, GameUnit.MIN_ORGANIZATION)
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )


                    val finalText: Int = organizationTextFieldValue.text.ifEmpty {
                        GameUnit.MIN_ORGANIZATION.toString()
                    }.toIntOrNull() ?: GameUnit.MIN_ORGANIZATION
                    updateSelectedUnits { it.copy(organization = finalText) }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        editorService.flushCompound()
                    }
                },
                placeholder = { Text(if (isOrganizationMixed) "Mixed" else "0") }
            )

            Spacer(Modifier.height(10.dp))
            Text("Stamina:")
            TextField(
                enabled = canStaminaBeModified,
                value = staminaTextFieldValue,
                onValueChange = { newValue ->
                    // Simply update the state with the complete new value
                    staminaTextFieldValue = newValue
                    staminaTextFieldValue = staminaTextFieldValue.copy(
                        text = newValue.text
                            .replace(Regex("[^0-9.]"), "").let { str ->
                                val value = str.toIntOrNull() ?: return@let str
                                val coercedValue = max(value, GameUnit.MIN_STAMINA)
                                if (coercedValue == value) return@let str
                                coercedValue.toString()
                            }
                    )


                    val finalText: Int = staminaTextFieldValue.text.ifEmpty {
                        GameUnit.MIN_STAMINA.toString()
                    }.toIntOrNull() ?: GameUnit.MIN_STAMINA
                    updateSelectedUnits { it.copy(stamina = finalText) }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        editorService.flushCompound()
                    }
                },
                placeholder = { Text(if (isStaminaMixed) "Mixed" else "0") }
            )

            // Delete Unit/Units
            DefaultButton(
                style = JewelTheme.defaultButtonStyle.let { style ->
                    val color = Color(196, 27, 27, 255)
                    val color2 = Color(182, 25, 25, 255)
                    val color3 = Color(165, 21, 21, 255)
                    ButtonStyle(
                        colors = ButtonColors(
                            Brush.linearGradient(listOf(color, color)),
                            style.colors.backgroundDisabled,
                            Brush.linearGradient(listOf(color, color)),
                            Brush.linearGradient(listOf(color3, color3)),
                            Brush.linearGradient(listOf(color2, color2)),
                            style.colors.content,
                            style.colors.contentDisabled,
                            style.colors.contentFocused,
                            style.colors.contentPressed,
                            style.colors.contentHovered,
                            style.colors.border,
                            style.colors.borderDisabled,
                            style.colors.borderFocused,
                            style.colors.borderPressed,
                            style.colors.borderHovered
                        ),
                        metrics = style.metrics,
                        focusOutlineAlignment = style.focusOutlineAlignment
                    )
                },
                onClick = {
                    editorService.deleteUnits(rawSelection)
                },
            ) {
                Text(if (selection.size > 1) "Delete units" else "Delete unit")
            }

        }
    }
}
