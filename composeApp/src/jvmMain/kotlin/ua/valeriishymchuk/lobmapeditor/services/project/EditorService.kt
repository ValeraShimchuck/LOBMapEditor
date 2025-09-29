package ua.valeriishymchuk.lobmapeditor.services.project

import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.annotations.ApiStatus
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.ComposedCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Objective
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EditorService<T : GameScenario<T>>(
    override val di: DI,
): DIAware {

    private val lock = ReentrantLock()

    private var composedCommands: MutableList<CommandWrapper<*>>  = mutableListOf()

    var scenario: MutableStateFlow<T?> = MutableStateFlow(null)
    val selectedUnits: MutableStateFlow<Set<Reference<Int, GameUnit>>> = MutableStateFlow(setOf())
    var selectedObjectives: MutableStateFlow<Reference<Int, Objective>?>  = MutableStateFlow(null)

    private val scenarioSetter: (T) -> Unit = {
        this.scenario.value = it
    }

    private val commonDataSetter: (GameScenario.CommonData) -> Unit = {
        this.scenario.value = this.scenario.value!!.withCommonData(it)
    }

    private val commonDataGetter: () -> GameScenario.CommonData = {
        this.scenario.value!!.commonData
    }

    private val scenarioGetter: () -> T = {
        scenario.value!!
    }

    var selectionStart: Vector2f = Vector2f()
    var selectionEnd: Vector2f = Vector2f()
    var selectionEnabled: Boolean = false
    var width: Int = 0
    var height: Int = 0
    // hoi4 mode
    var enableColorClosestPoint = false

    val projectionMatrix = Matrix4f()
    val viewMatrix = Matrix4f().identity()

    private val undoStack = ArrayDeque<CommandWrapper<*>>()
    private val redoStack = ArrayDeque<CommandWrapper<*>>()

    var cameraPosition: Vector2f get() {
        val centerX = width / 2
        val centerY = height / 2
        return fromScreenToWorldSpace(centerX, centerY)
    }
        set(value) {

            val centerX = width / 2
            val centerY = height / 2
            val currentCenter = fromScreenToWorldSpace(centerX, centerY)
            val worldDiff = value.sub(currentCenter, Vector2f())

            // Compute the transformed difference using the view matrix's linear part
            val dir = Vector3f(worldDiff.x, worldDiff.y, 0f)
            viewMatrix.transformDirection(dir)
            val transDiff = Vector2f(-dir.x, -dir.y)
            rawCameraPosition = rawCameraPosition.add(transDiff)
        }

    var rawCameraPosition: Vector2f
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
        lock {
            composedCommands.add(wrapper)
            wrapper.execute()
        }
    }

    fun executeCompoundCommon(command: Command<GameScenario.CommonData>) {
        val wrapper = CommandWrapper(commonDataGetter, commonDataSetter, command)
        lock {
            composedCommands.add(wrapper)
            wrapper.execute()
        }
    }

    private fun lock(handler: () -> Unit) {
        lock.withLock {
            handler()
        }
    }

    fun flushCompound() {
        lock {
            if (composedCommands.isEmpty()) return@lock
            val command = ComposedCommand(composedCommands.map { it.command })
            composedCommands.clear()
            undoStack.addLast(CommandWrapper(scenarioGetter, scenarioSetter, command as Command<T>))
            redoStack.clear()
        }
    }

    fun flushCompoundCommon() {
        lock {
            if (composedCommands.isEmpty()) return@lock
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

    @ApiStatus.Experimental
    fun fromWorldSpaceToNDC(x: Float, y: Float): Vector2f {
        val profView = viewMatrix.mul(projectionMatrix, Matrix4f())
        val cords = Vector4f(x, y, 0f, 1f)
        cords.mul(profView)
        return Vector2f(cords.x, cords.y)
    }

    @ApiStatus.Experimental
    fun fromNDCToScreen(ndc: Vector2f): Vector2i {
        val x = (ndc.x + 1f) / 2f
        val y = (ndc.y + 1f) / -2f
        return Vector2i(x.toInt(), y.toInt())
    }

    // wasn't used anywhere so far, so it might break
    @ApiStatus.Experimental
    fun fromWorldSpaceToScreen(
        x: Float,
        y: Float
    ): Vector2i {
        return fromNDCToScreen(fromWorldSpaceToNDC(x, y))
    }



    fun getTileCordsFromScreenClamp(cursorX: Int, cursorY: Int): Vector2i {
        val worldCoordinates = fromScreenToWorldSpace(cursorX, cursorY)
        val map = scenario.value!!.map

        // Clamp world coordinates to map boundaries
        val clampedX = worldCoordinates.x.coerceIn(0f, (map.widthPixels - 1).toFloat())
        val clampedY = worldCoordinates.y.coerceIn(0f, (map.heightPixels - 1).toFloat())

        // Convert to tile coordinates
        val tileX = (clampedX / GameConstants.TILE_SIZE).toInt()
        val tileY = (clampedY / GameConstants.TILE_SIZE).toInt()

        return Vector2i(tileX, tileY)
    }

    companion object {
        fun EditorService<GameScenario.Preset>.deleteUnits(map: Set<Reference<Int, GameUnit>>) {
            selectedUnits.value -= map
            val oldSelectedUnits = selectedUnits.value.map { it.getValue(scenario.value!!.units::get) }
            val oldList = scenario.value!!.units
            val newList = oldList.filterIndexed { index, unit -> !map.contains(Reference(index)) }
            execute(UpdateGameUnitListCommand(oldList, newList))
            val newSelectedList = scenario.value!!.units.mapIndexedNotNull { index, unit ->
                if (oldSelectedUnits.contains(unit)) return@mapIndexedNotNull Reference<Int, GameUnit>(index)
                null
            }
            selectedUnits.value = newSelectedList.toSet()
        }
    }



}