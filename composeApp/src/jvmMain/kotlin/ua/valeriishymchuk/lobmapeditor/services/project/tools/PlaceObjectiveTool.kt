package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.objective.ObjectiveType
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

object PlaceObjectiveTool : PresetTool() {
    var currentObjective = MutableStateFlow(Objective(
//        Reference(0),
        null,
        null,
        Position(0f, 0f),
        ObjectiveType.SMALL,
        ObjectiveType.SMALL.defaultVictoryPoints
    ))

    override fun flush(editorService: EditorService<GameScenario.Preset>) {
        editorService.flushCompoundCommon()
    }

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Nodes.Favorite,
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
            editorService.scenario.value!!.objectives,
            editorService.scenario.value!!.objectives.toMutableList().apply {
                add(currentObjective.value.copy(
                    position = Position(x, y)
                ))
            }
        ))

        return true
    }

    override val canBeUsedMultipleTimes: Boolean = false
}