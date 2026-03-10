package ua.valeriishymchuk.lobmapeditor.domain.property

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.ComboBox
import org.jetbrains.jewel.ui.component.PopupManager
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitFormation
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitStatus
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.getValue
import kotlin.let
import kotlin.math.max

interface UnitProperty<SELF : UnitProperty<SELF>> : PositionProperty<SELF> {

    val owner: Reference<Int, Player>
    val type: GameUnitType
    val status: UnitStatus
    val formation: UnitFormation?
    val health: Int
    val organization: Int
    val stamina: Int?

    fun withFormation(formation: UnitFormation?): SELF
    fun withHealth(health: Int): SELF
    fun withOrganization(organization: Int): SELF
    fun withStamina(stamina: Int?): SELF
    fun withStatus(status: UnitStatus): SELF
    fun withType(type: GameUnitType): SELF
    fun withOwner(owner: Reference<Int, Player>): SELF


    companion object {


        @OptIn(ExperimentalJewelApi::class,ExperimentalFoundationApi::class)
        @Composable
        fun Component(
            selectedObjectsReferences: Set<ScenarioReference>,
            onUpdate: ((UnitProperty<*>) -> UnitProperty<*>) -> Unit,
            onFlush: () -> Unit
        )  {
            val editorService by rememberInstance<EditorService<*>>()


            val scenarioNullable by editorService.scenario.collectAsState()
            val scenario = scenarioNullable as? GameScenario.Preset ?: return

            val selectedObjectsRaw = selectedObjectsReferences.map { it.dereference(scenario) }
            if (!selectedObjectsRaw.all { it is UnitProperty }) return

            val selectedObjects = selectedObjectsRaw.map { it as UnitProperty }
            if (selectedObjects.isEmpty()) return


            val ownerPopupManager = remember { PopupManager() }
            val unityTypePopupManager = remember { PopupManager() }
            val statusPopupManager = remember { PopupManager() }
            val formationPopupManager = remember { PopupManager() }

            val isOwnerMixed by derivedStateOf { selectedObjects.map { it.owner }.distinct().size > 1 }
            val isUnitTypeMixed by derivedStateOf { selectedObjects.map { it.type }.distinct().size > 1 }
            val isStatusMixed by derivedStateOf { selectedObjects.map { it.status }.distinct().size > 1 }
            val isFormationMixed by derivedStateOf { selectedObjects.map { it.formation }.distinct().size > 1 }
            val canFormationBeModified by derivedStateOf { selectedObjects.none { it.formation == null } }
            val isHealthMixed by derivedStateOf { selectedObjects.map { it.health }.distinct().size > 1 }
            val isOrganizationMixed by derivedStateOf { selectedObjects.map { it.organization }.distinct().size > 1 }
            val isStaminaMixed by derivedStateOf { selectedObjects.map { it.stamina }.distinct().size > 1 }
            val canStaminaBeModified by derivedStateOf { selectedObjects.none { it.stamina == null } }


            var healthTextFieldValue by remember {
                mutableStateOf(
                    Unit.let {
                        val currentText = when {
                            selectedObjects.isEmpty() -> ""
                            isHealthMixed -> ""
                            else -> selectedObjects.map { it.health }.distinct().firstOrNull()?.toString() ?: ""
                        }
                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length)
                        )
                    }
                )
            }

            // health Selection handler
            LaunchedEffect(selectedObjectsRaw) {

                val textValue = healthTextFieldValue.text.toIntOrNull()
                val value = selectedObjects.map { it.health }.distinct().firstOrNull()
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
                            selectedObjects.isEmpty() -> ""
                            isOrganizationMixed -> ""
                            else -> selectedObjects.map { it.organization }.distinct().firstOrNull()?.toString() ?: ""
                        }
                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length)
                        )
                    }
                )
            }

            // organization Selection handler
            LaunchedEffect(selectedObjectsRaw) {

                val textValue = organizationTextFieldValue.text.toIntOrNull()
                val value = selectedObjects.map { it.organization }.distinct().firstOrNull()
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
                            selectedObjects.isEmpty() -> ""
                            isStaminaMixed -> ""
                            else -> selectedObjects.map { it.stamina }.distinct().firstOrNull()?.toString() ?: ""
                        }
                        TextFieldValue(
                            text = currentText,
                            selection = TextRange(currentText.length)
                        )
                    }
                )
            }

            // stamina Selection handler
            LaunchedEffect(selectedObjectsRaw) {

                val textValue = staminaTextFieldValue.text.toIntOrNull()
                val value = selectedObjects.map { it.stamina }.distinct().firstOrNull()
                if (textValue != value || (textValue != null && isStaminaMixed)) {
                    val finalValue: String = if (value != null && !isStaminaMixed) value.toString()
                    else ""
                    staminaTextFieldValue = staminaTextFieldValue.copy(text = finalValue)
                }
            }


            Spacer(Modifier.height(10.dp))

            // Owner
            Text("Owner:")
            ComboBox(
                labelText = if (isOwnerMixed) "Mixed" else let {
                    val owner = selectedObjects.map { it.owner }.distinct().firstOrNull() ?: return@let ""
                    "${owner.key + 1} ${scenario.players[owner.key].team}"
                },
                popupManager = ownerPopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            scenario.players.withIndex().sortedByDescending {
                                it.index
                            }.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {

//                                        PlaceUnitTool.currentUnit.value = currentUnit.copy(
//                                            owner = Reference(item.index)
//                                        )

                                        onUpdate {
                                            it.withOwner(Reference(item.index))
                                        }
                                        onFlush()
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
                    val unityType = selectedObjects.map { it.type }.distinct().firstOrNull() ?: return@let ""
                    "$unityType"
                },
                popupManager = unityTypePopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            GameUnitType.entries.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        onUpdate { unit ->
                                            unit.withType(item)
                                                .withFormation(Unit.let {
                                                    if (item.hasFormation) {
                                                        unit.formation ?: UnitFormation.MASS
                                                    } else null
                                                })
                                                .withStamina(item.defaultStamina)
                                                .withHealth(item.defaultHealth)
                                                .withOrganization(item.defaultOrganization)
                                        }

                                        onFlush()
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

// Unit status
            Text("Status:")
            ComboBox(
                labelText = if (isStatusMixed) "Mixed" else let {
                    val status = selectedObjects.map { it.status }.distinct().firstOrNull() ?: return@let ""
                    "$status"
                },
                popupManager = statusPopupManager,
                popupContent = {
                    VerticallyScrollableContainer {
                        Column {
                            UnitStatus.entries.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp).onClick {
                                        onUpdate {
                                            it.withStatus(item)
                                        }
                                        onFlush()
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
                    val formation = selectedObjects.map { it.formation }.distinct().firstOrNull() ?: return@let ""
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
                                        onUpdate {
                                            it.withFormation(item)
                                        }
                                        onFlush()
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
                    onUpdate {
                        it.withHealth(finalText)
                    }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        onFlush()
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
                    onUpdate {
                        it.withOrganization(finalText)
                    }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        onFlush()
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
                    onUpdate {
                        it.withStamina(finalText)
                    }
                },
                modifier = Modifier.onFocusChanged { focus ->
                    if (!focus.isFocused) {
                        onFlush()
                    }
                },
                placeholder = { Text(if (isStaminaMixed) "Mixed" else "0") }
            )
        }
    }

}