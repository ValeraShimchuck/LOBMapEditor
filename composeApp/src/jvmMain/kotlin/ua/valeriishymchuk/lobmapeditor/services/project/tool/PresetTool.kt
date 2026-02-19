package ua.valeriishymchuk.lobmapeditor.services.project.tool

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService

abstract class PresetTool : SpecificTool<GameScenario.Preset, PresetEditorService>()