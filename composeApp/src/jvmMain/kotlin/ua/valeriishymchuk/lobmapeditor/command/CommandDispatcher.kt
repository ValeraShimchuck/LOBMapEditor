package ua.valeriishymchuk.lobmapeditor.command

import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import kotlin.collections.ArrayDeque

class CommandDispatcher<T : GameScenario<T>>(
    scenario: T
) {

    var scenario: T = scenario
    private set

    private val scenarioSetter: (T) -> Unit = {
        this.scenario = it
    }

    private val commonDataSetter: (GameScenario.CommonData) -> Unit = {
        this.scenario = this.scenario.withCommonData(it)
    }

    private val commonDataGetter: () -> GameScenario.CommonData = {
        this.scenario.commonData
    }

    private val scenarioGetter: () -> T = {
        scenario
    }

    private val undoStack = ArrayDeque<CommandWrapper<*>>()
    private val redoStack = ArrayDeque<CommandWrapper<*>>()

    fun execute(command: Command<T>) {
        val wrapper = CommandWrapper(scenarioGetter, scenarioSetter, command)
        wrapper.execute()
        undoStack.addLast(wrapper)
        redoStack.clear()
    }

    fun executeCommon(command: Command<GameScenario.CommonData>) {
        val wrapper = CommandWrapper(commonDataGetter, commonDataSetter, command)
        wrapper.execute()
        undoStack.addLast(wrapper)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val cmd = undoStack.removeLast()
            cmd.undo()
            redoStack.addLast(cmd)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val cmd = redoStack.removeLast()
            cmd.execute()
            undoStack.addLast(cmd)
        }
    }

    private class CommandWrapper<T>(
        val valueGetter: () -> T,
        val valueSetter: (T) -> Unit,
        val command: Command<T>
    ) {
        fun undo() {
            valueSetter(command.undo(valueGetter()))
        }

        fun execute() {
            valueSetter(command.execute(valueGetter()))
        }
    }

}