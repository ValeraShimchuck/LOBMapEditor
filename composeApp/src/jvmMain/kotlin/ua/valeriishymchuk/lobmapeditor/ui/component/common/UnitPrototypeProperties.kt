package ua.valeriishymchuk.lobmapeditor.ui.component.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.Text
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.player.Player
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitFormation
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitStatus
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import kotlin.math.max

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalJewelApi::class)
fun UnitPrototypeProperties(
    currentUnit: GameUnit,
    onUnitUpdate: (GameUnit) -> Unit,
    flush: () -> Unit = {}
) {
    val diEditorService by rememberInstance<EditorService<*>>();
    val editorService = diEditorService as? PresetEditorService ?: return
    val scenarioNullable by editorService.scenario.collectAsState()
    val scenario = scenarioNullable ?: return
    val currentPlayer = currentUnit.owner.getValue(scenario.players::get)

    println("Updated current unit $currentUnit")

    fun updateUnit(updater: (GameUnit) -> GameUnit, flush: Boolean = true) {
        onUnitUpdate(updater(currentUnit))
        if (flush) {
            flush()
        }
    }

    // Player owner of the unit
    Text("Owner:")
    DropDown(
        currentUnit.owner,
        scenario.players.withIndex().sortedByDescending { it.index }.map { Reference<Int, Player>(it.index) },
        { _, reference ->
            val value = reference.getValue(scenario.players::get)
            "${reference.key + 1} ${value.team}"
        },
        { _, reference ->
            updateUnit({
                it.copy(owner = reference)
            })
        }
    )

    DefaultVSpacer()

    // name
    Text("Name:")
    ReactiveTextField(
        currentUnit,
        currentUnit.name ?: "",
        {
            updateUnit({ unit ->
                println("Copying unit $currentUnit\n $unit")
                unit.copy(name = it.takeIf { str -> str.isNotBlank() })
            }, false)
        },
        "Unit name... (Blank - default name)",
        "Name",
        onFocusLoss = flush
    )

    DefaultVSpacer()

    // Unit Type
    Text("Type:")
    DropDown(
        currentUnit.type,
        GameUnitType.entries.toList(),
        { _, type ->
            type.name
        },
        { _, type ->
            updateUnit({ unit ->
                unit.copy(
                    type = type,
                    formation = Unit.let {
                        if (type.hasFormation) {
                            currentUnit.formation ?: UnitFormation.MASS
                        } else null
                    },
                    stamina = type.defaultStamina,
                    health = type.defaultHealth,
                    organization = type.defaultOrganization
                )
            })
        }
    )
    DefaultVSpacer()

    // Angle
    Text("Angle:")
    AnglePropertyComponent(
        currentUnit.rotation,
        {
            updateUnit({ unit ->
                unit.copy(rotation = it)
            }, false)
        },
        currentPlayer.team.color,
        onFlush = flush
    )

    DefaultVSpacer()

    // Status
    Text("Status:")
    DropDown(
        currentUnit.status,
        UnitStatus.entries.toList(),
        { _, status ->
            status.name
        },
        { _, status ->
            updateUnit({ unit ->
                unit.copy(status = status)
            })
        }
    )


    // Formation
    if (currentUnit.type.hasFormation) {
        DefaultVSpacer()

        Text("Formation:")
        DropDown(
            currentUnit.formation ?: UnitFormation.MASS,
            UnitFormation.entries.toList(),
            { _, formation ->
                formation.name
            },
            { _, formation ->
                updateUnit({ unit ->
                    unit.copy(formation = formation)
                })
            }
        )
    }

    DefaultVSpacer()

    // Health
    IntTextField(
        currentUnit,
        { currentUnit.health },
        { newHealth ->
            updateUnit({ unit ->
                unit.copy(health = newHealth)
            }, false)
        },
        onFocusLoss = flush,
        adjustValue = {
            max(it, GameUnit.MIN_HEALTH)
        },
        leadingText = "Health"
    )

    DefaultVSpacer()

    // Organization
    IntTextField(
        currentUnit,
        { currentUnit.organization },
        { newOrganization ->
            updateUnit({ unit ->
                unit.copy(organization = newOrganization)
            }, false)
        },
        onFocusLoss = flush,
        adjustValue = {
            max(it, GameUnit.MIN_ORGANIZATION)
        },
        leadingText = "Org"
    )

    // Stamina
    if (currentUnit.stamina != null) {
        DefaultVSpacer()

        IntTextField(
            currentUnit,
            { currentUnit.stamina!! },
            { newStamina ->
                updateUnit({ unit ->
                    unit.copy(stamina = newStamina)
                }, false)
            },
            onFocusLoss = flush,
            adjustValue = {
                max(it, GameUnit.MIN_STAMINA)
            },
            leadingText = "Stamina"
        )
    }




}