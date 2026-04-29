package ua.valeriishymchuk.lobmapeditor.render.input

import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.commands.Command.Companion.applyAllCompound
import ua.valeriishymchuk.lobmapeditor.commands.UpdateObjectiveListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.tool.ToolService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.TerrainPickTool
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import kotlin.collections.plus
import kotlin.math.abs
import kotlin.math.max

abstract class InputListener<S: GameScenario<S>>(
    override val di: DI
) : MouseAdapter(), MouseMotionListener, KeyListener, DIAware {

    protected var lastMouseX = 0
    protected var lastMouseY = 0
    private val scenario: GameScenario<*> get() = editorService.scenario.value!!

    protected var lastX = 0
    protected var lastY = 0
    protected var isDragging = false

    protected var rotatableUnit: ScenarioReference? = null


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
        editorService.deleteObjects(editorService.selectedObjects.value)
    }

    open fun onArrowDrag(e: MouseEvent) {

        val objReference = rotatableUnit ?: return
        val obj = objReference.dereference(scenario) as? PositionProperty ?: return
        val oldRotation = obj.rotation ?: return
        val pos = Vector2f(obj.position.x, obj.position.y)
        val draggedPos = editorService.fromScreenToWorldSpace(e.x, e.y)
        val differenceVector = draggedPos.sub(pos, Vector2f())
        val unitVector = Vector2f(1f, 0f)
        var newRotation = unitVector.angle(differenceVector)
        if (newRotation < 0f) {
            newRotation += 2 * Math.PI_f
        }
        val differenceRotation = newRotation - oldRotation
        val toUpdate = editorService.selectedObjects.value.map {
            it to it.dereference(scenario) as PositionProperty
        }.filter { it.second.rotation != null }
            .map { it.first }
        ScenarioReference.updateList(PositionProperty::class, toUpdate, scenario) { property ->
            property.withRotation(property.rotation!! + differenceRotation)
        }.applyAllCompound(editorService)
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


    protected fun handleDuplication() {
        if (!isCtrlPressed) return
        val objectsToCopy = editorService.selectedObjects.value.map { it to it.dereference(scenario) }
            .mapNotNull { (it.second as? PositionProperty)?.let { property -> it.first to property } }

        if (objectsToCopy.isEmpty()) return

        val minPos = objectsToCopy.map { Vector2f(it.second.position.x, it.second.position.y) }
            .reduce { vec1, vec2 ->
                vec1.min(vec2, Vector2f())
            }
        val maxPos = objectsToCopy.map { Vector2f(it.second.position.x, it.second.position.y) }
            .reduce { vec1, vec2 ->
                vec1.max(vec2, Vector2f())
            }

        val center = Vector2f((minPos.x + maxPos.x) / 2, (minPos.y + maxPos.y) / 2)
        val mousePos = editorService.fromScreenToWorldSpace(lastMouseX, lastMouseY)
        val difference = mousePos.sub(center, Vector2f())

        val (commands, newReferences) = ScenarioReference.duplicateList(objectsToCopy.map { it.first }, scenario)

        commands.applyAllCompound(editorService)

        ScenarioReference.updateList(PositionProperty::class, newReferences, scenario) { property ->
            property.withPosition(Position(property.position.x + difference.x, property.position.y + difference.y))
        }.applyAllCompound(editorService)

        editorService.flushCompound()

        editorService.selectedObjects.value = newReferences
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
        if (editorService.selectedObjects.value.isNotEmpty()) {
            ScenarioReference.updateList(
                PositionProperty::class,
                editorService.selectedObjects.value, scenario
            ) { obj ->
                val newUnitPos = Vector2f(obj.position.x, obj.position.y).add(change)
                obj.withPosition(
                    Position(
                        newUnitPos.x.coerceIn(0f, editorService.scenario.value!!.map.widthPixels.toFloat()),
                        newUnitPos.y.coerceIn(0f, editorService.scenario.value!!.map.heightPixels.toFloat())
                    )
                )
            }.applyAllCompound(editorService)
        }
    }

    protected fun checkSelectedObjectsDrag(e: MouseEvent): Boolean {
        if (!shouldDragSelectedObjects) return false
        val oldPos = Vector2f(lastDragPosition)
        val newPos = editorService.fromScreenToWorldSpace(e.x, e.y)
        lastDragPosition = newPos
        val change = newPos.sub(oldPos, Vector2f())
        onSelectionDrag(change)
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
        editorService.selectedObjects.value = setOf()
    }

    fun getClickedArrow(e: MouseEvent): ScenarioReference? {
        val clickedPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        val hitboxDimensions = Vector2f(
            54f,
            16f
        )
        val hitboxDimensionsMin = hitboxDimensions.mul(0f, -0.5f, Vector2f())
        val hitboxDimensionsMax = hitboxDimensions.mul(1f, 0.5f, Vector2f())
        return editorService.selectedObjects.value.mapNotNull { reference ->
            (reference.dereference(scenario) as? PositionProperty)?.let { property ->
                reference to property
            }
        }.filter { it.second.rotation != null }.firstOrNull { (reference, obj) ->
            val positionMatrix = Matrix4f()
            positionMatrix.setRotationXYZ(0f, 0f, obj.rotation ?: 0f)
            positionMatrix.setTranslation(Vector3f(obj.position.x, obj.position.y, 0f))
            val inversePositionMatrix = positionMatrix.invert(Matrix4f())
            val localPoint4f = Vector4f(clickedPoint, 0f, 1f)
                .mul(inversePositionMatrix, Vector4f())
            val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
            hitboxDimensionsMin.x < localPoint.x && localPoint.x < hitboxDimensionsMax.x &&
                    hitboxDimensionsMin.y < localPoint.y && localPoint.y < hitboxDimensionsMax.y
        }?.first
    }

    open fun onStartOfSelection(e: MouseEvent): Boolean {
        val objects = getClickedObjects(e)
        val shiftOrControl = isShiftPressed || isCtrlPressed
        if (objects.isNotEmpty() && !shiftOrControl) {
            shouldDragSelectedObjects = true
            lastDragPosition = editorService.fromScreenToWorldSpace(e.x, e.y)
            val firstSelected = objects.firstOrNull { (reference, _) ->
                editorService.selectedObjects.value.contains(reference)
            }

            if (firstSelected == null) {
                editorService.selectedObjects.value = emptySet()
                editorService.selectedObjects.value += objects.first().first
            }
            return true

        }


        val arrowOfUnit = getClickedArrow(e)
        if (arrowOfUnit != null) {
            rotatableUnit = arrowOfUnit
            return true
        }
        return false
    }

    open fun getAllObjects(): List<ScenarioReference> {
        return editorService.getAllObjects().toList()
    }

    fun getAllObjectsWithPosition(): List<Pair<ScenarioReference, PositionProperty<*>>> {
        val allObjects = getAllObjects()
        val filteredObjects = arrayListOf<ScenarioReference>()
        val positionalObjects = arrayListOf<PositionProperty<*>>()

        allObjects.forEach { reference ->
            val obj = reference.dereference(scenario)
            if (obj !is PositionProperty) return@forEach
            filteredObjects.add(reference)
            positionalObjects.add(obj)
        }

        return filteredObjects.indices.map { id ->
            filteredObjects[id] to positionalObjects[id]
        }
    }




    protected fun checkStartOfSelection(e: MouseEvent) {
        if (e.button != MouseEvent.BUTTON1) return
        if (toolService.refenceOverlayTool.hideSprites.value) {
            editorService.selectionStart = editorService.fromScreenToNDC(e.x, e.y)
            editorService.selectionEnd = editorService.fromScreenToNDC(e.x, e.y)
            isSelectionDragging = true
            return
        }


        if (onStartOfSelection(e)) return

        editorService.selectionStart = editorService.fromScreenToNDC(e.x, e.y)
        editorService.selectionEnd = editorService.fromScreenToNDC(e.x, e.y)
        isSelectionDragging = true
    }

    fun getClickedObjects(e: MouseEvent): List<Pair<ScenarioReference, PositionProperty<*>>> {
        val clickPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        val objectiveScale = max((2.5f / editorService.viewMatrix.getScale(Vector3f()).x), 1f)

        return getAllObjectsWithPosition().mapNotNull { (reference, obj) ->

            val hitboxMin = obj.hitboxDimensions.div(
                -2f,
                Vector2f()
            )
            val hitboxMax = obj.hitboxDimensions.div(
                2f,
                Vector2f()
            )

            val positionMatrix = Matrix4f()


            positionMatrix.setRotationXYZ(0f, 0f, obj.rotation ?: 0f)
            positionMatrix.setTranslation(Vector3f(obj.position.x, obj.position.y, 0f))

            if (obj is Objective) {
                positionMatrix.scale(objectiveScale)
            }
            val inversePositionMatrix = positionMatrix.invert(Matrix4f())
            val localPoint4f = Vector4f(clickPoint, 0f, 1f)
                .mul(inversePositionMatrix, Vector4f())
            val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
            val hit = hitboxMin.x < localPoint.x && localPoint.x < hitboxMax.x &&
                    hitboxMin.y < localPoint.y && localPoint.y < hitboxMax.y
            if (!hit) return@mapNotNull null
            reference to obj
        }

    }


    open fun onSingleSelection(e: MouseEvent) {
        val newSelectedObjects = getClickedObjects(e)
        val references = newSelectedObjects.map { it.first }

        if (!isShiftPressed && !isCtrlPressed) {
            editorService.selectedObjects.value = setOf()
        }

        if (!isCtrlPressed) editorService.selectedObjects.value += references
        else editorService.selectedObjects.value -= references
    }

    protected fun checkSingleSelection(e: MouseEvent) {
        if (toolService.refenceOverlayTool.hideSprites.value) return
        onSingleSelection(e)
    }

    open fun onSelectionEndBegin(): Boolean {
        if (rotatableUnit != null) {
            rotatableUnit = null
            editorService.flushCompound()
            editorService.rerenderTrigger.value = !editorService.rerenderTrigger.value
            return true
        }
        return false
    }

    open fun onSelectionEnd() {
        val worldPosStart = editorService.fromNDCToWorldSpace(editorService.selectionStart)
        val worldPosEnd = editorService.fromNDCToWorldSpace(editorService.selectionEnd)
        val worldPosMin = worldPosStart.min(worldPosEnd, Vector2f())
        val worldPosMax = worldPosStart.max(worldPosEnd, Vector2f())

        val selectedObjects = getAllObjectsWithPosition().filter { (reference, obj) ->
            val pos = obj.position
            worldPosMin.x < pos.x && pos.x < worldPosMax.x &&
                    worldPosMin.y < pos.y && pos.y < worldPosMax.y
        }

        val newObjects = selectedObjects.map { it.first }

        if (!isShiftPressed && !isCtrlPressed && !toolService.refenceOverlayTool.hideSprites.value) editorService.selectedObjects.value =
            setOf()
        if (!isCtrlPressed) editorService.selectedObjects.value += newObjects
        else editorService.selectedObjects.value -= newObjects
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
            editorService.flushCompound()
            editorService.rerenderTrigger.value = !editorService.rerenderTrigger.value
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