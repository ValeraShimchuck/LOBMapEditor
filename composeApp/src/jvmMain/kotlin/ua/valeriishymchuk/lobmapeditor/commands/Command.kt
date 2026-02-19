package ua.valeriishymchuk.lobmapeditor.commands

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService

interface Command<T> {

    fun execute(input: T): T

    fun undo(input: T): T

    fun applyCompound(editorService: EditorService<*>) {
        editorService.executeCompound(this)
    }

    companion object {
        fun Collection<Command<*>>.applyAllCompound(editorService: EditorService<*>) {
            forEach {
                editorService.executeCompound(it)
            }
        }
    }

    interface CommonData : Command<GameScenario.CommonData> {
        fun asPreset(): Preset {
            return WrapCommonToPresetCommand(this)
        }

        fun asHybrid(): Hybrid {
            return WrapCommonToHybridCommand(this)
        }
    }
    interface Preset : Command<GameScenario.Preset>
    interface Hybrid : Command<GameScenario.Hybrid>

}