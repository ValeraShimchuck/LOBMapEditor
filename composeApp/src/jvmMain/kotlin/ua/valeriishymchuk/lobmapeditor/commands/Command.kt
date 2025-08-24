package ua.valeriishymchuk.lobmapeditor.commands

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario

interface Command<T> {

    fun execute(input: T): T

    fun undo(input: T): T

    interface CommonData : Command<GameScenario.CommonData>
    interface Preset : Command<GameScenario.Preset>
    interface Hybrid : Command<GameScenario.Hybrid>

}