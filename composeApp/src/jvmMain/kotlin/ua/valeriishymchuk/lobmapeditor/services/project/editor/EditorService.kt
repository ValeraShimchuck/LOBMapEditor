package ua.valeriishymchuk.lobmapeditor.services.project.editor

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.joml.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.commands.Command
import ua.valeriishymchuk.lobmapeditor.commands.Command.Companion.applyAllCompound
import ua.valeriishymchuk.lobmapeditor.commands.ComposedCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.reference.TriggerScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.trigger.GameAction
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.LifecycleService
import ua.valeriishymchuk.lobmapeditor.services.ScenarioIOService
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.emptyList
import kotlin.concurrent.withLock

sealed class EditorService<T : GameScenario<T>>(
    override val di: DI,
) : DIAware {

    val throwTestError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val scenarioIOService by di.instance<ScenarioIOService>()
    protected val projectRef by di.instance<ProjectRef>()
    protected val lifecycleService by di.instance<LifecycleService>()

    protected val lock = ReentrantLock()

    protected var composedCommands: MutableList<CommandWrapper<*>> = mutableListOf()

    val rerenderTrigger = MutableStateFlow(true)

    var openglUpdateState = MutableStateFlow(0)

    var scenario: MutableStateFlow<T?> = MutableStateFlow(null)

    var selectedObjects: MutableStateFlow<Set<ScenarioReference>> = MutableStateFlow(emptySet())

    open fun getAllObjects(): Set<ScenarioReference> {
        // TODO add other objects, such as objectives and objectives/units from triggers
        val set: MutableSet<ScenarioReference> = scenario.value!!.objectives.indices.map {
            Objective.ScenarioObjectiveReference(it)
        }.toMutableSet()

        set.addAll(findAllTriggerUnits())
        return set
    }

    private fun findAllTriggerUnits(): Set<ScenarioReference> {
        val set: MutableSet<ScenarioReference> = mutableSetOf()

        scenario.value!!.triggers.forEachIndexed { triggerId, trigger ->
            trigger.actions.forEachIndexed { actionId, action ->
                if (action is GameAction.AddUnit) {
                    set.addAll(action.gameUnits.mapIndexed { unitId, _ ->
                        GameUnit.TriggerUnitReference(
                            TriggerScenarioReference.ObjectAddress(
                                triggerId,
                                actionId,
                                listOf(unitId)
                            )
                        )
                    })
                    return@forEachIndexed
                }
                if (action !is GameAction.AddTrigger) return@forEachIndexed
                fun traverseDeep(action: GameAction.AddTrigger, subAddress: List<Int>) {
                    action.triggers.forEachIndexed { deepTriggerId, deepTrigger ->
                        deepTrigger.actions.forEachIndexed { deepActionId, deepAction ->
                            val currentSubAddress = subAddress.toMutableList()
                            currentSubAddress.add(deepTriggerId)
                            currentSubAddress.add(deepActionId)
                            if (deepAction is GameAction.AddUnit) {
                                deepAction.gameUnits.forEachIndexed { deepUnitId, _ ->
                                    val unitAddress = currentSubAddress.toMutableList()
                                    unitAddress.add(deepUnitId)
                                    set.add(GameUnit.TriggerUnitReference(
                                        TriggerScenarioReference.ObjectAddress(
                                            triggerId, actionId, unitAddress
                                        )
                                    ))
                                }
                                return@forEachIndexed
                            }
                            if (deepAction !is GameAction.AddTrigger) return@forEachIndexed
                            traverseDeep(deepAction, currentSubAddress)
                        }
                    }
                }
                traverseDeep(action, emptyList())
            }
        }
        return set
    }

    var lastSave: Long = 0
        protected set
    var lastHashCode: Int = 0
        protected set
    var lastAction: Long = 0
        protected set
    protected var savingJob: Job? = null

    protected val scenarioSetter: (T) -> Unit = { newScenario ->
        val newSelectedObjects = selectedObjects.value.filter { it.isValid(newScenario) }
        if (newSelectedObjects.size != selectedObjects.value.size) selectedObjects.value = newSelectedObjects.toSet()
        this.scenario.value = newScenario
    }


    protected val scenarioGetter: () -> T = {
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
    val viewMatrix = Matrix4f().identity()!!

    protected val undoStack = ArrayDeque<CommandWrapper<*>>()
    protected val redoStack = ArrayDeque<CommandWrapper<*>>()

    var cameraPosition: Vector2f
        get() {
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

    protected fun checkComposedCommandsIntegrity(typeChecker: (Command<*>) -> Boolean) {
        if (composedCommands.isEmpty()) return
        if (!composedCommands.all { typeChecker(it.command) })
            throw IllegalStateException("The composed commands list doesn't have integrity: $composedCommands")
    }


    fun updateCommonData(updater: (GameScenario.CommonData) -> GameScenario.CommonData) {
        scenario.value = scenario.value?.let {
            it.withCommonData(updater(it.commonData))
        }
    }

    init {
        lifecycleService.onClose = {
            savingJob = null
            println("trying to force save")
            save(true, blocking = true)
        }
    }

    abstract fun importScenario(scenario: T)

    protected val saveCount = AtomicInteger()

    fun save(forceSave: Boolean = false, blocking: Boolean = false) {
        val scenario = scenario.value ?: return
        if (savingJob?.isActive == true) return
        if (!forceSave) {
            if (System.currentTimeMillis() - lastSave < 30000 && System.currentTimeMillis() - lastAction < 5000) return
        }
        if (scenario.hashCode() == lastHashCode) return

        suspend fun save0() {
            lastSave = System.currentTimeMillis()
            lastHashCode = scenario.hashCode()
            val backupId = saveCount.incrementAndGet() % 10
            val backupFile = projectRef.getBackupFile(backupId)
            projectRef.mapFile.copyTo(backupFile, overwrite = true)
            scenarioIOService.save(scenario, projectRef.mapFile)
            println("Saved map")
        }

        if (blocking) {
            runBlocking {
                save0()
            }
        } else {
            savingJob = CoroutineScope(Dispatchers.IO).launch {
                save0()
            }
        }


    }


    fun deleteObjects(references: Set<ScenarioReference>) {
        selectedObjects.value = emptySet()
        ScenarioReference.deleteList(references, scenario.value!!).applyAllCompound(this)
        flushCompound()
    }

    fun executeCompound(command: Command<*>) {
        executeCompoundRaw(convertCommand(command))
    }

    protected fun executeCompoundRaw(command: Command<T>) {
        lastAction = System.currentTimeMillis()
        val wrapper = CommandWrapper(scenarioGetter, scenarioSetter, command)
        lock {
            composedCommands.add(wrapper)
            wrapper.execute()
        }
    }

    protected fun lock(handler: () -> Unit) {
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

    abstract fun castCommandOrFail(command: Command<*>): Command<T>

    abstract fun convertCommonCommand(command: Command.CommonData): Command<T>

    fun execute(command: Command<*>) {
        executeRaw(convertCommand(command))
    }

    protected fun convertCommand(command: Command<*>): Command<T> {
        return if (command is Command.CommonData) {
            convertCommonCommand(command)
        } else {
            castCommandOrFail(command)
        }
    }

    protected fun executeRaw(command: Command<T>) {
        val wrapper = CommandWrapper(scenarioGetter, scenarioSetter, command)
        lastAction = System.currentTimeMillis()
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

    protected class CommandWrapper<T>(
        val valueGetter: () -> T,
        val valueSetter: (T) -> Unit,
        val command: Command<T>
    ) {

        val creationStack = StackWalker.getInstance()
            .walk { it.map { frame -> frame.toStackTraceElement().toString() }.toList() }.joinToString("\n")

        fun undo() {

            try {
                valueSetter(command.undo(valueGetter()))
            } catch (e: Throwable) {
                println("Undoing $command the input ${valueGetter()?.javaClass?.simpleName}")
                e.printStackTrace()
                println("Was created at $creationStack")
            }

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
        val map = scenario.value!!.map

        // Clamp world coordinates to map boundaries
        val clampedX = worldCoordinates.x.coerceIn(0f, (map.widthPixels - 1).toFloat())
        val clampedY = worldCoordinates.y.coerceIn(0f, (map.heightPixels - 1).toFloat())

        // Convert to tile coordinates
        val tileX = (clampedX / GameConstants.TILE_SIZE).toInt()
        val tileY = (clampedY / GameConstants.TILE_SIZE).toInt()

        return Vector2i(tileX, tileY)
    }

}