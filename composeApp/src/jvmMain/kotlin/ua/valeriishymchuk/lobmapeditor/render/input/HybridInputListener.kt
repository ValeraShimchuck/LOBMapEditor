package ua.valeriishymchuk.lobmapeditor.render.input

import org.joml.*
import org.kodein.di.DI
import ua.valeriishymchuk.lobmapeditor.commands.UpdateDeploymentZoneCommand
import ua.valeriishymchuk.lobmapeditor.domain.DeploymentZone
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.Position
import ua.valeriishymchuk.lobmapeditor.services.project.editor.HybridEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.tool.HybridToolService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.event.MouseEvent

class HybridInputListener(di: DI) : InputListener<GameScenario.Hybrid>(di) {


    private val hybridEditorService: HybridEditorService by lazy {
        editorService as HybridEditorService
    }

    private enum class ArrowDirection(
        angle: Float,
        val positionGetter: (DeploymentZone) -> Vector2f,
        val positionInverter: (Vector2f, DeploymentZone) -> DeploymentZone,
        val pickAxis: (Vector2f) -> Vector2f,
        val scaleModifier: Float
    ) {
        UP(-90f, { zone ->
            Vector2f(zone.position.x + zone.width / 2, zone.position.y)
        }, { pos, oldZone ->
            val newPos = Vector2f(
                oldZone.position.x,
                pos.y
            )

            val newDimensions = Vector2f(
                oldZone.width,
                oldZone.position.y + oldZone.height - pos.y
            )
            DeploymentZone(
                oldZone.team,
                Position(
                    newPos.x,
                    newPos.y
                ),
                newDimensions.x,
                newDimensions.y
            )
        }, { e -> ArrowDirection.vertical(e) }, 1f),
        DOWN(90f, { zone ->
            Vector2f(zone.position.x + zone.width / 2, zone.position.y + zone.height)
        }, { pos, oldZone ->
            val newPos = Vector2f(
                oldZone.position.x,
                oldZone.position.y
            )

            val newDimensions = Vector2f(
                oldZone.width,
                pos.y - oldZone.position.y
            )
            DeploymentZone(
                oldZone.team,
                Position(
                    newPos.x,
                    newPos.y
                ),
                newDimensions.x,
                newDimensions.y
            )
        }, { e -> ArrowDirection.vertical(e) }, -1f),
        LEFT(180f, { zone ->
            Vector2f(zone.position.x, zone.position.y + zone.height / 2)
        }, { pos, oldZone ->
            val newPos = Vector2f(
                pos.x,
                oldZone.position.y
            )

            val newDimensions = Vector2f(
                oldZone.position.x + oldZone.width - pos.x,
                oldZone.height
            )
            DeploymentZone(
                oldZone.team,
                Position(
                    newPos.x,
                    newPos.y
                ),
                newDimensions.x,
                newDimensions.y
            )
        }, { e -> ArrowDirection.horizontal(e) }, 1f),
        RIGHT(0f, { zone ->
            Vector2f(zone.position.x + zone.width, zone.position.y + zone.height / 2)
        }, { pos, oldZone ->
            val newPos = Vector2f(
                oldZone.position.x,
                oldZone.position.y
            )

            val newDimensions = Vector2f(
                pos.x - oldZone.position.x,
                oldZone.height
            )
            DeploymentZone(
                oldZone.team,
                Position(
                    newPos.x,
                    newPos.y
                ),
                newDimensions.x,
                newDimensions.y
            )
        }, { e -> ArrowDirection.horizontal(e) }, -1f);

        val rotationRadians: Float = Math.toRadians(angle)

        companion object {
            fun vertical(e: Vector2f): Vector2f {
                return Vector2f(0f, e.y)
            }

            fun horizontal(e: Vector2f): Vector2f {
                return Vector2f(e.x, 0f)
            }
        }

    }

    private data class ZoneArrow(
        val reference: Reference<Int, DeploymentZone>,
        val direction: ArrowDirection
    )

    private var currentZoneArrow: ZoneArrow? = null

    private val hybridToolService: HybridToolService by lazy {
        toolService as HybridToolService
    }

    val canSelect: Boolean
        get() = hybridToolService.deploymentZoneTool.canBeSelected.value &&
                !hybridToolService.deploymentZoneTool.isHidden.value

    private fun Int.checkValidIndex(): Int {
        if (this < 0) throw IllegalArgumentException("Index less then 0 $this")
        return this
    }

    override fun onStartOfSelection(e: MouseEvent): Boolean {
        if (!canSelect) return false
        val zone = getClickedZone(e)
        val shiftOrControl = isShiftPressed || isCtrlPressed
        if (zone != null && !shiftOrControl) {
            editorService.selectedObjectives.value = null
            shouldDragSelectedObjects = true
            lastDragPosition = editorService.fromScreenToWorldSpace(e.x, e.y)
            val reference = Reference<Int, DeploymentZone>(
                hybridEditorService.scenario.value!!.deploymentZones.indexOf(zone).checkValidIndex()
            )
            hybridToolService.deploymentZoneTool.selected.value = reference
            return true
        }

        val zoneArrow = getClickedArrow(e)
        if (zoneArrow != null) {
            currentZoneArrow = zoneArrow
            return true
        }

        return false
    }

    override fun onSelectionEndBegin(): Boolean {
        if (currentZoneArrow != null) {
            currentZoneArrow = null
            lastArrowDragPos = null
            hybridEditorService.flushCompound()
            return true
        }
        return false
    }

    override fun onSelectionEnd() {
        if (!canSelect) return
        val worldPosStart = editorService.fromNDCToWorldSpace(editorService.selectionStart)
        val worldPosEnd = editorService.fromNDCToWorldSpace(editorService.selectionEnd)
        val worldPosMin = worldPosStart.min(worldPosEnd, Vector2f())
        val worldPosMax = worldPosStart.max(worldPosEnd, Vector2f())

        val selectedZone = hybridEditorService.scenario.value!!.deploymentZones.firstOrNull { zone ->
            val pos = zone.position
            worldPosMin.x < pos.x && pos.x < worldPosMax.x &&
                    worldPosMin.y < pos.y && pos.y < worldPosMax.y
        }
        val selectedZoneReference = selectedZone?.let { zone ->
            Reference<Int, DeploymentZone>(
                hybridEditorService.scenario.value!!.deploymentZones.indexOf(zone).checkValidIndex()
            )
        }
        if (selectedZoneReference != null) editorService.selectedObjectives.value = null
        hybridToolService.deploymentZoneTool.selected.value = selectedZoneReference
    }

    override fun onSingleSelection(e: MouseEvent) {
        if (!canSelect) return
        val selectedZoneReference = getClickedZone(e)?.let { zone ->
            Reference<Int, DeploymentZone>(
                hybridEditorService.scenario.value!!.deploymentZones.indexOf(
                    zone
                ).checkValidIndex()
            )
        }
        hybridToolService.deploymentZoneTool.selected.value = selectedZoneReference
    }

    private fun getClickedZone(e: MouseEvent): DeploymentZone? {
        val clickedPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        return hybridEditorService.scenario.value!!.deploymentZones.firstOrNull { zone ->
            val zoneDimensionsMin = Vector2f(0f)
            val zoneDimensionsMax = Vector2f(zone.width, zone.height)
            val positionMatrix = Matrix4f()
            positionMatrix.setTranslation(Vector3f(zone.position.x, zone.position.y, 0f))
            val inversePositionMatrix = positionMatrix.invert(Matrix4f())
            val localPoint4f = Vector4f(clickedPoint, 0f, 1f)
                .mul(inversePositionMatrix, Vector4f())
            val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
            zoneDimensionsMin.x < localPoint.x && localPoint.x < zoneDimensionsMax.x &&
                    zoneDimensionsMin.y < localPoint.y && localPoint.y < zoneDimensionsMax.y
        }
    }

    override fun onSelectionClear() {
        hybridToolService.deploymentZoneTool.selected.value = null
    }

    override fun onSelectionDrag(change: Vector2f) {
        if (!canSelect) return
        val selectedZone = hybridToolService.deploymentZoneTool.selected.value
        if (selectedZone != null) {

            val oldZone = selectedZone.getValue(hybridEditorService.scenario.value!!.deploymentZones::get)
            val newPos = Vector2f(oldZone.position.x, oldZone.position.y).add(change)
            val newZone = oldZone.copy(
                position = Position(
                    newPos.x.coerceIn(0f, editorService.scenario.value!!.map.widthPixels.toFloat()),
                    newPos.y.coerceIn(0f, editorService.scenario.value!!.map.heightPixels.toFloat())
                )
            )

            hybridEditorService.executeCompound(
                UpdateDeploymentZoneCommand(
                    selectedZone.key,
                    oldZone,
                    newZone
                )
            )
        }
    }

    private fun getClickedArrow(e: MouseEvent): ZoneArrow? {
        if (!canSelect) return null
        val clickedPoint = editorService.fromScreenToWorldSpace(e.x, e.y)
        val hitboxDimensions = Vector2f(
            54f,
            16f
        )
        val hitboxDimensionsMin = hitboxDimensions.mul(0f, -0.5f, Vector2f())
        val hitboxDimensionsMax = hitboxDimensions.mul(1f, 0.5f, Vector2f())
        return hybridToolService.deploymentZoneTool.selected.value?.let { zoneReference ->
            val zone = zoneReference.getValue(hybridEditorService.scenario.value!!.deploymentZones::get)
            val arrow = ArrowDirection.entries.firstOrNull { direction ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, direction.rotationRadians)
                val pos = direction.positionGetter(zone)
                positionMatrix.setTranslation(Vector3f(pos.x, pos.y, 0f))
                val inversePositionMatrix = positionMatrix.invert(Matrix4f())
                val localPoint4f = Vector4f(clickedPoint, 0f, 1f)
                    .mul(inversePositionMatrix, Vector4f())
                val localPoint = Vector2f(localPoint4f.x, localPoint4f.y)
                hitboxDimensionsMin.x < localPoint.x && localPoint.x < hitboxDimensionsMax.x &&
                        hitboxDimensionsMin.y < localPoint.y && localPoint.y < hitboxDimensionsMax.y
            }
            arrow?.let { ZoneArrow(zoneReference, it) }
        }
    }

    private var lastArrowDragPos: Vector2f? = null

    override fun onArrowDrag(e: MouseEvent) {
        val zoneArrow = currentZoneArrow ?: return
        val zone = zoneArrow.reference.getValue(hybridEditorService.scenario.value!!.deploymentZones::get)
        val worldPos = editorService.fromScreenToWorldSpace(e.x, e.y)
        val currentClickedPos = zoneArrow.direction.pickAxis(worldPos)
        val oldPos = lastArrowDragPos
        if (oldPos == null) {
            lastArrowDragPos = currentClickedPos
            return
        }

        val difference = currentClickedPos.sub(oldPos, Vector2f())
        lastArrowDragPos = currentClickedPos
        val halfHeight = zone.height / 2
        val halfWidth = zone.width / 2
        val center = Vector2f(
            zone.position.x + halfWidth,
            zone.position.y + halfHeight
        ) // might be useful for shift click functionality


        var newZone: DeploymentZone
        if (isCtrlPressed) { // just move
            val newPos = zoneArrow.direction.positionGetter(zone).add(difference)
            newZone = zoneArrow.direction.positionInverter(newPos, zone)
        } else if (isShiftPressed) { // proportionally scale
            val newHalfDimensions = Vector2f(halfWidth, halfHeight).sub(
                difference.mul(
                    zoneArrow.direction.scaleModifier,
                    Vector2f()
                )
            )
            val newDimensions = newHalfDimensions.mul(2f, Vector2f())
            val newZonePos = center.sub(newHalfDimensions, Vector2f())
            newZone = zone.copy(
                position = Position(
                    newZonePos.x,
                    newZonePos.y
                ),
                width = newDimensions.x,
                height = newDimensions.y
            )
        } else { // default click
            newZone = zone.copy(
                position = Position(
                    zone.position.x + difference.x,
                    zone.position.y + difference.y
                )
            )
        }
        val scenario = hybridEditorService.scenario.value!!
        newZone = newZone.copy(
            position = Position(
                newZone.position.x.coerceIn(0f, scenario.map.widthPixels.toFloat()),
                newZone.position.y.coerceIn(0f, scenario.map.heightPixels.toFloat())
            )
        )

        hybridEditorService.executeCompound(
            UpdateDeploymentZoneCommand(
                zoneArrow.reference.key,
                zone,
                newZone
            )
        )

    }

}