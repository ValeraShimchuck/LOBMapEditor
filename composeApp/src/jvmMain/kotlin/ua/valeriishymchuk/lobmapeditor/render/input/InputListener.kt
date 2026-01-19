package ua.valeriishymchuk.lobmapeditor.render.input

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tools.TerrainPickTool
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import kotlin.math.abs
import kotlin.math.max

abstract class InputListener<S: GameScenario<S>>(
    override val di: DI
) : MouseAdapter(), MouseMotionListener, KeyListener, DIAware {

    protected var lastMouseX = 0
    protected var lastMouseY = 0

    protected var lastX = 0
    protected var lastY = 0
    protected var isDragging = false

    protected var leftLastX: Int? = null
    protected var leftLastY: Int? = null
    protected var isToolDragging = false

    protected var isShiftPressed = false
    protected var isCtrlPressed = false
    protected var isSelectionDragging: Boolean = false
    protected var shouldDragSelectedObjects: Boolean = false
    protected var lastDragPosition: Vector2f = Vector2f()


    protected open val editorService: EditorService<*> by di.instance()
    protected val toolService: ToolService<*> by di.instance()

    fun getPointsBetween(start: Vector2i, end: Vector2i): List<Vector2i> {
        val points = mutableListOf<Vector2i>()

        val x0 = start.x
        val y0 = start.y
        val x1 = end.x
        val y1 = end.y

        val dx = abs(x1 - x0)
        val dy = -abs(y1 - y0)

        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1

        var error = dx + dy
        var currentX = x0
        var currentY = y0

        while (true) {
            points.add(Vector2i(currentX, currentY))
            if (currentX == x1 && currentY == y1) break
            val e2 = 2 * error
            if (e2 >= dy) {
                if (currentX == x1) break
                error += dy
                currentX += sx
            }
            if (e2 <= dx) {
                if (currentY == y1) break
                error += dx
                currentY += sy
            }
        }

        return points
    }

    override fun keyTyped(e: KeyEvent) {

    }

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_SHIFT -> isShiftPressed = true
            KeyEvent.VK_CONTROL -> isCtrlPressed = true
        }
    }

    open fun onDelete() {

    }

    open fun onDuplicate() {

    }

    open fun onArrowDrag(e: MouseEvent) {
    }

    override fun keyReleased(e: KeyEvent) {
        when (e.keyCode) {
//                KeyEvent.VK_F -> setTerrainHeight = !setTerrainHeight
            KeyEvent.VK_Z -> {
                if (!isCtrlPressed) return
                if (isShiftPressed) {
                    editorService.redo()
                } else editorService.undo()
            }

            KeyEvent.VK_SHIFT -> isShiftPressed = false
            KeyEvent.VK_CONTROL -> isCtrlPressed = false

            KeyEvent.VK_DELETE -> {
                onDelete()
                editorService.selectedObjectives.value?.let { reference ->
                    editorService.deleteObjectives(
                        setOf(reference)
                    )
                }

            }

            KeyEvent.VK_D -> {
                handleDuplication()
            }

            KeyEvent.VK_E -> {
//                runTestError()
            }

            KeyEvent.VK_R -> {
                if (isCtrlPressed) {
                    toolService.refenceOverlayTool.enabled.value = !toolService.refenceOverlayTool.enabled.value
                }
            }


            KeyEvent.VK_G -> {
                if (isCtrlPressed) {
                    toolService.gridTool.enabled.value = !toolService.gridTool.enabled.value
                }
            }


            KeyEvent.VK_Q -> {
                if (isCtrlPressed) {
                    toolService.currentTool.value = TerrainPickTool
                }
            }

        }
    }

    protected fun runTestError() {
        if (!isCtrlPressed) return
        println("Throwing error")
        editorService.throwTestError.value = true
//        throw IllegalStateException("Test exception")
    }

    protected fun handleDuplication() {
        if (!isCtrlPressed) return
        onDuplicate()
        val selectedObjectivesToCopy = editorService.selectedObjectives.value
            ?.getValue(editorService.scenario.value!!.objectives::get) ?: return

        val center = Vector2f(selectedObjectivesToCopy.position.x, selectedObjectivesToCopy.position.y)
        val mousePos = editorService.fromScreenToWorldSpace(lastMouseX, lastMouseY)
        val difference = mousePos.sub(center, Vector2f())
        val oldList = editorService.scenario.value!!.objectives
        val newList = oldList.toMutableList()
        newList.add(selectedObjectivesToCopy.copy(position = Position(
            selectedObjectivesToCopy.position.x + difference.x,
            selectedObjectivesToCopy.position.y + difference.y
        )
        ))
        editorService.executeCommon(
            UpdateObjectiveListCommand(
                oldList,
                newList
            )
        )
    }


    override fun mouseDragged(e: MouseEvent) {
        checkMiddleMouse(e)
        checkTilePainting(e)
        checkSelectionDrag(e)
        checkSelectedObjectsDrag(e)
        checkArrowDrag(e)
//        println("Current thread: ${Thread.currentThread().name}")

    }

    protected fun checkArrowDrag(e: MouseEvent) {
        onArrowDrag(e)
    }

    override fun mouseMoved(e: MouseEvent) {
        lastMouseX = e.x
        lastMouseY = e.y
    }

    protected fun checkTilePainting(e: MouseEvent): Boolean {
        if (!isToolDragging) return false
        if (leftLastX == null || leftLastY == null) {
            leftLastX = e.x
            leftLastY = e.y
            return false
        }
        val oldCords = editorService.getTileCordsFromScreenClamp(
            leftLastX ?: return false,
            leftLastY ?: return false
        )
            .mul(GameConstants.TILE_SIZE)
        leftLastX = e.x
        leftLastY = e.y
        val newCords = editorService.getTileCordsFromScreenClamp(
            leftLastX ?: return false,
            leftLastY ?: return false
        )
            .mul(GameConstants.TILE_SIZE)


        val shouldRender = toolService.useToolManyTimes(
            getPointsBetween(oldCords, newCords)
                .distinct()
                .map {
                    Vector2f(it)
                }, false
        )
        return shouldRender
    }


    protected fun checkMiddleMouse(e: MouseEvent): Boolean {
        if (!isDragging) return false
        val dx = e.x - lastX
        val dy = e.y - lastY
        lastX = e.x
        lastY = e.y

        if (dx == 0 && dy == 0) return false

        editorService.rawCameraPosition = editorService.rawCameraPosition.add(dx.toFloat(), dy.toFloat())
        return true

    }

    protected fun checkSelectionDrag(e: MouseEvent): Boolean {
        if (!isSelectionDragging) return false
        editorService.selectionEnd = editorService.fromScreenToNDC(e.x, e.y)
        if (!editorService.selectionEnabled && editorService.selectionStart.distance(editorService.selectionEnd) > 0.05f)
            editorService.selectionEnabled = true
        return editorService.selectionEnabled
    }

    open fun onSelectionDrag(change: Vector2f) {

    }

    protected fun checkSelectedObjectsDrag(e: MouseEvent): Boolean {
        if (!shouldDragSelectedObjects) return false
        val oldPos = Vector2f(lastDragPosition)
        val newPos = editorService.fromScreenToWorldSpace(e.x, e.y)
        lastDragPosition = newPos
        val change = newPos.sub(oldPos, Vector2f())

        onSelectionDrag(change)

        val selectedObjective = editorService.selectedObjectives.value
        if (selectedObjective != null) {
            val objectiveList = editorService.scenario.value!!.objectives.toMutableList()
            val oldObjective = selectedObjective.getValue(editorService.scenario.value!!.objectives::get)
            val newObjectivePos = Vector2f(
                oldObjective.position.x.coerceIn(0f, editorService.scenario.value!!.map.widthPixels.toFloat()),
                oldObjective.position.y.coerceIn(0f, editorService.scenario.value!!.map.heightPixels.toFloat())
            ).add(change)
            val newObjective = oldObjective.copy(
                position = Position(newObjectivePos.x, newObjectivePos.y)
            )
            objectiveList[selectedObjective.key] = newObjective
            editorService.executeCompoundCommon(
                UpdateObjectiveListCommand(
                    editorService.scenario.value!!.objectives,
                    objectiveList
                )
            )
        }

        return true
    }

    override fun mousePressed(e: MouseEvent) {
        checkMiddlePressed(e)
        checkToolUsage(e)
        checkStartOfSelection(e)
    }

    override fun mouseReleased(e: MouseEvent) {
        checkMiddleReleased(e)
        checkEndOfSelection(e)
        checkEndOfToolUsage(e)
    }

    open fun onSelectionClear() {

    }
    open fun onStartOfSelection(e: MouseEvent): Boolean {
        return false
    }

    protected fun checkStartOfSelection(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON1) return
        if (toolService.refenceOverlayTool.hideSprites.value) {
            editorService.selectionStart = editorService.fromScreenToNDC(e.x, e.y)
            editorService.selectionEnd = editorService.fromScreenToNDC(e.x, e.y)
            isSelectionDragging = true
            return
        }

        val objective = getClickedObjective(e)
        val shiftOrControl = isShiftPressed || isCtrlPressed
        if (objective != null && !shiftOrControl) {
            onSelectionClear()
            editorService.selectedObjectives.value =
                Reference(editorService.scenario.value!!.objectives.indexOf(objective))
            lastDragPosition = editorService.fromScreenToWorldSpace(e.x, e.y)
            shouldDragSelectedObjects = true
            return
        }

        if (onStartOfSelection(e)) return

        editorService.selectionStart = editorService.fromScreenToNDC(e.x, e.y)
        editorService.selectionEnd = editorService.fromScreenToNDC(e.x, e.y)
        isSelectionDragging = true
    }

    protected fun getClickedObjective(e: MouseEvent): Objective? {
        val clickedPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        val objectiveDimensions = Vector2f(
            GameConstants.TILE_SIZE.toFloat()
        ).mul(1.3f)
        val objectiveDimensionMin = objectiveDimensions.div(-2f, Vector2f())
        val objectiveDimensionMax = objectiveDimensions.div(2f, Vector2f())
        // checking selection for objectives
        val objectiveScale = max((2.5f / editorService.viewMatrix.getScale(Vector3f()).x), 1f)

        return editorService.scenario.value!!.objectives.firstOrNull { objective ->
            val positionMatrix = Matrix4f()
            positionMatrix.setTranslation(Vector3f(objective.position.x, objective.position.y, 0f))
            positionMatrix.scale(objectiveScale)
            val inversePositionMatrix = positionMatrix.invert(Matrix4f())
            val localPoint4f = Vector4f(clickedPoint, 0f, 1f)
                .mul(inversePositionMatrix, Vector4f())
            val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
            objectiveDimensionMin.x < localPoint.x && localPoint.x < objectiveDimensionMax.x &&
                    objectiveDimensionMin.y < localPoint.y && localPoint.y < objectiveDimensionMax.y
        }
    }

    open fun onSingleSelection(e: MouseEvent) {

    }

    protected fun checkSingleSelection(e: MouseEvent) {
        if (toolService.refenceOverlayTool.hideSprites.value) return
        val objective = getClickedObjective(e)
        if (objective != null) {
            onSelectionClear()
            editorService.selectedObjectives.value =
                Reference(editorService.scenario.value!!.objectives.indexOf(objective))
            return
        }
        editorService.selectedObjectives.value = null
        onSingleSelection(e)
    }

    open fun onSelectionEndBegin(): Boolean {
        return false
    }

    open fun onSelectionEnd() {

    }

    protected fun checkEndOfSelection(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON1) return
        if (toolService.refenceOverlayTool.hideSprites.value) {
            isSelectionDragging = false
            editorService.selectionEnabled = false
            return
        }

        if (onSelectionEndBegin()) return


        isSelectionDragging = false

        if (shouldDragSelectedObjects) {
            shouldDragSelectedObjects = false
            if (editorService.selectedObjectives.value == null)
                editorService.flushCompound()
            else editorService.flushCompoundCommon()
            return
        }
        if (!editorService.selectionEnabled) {
            checkSingleSelection(e)
            return
        }
        editorService.selectionEnabled = false
        onSelectionEnd()
    }

    protected fun checkMiddlePressed(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON2) return
        lastX = e.x
        lastY = e.y
        isDragging = true
    }

    protected fun checkMiddleReleased(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON2) return
        leftLastX = e.x
        leftLastX = e.y
        isDragging = false
    }

    protected fun checkToolUsage(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON3) return
        isToolDragging = true
        val worldCoordinates = editorService.fromScreenToWorldSpace(e.x, e.y)
        if (worldCoordinates.x < 0 || worldCoordinates.y < 0) return
        if (worldCoordinates.x > editorService.scenario.value!!.map.widthPixels || worldCoordinates.y > editorService.scenario.value!!.map.heightPixels) return
        toolService.useTool(worldCoordinates.x, worldCoordinates.y)
    }

    protected fun checkEndOfToolUsage(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON3) return
        isToolDragging = false
        toolService.flushCompoundCommands()
        leftLastX = null
        leftLastY = null
    }


    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val zoomIntensity = 0.1
        val zoomFactor = 1.0 + zoomIntensity * -e.wheelRotation // Invert scroll direction

        // Get world position of mouse before scaling
        val oldWorldPos = editorService.fromScreenToWorldSpace(e.x, e.y)

        // Create a copy of the current view matrix and apply scaling
        val newView = Matrix4f(editorService.viewMatrix)
        newView.scaleLocal(zoomFactor.toFloat(), zoomFactor.toFloat(), 1f)

        // Get world position of mouse after scaling (without translation adjustment)
        val newWorldPos = editorService.fromScreenToWorldSpace(e.x, e.y, newView)

        // Calculate required translation adjustment in world space
        val delta = Vector2f(oldWorldPos).sub(newWorldPos)

        // Extract scale factors from scaled view matrix
        val scaleX = newView.m00()
        val scaleY = newView.m11()

        if (scaleX !in 0.03..14.0) return

        // Apply translation adjustment (inverse because view matrix is inverse of camera)
        val translation = newView.getColumn(3, Vector4f())
        translation.x -= delta.x * scaleX
        translation.y -= delta.y * scaleY
        newView.setColumn(3, translation)

        // Update main view matrix and request redraw
        editorService.viewMatrix.set(newView)
    }


}