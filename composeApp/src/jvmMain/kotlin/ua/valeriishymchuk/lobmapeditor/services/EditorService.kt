package ua.valeriishymchuk.lobmapeditor.services

import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.ComposedCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EditorService<T : GameScenario<T>>(
    scenario: T
) {

    private val lock = ReentrantLock()

    private var composedCommands: MutableList<CommandWrapper<*>>  = mutableListOf()

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

    fun executeCompound(command: Command<T>) {
        val wrapper = CommandWrapper(scenarioGetter, scenarioSetter, command)
        lock.withLock {
            composedCommands.add(wrapper)
            wrapper.execute()
        }
    }

    fun executeCompoundCommon(command: Command<GameScenario.CommonData>) {
        val wrapper = CommandWrapper(commonDataGetter, commonDataSetter, command)
        lock.withLock {
            composedCommands.add(wrapper)
            wrapper.execute()
        }
    }

    fun flushCompound() {
        lock.withLock {
            if (composedCommands.isEmpty()) return@withLock
            val command = ComposedCommand(composedCommands.map { it.command })
            composedCommands.clear()
            undoStack.addLast(CommandWrapper(scenarioGetter, scenarioSetter, command as Command<T>))
            redoStack.clear()
        }
    }

    fun flushCompoundCommon() {
        lock.withLock {
            if (composedCommands.isEmpty()) return@withLock
            val command = ComposedCommand(composedCommands.map { it.command })
            composedCommands.clear()
            undoStack.addLast(CommandWrapper(commonDataGetter, commonDataSetter, command as Command<GameScenario.CommonData>))
            redoStack.clear()
        }
    }

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