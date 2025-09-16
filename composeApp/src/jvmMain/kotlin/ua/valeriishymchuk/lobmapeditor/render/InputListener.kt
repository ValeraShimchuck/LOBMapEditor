package ua.valeriishymchuk.lobmapeditor.render

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Objective
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit.Companion.UNIT_DIMENSIONS
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.ToolService
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import kotlin.getValue
import kotlin.math.abs
import kotlin.math.max
import kotlin.ranges.contains

class InputListener(
    private val rerender: () -> Unit,
    override val di: DI
) : MouseAdapter(), MouseMotionListener, KeyListener, DIAware {

    private var lastX = 0
    private var lastY = 0
    private var isDragging = false

    private var leftLastX: Int? = null
    private var leftLastY: Int? = null
    private var isToolDragging = false

    private var isShiftPressed = false
    private var isCtrlPressed = false
    private var isSelectionDragging: Boolean = false
    private var shouldDragSelectedObjects: Boolean = false
    private var lastDragPosition: Vector2f = Vector2f()

    private val editorService: EditorService<GameScenario.Preset> by di.instance()
    private val toolService: ToolService by di.instance()

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

    override fun keyTyped(e: KeyEvent) {}

    override fun keyPressed(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_SHIFT -> isShiftPressed = true
            KeyEvent.VK_CONTROL -> isCtrlPressed = true
        }
    }

    override fun keyReleased(e: KeyEvent) {
        when (e.keyCode) {
//                KeyEvent.VK_F -> setTerrainHeight = !setTerrainHeight
            KeyEvent.VK_Z -> {
                if (!isCtrlPressed) return
                if (isShiftPressed) {
                    editorService.redo()
                } else editorService.undo()
                rerender()
            }

            KeyEvent.VK_SHIFT -> isShiftPressed = false
            KeyEvent.VK_CONTROL -> isCtrlPressed = false
        }
    }

    override fun mouseDragged(e: MouseEvent) {

        val shouldRender = listOf(
            checkMiddleMouse(e),
            checkTilePainting(e),
            checkSelectionDrag(e),
            checkSelectedObjectsDrag(e)
        ).any { it }
        if (shouldRender) rerender()
    }

    override fun mouseMoved(e: MouseEvent) {}

    private fun checkTilePainting(e: MouseEvent): Boolean {
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


    private fun checkMiddleMouse(e: MouseEvent): Boolean {
        if (!isDragging) return false
        val dx = e.x - lastX
        val dy = e.y - lastY
        lastX = e.x
        lastY = e.y

        if (dx == 0 && dy == 0) return false

        editorService.cameraPosition = editorService.cameraPosition.add(dx.toFloat(), dy.toFloat())
        return true

    }

    private fun checkSelectionDrag(e: MouseEvent): Boolean {
        if (!isSelectionDragging) return false
        editorService.selectionEnd = editorService.fromScreenToNDC(e.x, e.y)
        if (!editorService.selectionEnabled && editorService.selectionStart.distance(editorService.selectionEnd) > 0.05f)
            editorService.selectionEnabled = true
        return editorService.selectionEnabled
    }

    private fun checkSelectedObjectsDrag(e: MouseEvent): Boolean {
        if (!shouldDragSelectedObjects) return false
        val oldPos = Vector2f(lastDragPosition)
        val newPos = editorService.fromScreenToWorldSpace(e.x, e.y)
        lastDragPosition = newPos
        val change = newPos.sub(oldPos, Vector2f())
        if (editorService.selectedUnits.value.isNotEmpty()) {
            val unitList = editorService.scenario.value!!.units.toMutableList()
            editorService.selectedUnits.value.forEach { reference ->
                val oldUnit = reference.getValue(editorService.scenario.value!!.units::get)
                val newUnitPos = Vector2f(oldUnit.position.x, oldUnit.position.y).add(change)
                val newUnit = oldUnit.copy(
                    position = Position(
                        newUnitPos.x.coerceIn(0f,editorService.scenario.value!!.map.widthPixels.toFloat()),
                        newUnitPos.y.coerceIn(0f,editorService.scenario.value!!.map.heightPixels.toFloat())
                    )
                )
                unitList[reference.key] = newUnit
            }
            editorService.executeCompound(UpdateGameUnitListCommand(
                editorService.scenario.value!!.units,
                unitList
            ))
        }

        val selectedObjective = editorService.selectedObjectives.value
        if (selectedObjective != null) {
            val objectiveList = editorService.scenario.value!!.objectives.toMutableList()
            val oldObjective = selectedObjective.getValue(editorService.scenario.value!!.objectives::get)
            val newObjectivePos = Vector2f(
                oldObjective.position.x.coerceIn(0f,editorService.scenario.value!!.map.widthPixels.toFloat()),
                oldObjective.position.y.coerceIn(0f,editorService.scenario.value!!.map.heightPixels.toFloat())
            ).add(change)
            val newObjective = oldObjective.copy(
                position = Position(newObjectivePos.x, newObjectivePos.y)
            )
            objectiveList[selectedObjective.key] = newObjective
            editorService.executeCompoundCommon(UpdateObjectiveListCommand(
                editorService.scenario.value!!.objectives,
                objectiveList
            ))
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

    private fun checkStartOfSelection(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON1) return
        val objective = getClickedObjective(e)
        val shiftOrControl = isShiftPressed || isCtrlPressed
        if (objective != null && !shiftOrControl) {

            editorService.selectedUnits.value = setOf()
            editorService.selectedObjectives.value = Reference(editorService.scenario.value!!.objectives.indexOf(objective))
            lastDragPosition = editorService.fromScreenToWorldSpace(e.x, e.y)
            shouldDragSelectedObjects = true
            rerender()
            return
        }

        val units = getClickedUnits(e)
        if (units.isNotEmpty() && !shiftOrControl) {
            editorService.selectedObjectives.value = null
            shouldDragSelectedObjects = true
            lastDragPosition = editorService.fromScreenToWorldSpace(e.x, e.y)
            val firstSelected = units.firstOrNull { unit ->
                val reference = Reference<Int, GameUnit>(editorService.scenario.value!!.units.indexOf(unit))
                editorService.selectedUnits.value.contains(reference)
            }
            if (firstSelected == null) {
                editorService.selectedUnits.value = setOf()
                editorService.selectedUnits.value += Reference(editorService.scenario.value!!.units.indexOf(units.first()))
            }
            rerender()
            return
        }


        editorService.selectionStart = editorService.fromScreenToNDC(e.x, e.y)
        editorService.selectionEnd = editorService.fromScreenToNDC(e.x, e.y)
        isSelectionDragging = true
    }

    private fun getClickedObjective(e: MouseEvent): Objective? {
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

    private fun getClickedUnits(e: MouseEvent): List<GameUnit> {
        val clickedPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        val unitDimensionsMin = UNIT_DIMENSIONS.div(-2f, Vector2f())
        val unitDimensionsMax = UNIT_DIMENSIONS.div(2f, Vector2f())
        return editorService.scenario.value!!.units.filter { unit ->
            val positionMatrix = Matrix4f()
            positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
            positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
            val inversePositionMatrix = positionMatrix.invert(Matrix4f())
            val localPoint4f = Vector4f(clickedPoint, 0f, 1f)
                .mul(inversePositionMatrix, Vector4f())
            val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
            unitDimensionsMin.x < localPoint.x && localPoint.x < unitDimensionsMax.x &&
                    unitDimensionsMin.y < localPoint.y && localPoint.y < unitDimensionsMax.y
        }
    }

    private fun checkSingleSelection(e: MouseEvent) {
        val objective = getClickedObjective(e)
        if (objective != null) {
            editorService.selectedUnits.value = setOf()
            editorService.selectedObjectives.value = Reference(editorService.scenario.value!!.objectives.indexOf(objective))
            rerender()
            return
        }

        val newSelectedUnits = getClickedUnits(e).map { unit ->
            Reference<Int, GameUnit>(editorService.scenario.value!!.units.indexOf(unit))
        }
        editorService.selectedObjectives.value = null
        if (!isShiftPressed && !isCtrlPressed) {
            editorService.selectedUnits.value = setOf()
        }
        if (!isCtrlPressed) editorService.selectedUnits.value += newSelectedUnits
        else editorService.selectedUnits.value -= newSelectedUnits.toSet()
        rerender()

    }

    private fun checkEndOfSelection(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON1) return
        isSelectionDragging = false
        if (shouldDragSelectedObjects) {
            shouldDragSelectedObjects = false
            editorService.flushCompound()
            editorService.flushCompoundCommon()
            return
        }
        if (!editorService.selectionEnabled) {
            checkSingleSelection(e)
            return
        }
        editorService.selectionEnabled = false
        val worldPosStart = editorService.fromNDCToWorldSpace(editorService.selectionStart)
        val worldPosEnd = editorService.fromNDCToWorldSpace(editorService.selectionEnd)
        val worldPosMin = worldPosStart.min(worldPosEnd, Vector2f())
        val worldPosMax = worldPosStart.max(worldPosEnd, Vector2f())
        val selectedUnits = editorService.scenario.value!!.units.filter { unit ->
            val pos = unit.position
            worldPosMin.x < pos.x && pos.x < worldPosMax.x &&
                    worldPosMin.y < pos.y && pos.y < worldPosMax.y
        }
        val newSelectedUnits = selectedUnits.map { unit ->
            Reference<Int, GameUnit>(editorService.scenario.value!!.units.indexOf(unit))
        }
        if (newSelectedUnits.isNotEmpty()) editorService.selectedObjectives.value = null
        if (!isShiftPressed && !isCtrlPressed) editorService.selectedUnits.value = setOf()
        if (!isCtrlPressed) editorService.selectedUnits.value += newSelectedUnits
        else editorService.selectedUnits.value -= newSelectedUnits.toSet()
        rerender()
    }

    private fun checkMiddlePressed(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON2) return
        lastX = e.x
        lastY = e.y
        isDragging = true
    }

    private fun checkMiddleReleased(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON2) return
        leftLastX = e.x
        leftLastX = e.y
        isDragging = false
    }

    private fun checkToolUsage(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON3) return
        isToolDragging = true
        val worldCoordinates = editorService.fromScreenToWorldSpace(e.x, e.y)
        if (worldCoordinates.x < 0 || worldCoordinates.y < 0) return
        if (worldCoordinates.x > editorService.scenario.value!!.map.widthPixels || worldCoordinates.y > editorService.scenario.value!!.map.heightPixels) return
        if (toolService.useTool(worldCoordinates.x, worldCoordinates.y)) rerender()
    }

    private fun checkEndOfToolUsage(e: MouseEvent) {
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

        if (scaleX !in 0.08..14.0) return

        // Apply translation adjustment (inverse because view matrix is inverse of camera)
        val translation = newView.getColumn(3, Vector4f())
        translation.x -= delta.x * scaleX
        translation.y -= delta.y * scaleY
        newView.setColumn(3, translation)

        // Update main view matrix and request redraw
        editorService.viewMatrix.set(newView)
        rerender()
    }


}