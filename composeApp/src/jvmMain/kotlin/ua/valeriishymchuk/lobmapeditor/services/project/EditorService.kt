package ua.valeriishymchuk.lobmapeditor.services.project

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.ComposedCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Objective
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EditorService<T : GameScenario<T>>(
    override val di: DI,
): DIAware {

    private val lock = ReentrantLock()

    private var composedCommands: MutableList<CommandWrapper<*>>  = mutableListOf()

    lateinit var scenario: T
    val selectedUnits: MutableSet<Reference<Int, GameUnit>> = ConcurrentHashMap.newKeySet()
    var selectedObjectives: Reference<Int, Objective>? = null

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

    var selectionStart: Vector2f = Vector2f()
    var selectionEnd: Vector2f = Vector2f()
    var selectionEnabled: Boolean = false
    var width: Int = 0
    var height: Int = 0

    val projectionMatrix = Matrix4f()
    val viewMatrix = Matrix4f().identity()

    private val undoStack = ArrayDeque<CommandWrapper<*>>()
    private val redoStack = ArrayDeque<CommandWrapper<*>>()

    var cameraPosition: Vector2f
        get() = viewMatrix.getColumn(3, Vector4f()).let {
            Vector2f(it.x, it.y)
        }
        set(value) {
            val vector4 = Vector4f()
            vector4.w = 1.0f
            vector4.x = value.x
            vector4.y = value.y
            viewMatrix.setColumn(3, vector4)
        }

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

    fun fromScreenToNDC(
        cursorX: Int, cursorY: Int,
    ): Vector2f {

        val winX = (cursorX - width.toFloat() / 2) / (width / 2)
        val winY = (cursorY - height.toFloat() / 2) / (height / 2) * -1
        return Vector2f(winX, winY)
    }

    fun fromNDCToWorldSpace(
        ndc: Vector2f,
        viewMatrix: Matrix4f = this.viewMatrix,
        projectionMatrix: Matrix4f = this.projectionMatrix,
    ): Vector2f {
        val invertProj = projectionMatrix.invert(Matrix4f())
        val invertView = viewMatrix.invert(Matrix4f())
        val invProfView = invertView.mul(invertProj, Matrix4f())
        val cords = Vector4f(ndc, 0f, 1f)
        cords.mul(invProfView)
        return Vector2f(cords.x, cords.y)
    }

    fun fromScreenToWorldSpace(
        cursorX: Int,
        cursorY: Int,
        viewMatrix: Matrix4f = this.viewMatrix,
        projectionMatrix: Matrix4f = this.projectionMatrix,
    ): Vector2f {

        return fromNDCToWorldSpace(
            fromScreenToNDC(cursorX, cursorY),
            viewMatrix,
            projectionMatrix
        )
    }

    fun getTileCordsFromScreenClamp(cursorX: Int, cursorY: Int): Vector2i {
        val worldCoordinates = fromScreenToWorldSpace(cursorX, cursorY)
        val map = scenario.map

        // Clamp world coordinates to map boundaries
        val clampedX = worldCoordinates.x.coerceIn(0f, (map.widthPixels - 1).toFloat())
        val clampedY = worldCoordinates.y.coerceIn(0f, (map.heightPixels - 1).toFloat())

        // Convert to tile coordinates
        val tileX = (clampedX / GameConstants.TILE_SIZE).toInt()
        val tileY = (clampedY / GameConstants.TILE_SIZE).toInt()

        return Vector2i(tileX, tileY)
    }

}