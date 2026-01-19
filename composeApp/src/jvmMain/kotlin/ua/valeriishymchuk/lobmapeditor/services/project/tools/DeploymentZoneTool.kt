package ua.valeriishymchuk.lobmapeditor.services.project.tools

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import ua.valeriishymchuk.lobmapeditor.domain.DeploymentZone
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.editor.HybridEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import ua.valeriishymchuk.lobmapeditor.ui.component.project.tool.ToolUiInfo

class DeploymentZoneTool : SpecificTool<GameScenario.Hybrid, HybridEditorService>() {

    override val uiInfo: ToolUiInfo = ToolUiInfo(
        AllIconsKeys.Nodes.Module8x8,
        "Deployment Zone Configurator",
        "Deployment Zone Configurator: allows you to edit the deployment zone on the map"
    )

    val selected = MutableStateFlow<Reference<Int, DeploymentZone>?>(null)
    val isHidden = MutableStateFlow(false)
    val canBeSelected = MutableStateFlow(false)


    override fun flush(editorService: HybridEditorService) { }


}