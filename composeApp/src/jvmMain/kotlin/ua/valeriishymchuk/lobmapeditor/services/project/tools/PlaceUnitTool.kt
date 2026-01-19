package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.joml.Math
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitFormation
import ua.valeriishymchuk.lobmapeditor.domain.unit.UnitStatus
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

object PlaceUnitTool : PresetTool() {
    var currentUnit = MutableStateFlow(GameUnit(
        null,
        Reference(0),
        Position(0f, 0f),
        Math.toRadians(90f),
        GameUnitType.LINE_INFANTRY,
        UnitStatus.STANDING,
        UnitFormation.MASS,
        GameUnitType.LINE_INFANTRY.defaultHealth,
        GameUnitType.LINE_INFANTRY.defaultOrganization,
        GameUnitType.LINE_INFANTRY.defaultStamina
    ))

    override fun flush(editorService: PresetEditorService) {
        editorService.flushCompoundCommon()
    }

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Vcs.ShelveSilent,
        "Place unit",
        "Place unit: place a unit on map"
    )

    override fun useToolAt(
        editorService: PresetEditorService,
        x: Float,
        y: Float,
        flushCompoundCommands: Boolean,
    ): Boolean {

        editorService.execute(UpdateGameUnitListCommand(
            editorService.scenario.value!!.units,
            editorService.scenario.value!!.units.toMutableList().apply {
                add(currentUnit.value.copy(
                    position = Position(x, y)
                ))
            }
        ))

        return true
    }

    override val canBeUsedMultipleTimes: Boolean = false


}