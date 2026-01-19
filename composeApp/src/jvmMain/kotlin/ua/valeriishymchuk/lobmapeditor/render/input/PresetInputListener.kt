package ua.valeriishymchuk.lobmapeditor.render.input

import org.joml.Math
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.commands.UpdateGameUnitListCommand
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.event.MouseEvent

class PresetInputListener(di: DI) : InputListener<GameScenario.Preset>(di) {

    private var rotatableUnit: Reference<Int, GameUnit>? = null


    override val editorService: PresetEditorService by lazy {
        super.editorService as PresetEditorService
    }

    override fun onStartOfSelection(e: MouseEvent): Boolean {
        val units = getClickedUnits(e)
        val shiftOrControl = isShiftPressed || isCtrlPressed
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
                editorService.selectedUnits.value += Reference(
                    editorService.scenario.value!!.units.indexOf(
                        units.first()
                    )
                )
            }
            return true
        }

        val arrowOfUnit = getClickedArrow(e)
        if (arrowOfUnit != null) {
            rotatableUnit = Reference(editorService.scenario.value!!.units.indexOf(arrowOfUnit))
            return true
        }
        return false
    }

    override fun onSelectionEndBegin(): Boolean {
        if (rotatableUnit != null) {
            rotatableUnit = null
            editorService.flushCompound()
            return true
        }
        return false
    }

    override fun onSelectionEnd() {
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
        if (!isShiftPressed && !isCtrlPressed && !toolService.refenceOverlayTool.hideSprites.value) editorService.selectedUnits.value = setOf()
        if (!isCtrlPressed) editorService.selectedUnits.value += newSelectedUnits
        else editorService.selectedUnits.value -= newSelectedUnits.toSet()
    }

    override fun onSingleSelection(e: MouseEvent) {
        val newSelectedUnits = getClickedUnits(e).map { unit ->
            Reference<Int, GameUnit>(editorService.scenario.value!!.units.indexOf(unit))
        }
        editorService.selectedObjectives.value = null
        if (!isShiftPressed && !isCtrlPressed) {
            editorService.selectedUnits.value = setOf()
        }
        if (!isCtrlPressed) editorService.selectedUnits.value += newSelectedUnits
        else editorService.selectedUnits.value -= newSelectedUnits.toSet()
    }

    private fun getClickedUnits(e: MouseEvent): List<GameUnit> {
        val clickedPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        return editorService.scenario.value!!.units.filter { unit ->
            val unitDimensionsMin = (unit.formation?.dimensions ?: GameUnit.Companion.UNIT_DIMENSIONS).div(-2f,
                Vector2f()
            )
            val unitDimensionsMax = (unit.formation?.dimensions ?: GameUnit.Companion.UNIT_DIMENSIONS).div(2f,
                Vector2f()
            )
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

    private fun getClickedArrow(e: MouseEvent): GameUnit? {
        val clickedPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        val hitboxDimensions = Vector2f(
            54f,
            16f
        )
        val hitboxDimensionsMin = hitboxDimensions.mul(0f, -0.5f, Vector2f())
        val hitboxDimensionsMax = hitboxDimensions.mul(1f, 0.5f, Vector2f())
        return editorService.selectedUnits.value.map { reference ->
            reference.getValue(editorService.scenario.value!!.units::get)
        }.firstOrNull { unit ->
            val positionMatrix = Matrix4f()
            positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
            positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
            val inversePositionMatrix = positionMatrix.invert(Matrix4f())
            val localPoint4f = Vector4f(clickedPoint, 0f, 1f)
                .mul(inversePositionMatrix, Vector4f())
            val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
            hitboxDimensionsMin.x < localPoint.x && localPoint.x < hitboxDimensionsMax.x &&
                    hitboxDimensionsMin.y < localPoint.y && localPoint.y < hitboxDimensionsMax.y
        }
    }

    override fun onSelectionClear() {
        editorService.selectedUnits.value = setOf()
    }

    override fun onSelectionDrag(change: Vector2f) {
        if (editorService.selectedUnits.value.isNotEmpty()) {
            val unitList = editorService.scenario.value!!.units.toMutableList()
            editorService.selectedUnits.value.forEach { reference ->
                val oldUnit = reference.getValue(editorService.scenario.value!!.units::get)
                val newUnitPos = Vector2f(oldUnit.position.x, oldUnit.position.y).add(change)
                val newUnit = oldUnit.copy(
                    position = Position(
                        newUnitPos.x.coerceIn(0f, editorService.scenario.value!!.map.widthPixels.toFloat()),
                        newUnitPos.y.coerceIn(0f, editorService.scenario.value!!.map.heightPixels.toFloat())
                    )
                )
                unitList[reference.key] = newUnit
            }

            editorService.executeCompound(
                UpdateGameUnitListCommand(
                    editorService.scenario.value!!.units,
                    unitList
                )
            )
        }
    }

    override fun onRotation(e: MouseEvent) {
        val unitReference = rotatableUnit ?: return
        val unit = unitReference.getValue(editorService.scenario.value!!.units::get)
        val unitPos = Vector2f(unit.position.x, unit.position.y)
        val draggedPos = editorService.fromScreenToWorldSpace(e.x, e.y)
        val differenceVector = draggedPos.sub(unitPos, Vector2f())
        val unitVector = Vector2f(1f, 0f)
        var newRotation = unitVector.angle(differenceVector)
        if (newRotation < 0f) {
            newRotation += 2 * Math.PI_f
        }
        val oldRotation = unit.rotationRadians
        val differenceRotation = newRotation - oldRotation
        editorService.executeCompound(
            UpdateGameUnitListCommand(
                editorService.scenario.value!!.units,
                editorService.scenario.value!!.units.mapIndexed { index, unit ->
                    val unitToChange = editorService.selectedUnits.value.firstOrNull { reference ->
                        reference.key == index
                    }
                    if (unitToChange == null) return@mapIndexed unit
                    unit.copy(rotationRadians = unit.rotationRadians + differenceRotation)
                }
            ))
    }

    override fun onDelete() {
        editorService.deleteUnits(
            editorService.selectedUnits.value
        )
    }

    override fun onDuplicate() {
        val selectedUnitsToCopy =
            editorService.selectedUnits.value.map { it.getValue(editorService.scenario.value!!.units::get) }
        if (selectedUnitsToCopy.isNotEmpty()) {
            val minPos = selectedUnitsToCopy.map { Vector2f(it.position.x, it.position.y) }
                .reduce { vec1, vec2 ->
                    vec1.min(vec2, Vector2f())
                }
            val maxPos = selectedUnitsToCopy.map { Vector2f(it.position.x, it.position.y) }
                .reduce { vec1, vec2 ->
                    vec1.max(vec2, Vector2f())
                }

            val center = Vector2f((minPos.x + maxPos.x ) / 2, (minPos.y + maxPos.y ) / 2)
            val mousePos = editorService.fromScreenToWorldSpace(lastMouseX, lastMouseY)
            val difference = mousePos.sub(center, Vector2f())
            val unitsToAdd = selectedUnitsToCopy.map {
                it.copy(position = Position(it.position.x + difference.x, it.position.y + difference.y))
            }
            val oldList = editorService.scenario.value!!.units
            val newList = oldList.toMutableList()
            newList.addAll(unitsToAdd)
            editorService.execute(
                UpdateGameUnitListCommand(
                    oldList,
                    newList
                )
            )
        }
    }

}