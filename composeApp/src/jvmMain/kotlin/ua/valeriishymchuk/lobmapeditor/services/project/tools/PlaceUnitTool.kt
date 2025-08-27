package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolUiInfo

object PlaceUnitTool : PresetTool() {
    var currentUnit = MutableStateFlow(GameUnit(
        null,
        Reference(0),
        Position(0f, 0f),
        0f,
        GameUnitType.LINE_INFANTRY
    ))

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
        editorService.flushCompoundCommon()
    }

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Vcs.ShelveSilent,
        "Place unit",
        "Place unit: place a unit on map"
    )

    override fun useToolAt(
        editorService: EditorService<GameScenario.Preset>,
        x: Float,
        y: Float,
        flushCompoundCommands: Boolean,
    ): Boolean {

        editorService.execute(UpdateGameUnitListCommand(
            editorService.scenario.units,
            editorService.scenario.units.toMutableList().apply {
                add(currentUnit.value.copy(
                    position = Position(x, y)
                ))
            }
        ))

        return true
    }

    override val canBeUsedMultipleTimes: Boolean = false


}