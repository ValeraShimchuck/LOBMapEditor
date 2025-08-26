package ua.valeriishymchuk.lobmapeditor.services.project.tools

import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Objective
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.project.ToolUiInfo

object PlaceObjectiveTool : PresetTool() {
    var currentObjective: Objective = Objective(
//        Reference(0),
        null,
        null,
        Position(0f, 0f)
    )

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
        editorService.flushCompoundCommon()
    }

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Ide.FeedbackRatingOn,
        "Place objective",
        "Place objective: place an objective on map"
    )

    override fun useToolAt(
        editorService: EditorService<GameScenario.Preset>,
        x: Float,
        y: Float,
        flushCompoundCommands: Boolean,
    ): Boolean {

        editorService.executeCommon(UpdateObjectiveListCommand(
            editorService.scenario.objectives,
            editorService.scenario.objectives.toMutableList().apply {
                add(currentObjective.copy(
                    position = Position(x, y)
                ))
            }
        ))

        return true
    }

    override val canBeUsedMultipleTimes: Boolean = false
}