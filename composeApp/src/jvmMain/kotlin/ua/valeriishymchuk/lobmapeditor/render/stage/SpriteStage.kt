package ua.valeriishymchuk.lobmapeditor.render.stage

import androidx.compose.ui.graphics.Color
import com.jogamp.opengl.GL.GL_TRIANGLES
import org.joml.*
import ua.valeriishymchuk.lobmapeditor.domain.objective.Objective
import ua.valeriishymchuk.lobmapeditor.domain.objective.ObjectiveType
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.unit.*
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit.Companion.UNIT_DIMENSIONS
import ua.valeriishymchuk.lobmapeditor.render.context.HybridRenderContext
import ua.valeriishymchuk.lobmapeditor.render.context.PresetRenderContext
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.SpriteProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max

class SpriteStage(
    ctx: CurrentGL
) : RenderStage {

    private val spriteProgram = SpriteProgram(
        ctx,
        loadShaderSource("vsprite"),
        loadShaderSource("fsprite")
    )

    private fun PresetRenderContext.renderUnitArrows() {
        // arrow body
        also {
            val selectionsToRender = selectedUnits.toList()
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    false,
                    Vector4f(0.3f, 0.3f, 1f, 1f),
                    textureStorage.arrowBody,
                    -1
                )
            )
            val vbo = selectionsToRender.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                val selectionDimensions = Vector2f(
                    48f,
                    8f
                ).mul(0.8f)
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        selectionDimensions.mul(0f, -0.5f, Vector2f()),
                        selectionDimensions.mul(1f, 0.5f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(glCtx, vbo)


            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

        // arrow head
        also {
            val selectionsToRender = selectedUnits.toList()
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    false,
                    Vector4f(0.3f, 0.3f, 1f, 1f),
                    textureStorage.arrowHead,
                    -1
                )
            )
            val vbo = selectionsToRender.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                val selectionDimensions = Vector2f(
                    48f,
                    8f
                ).mul(0.8f)
                val arrowDimensions = Vector2f(
                    32f
                ).mul(0.5f)
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        selectionDimensions.mul(0.8f, 0f, Vector2f())
                            .add(arrowDimensions.mul(0f, -0.5f, Vector2f())),
                        selectionDimensions.mul(0.8f, 0f, Vector2f())
                            .add(arrowDimensions.mul(1f, 0.5f, Vector2f())),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(glCtx, vbo)


            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }
    }

    private fun HybridRenderContext.renderZoneArrows() {
        val selectionReference = toolService.deploymentZoneTool.selected.value ?: return
        val selection = selectionReference.getValue(scenario.deploymentZones::get)

        val positionsAndAngles: List<Pair<Vector2f, Float>> = listOf(
            Vector2f(selection.position.x + selection.width / 2, selection.position.y) to -90f, // top
            Vector2f(selection.position.x + selection.width / 2, selection.position.y + selection.height) to 90f, // bottom
            Vector2f(selection.position.x, selection.position.y + selection.height / 2) to 180f, // left
            Vector2f(selection.position.x + selection.width, selection.position.y + selection.height / 2) to 0f
        ).map { (pos, angle) ->
            pos to Math.toRadians(angle)
        }

        // arrow body
        also {
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    false,
                    Vector4f(0.3f, 0.3f, 1f, 1f),
                    textureStorage.arrowBody,
                    -1
                )
            )
            val vbo = arrayListOf<SpriteProgram.BufferData>()
            positionsAndAngles.forEach { (pos, angle) ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(pos.x, pos.y, 0f))
                positionMatrix.setRotationXYZ(0f, 0f, angle)
                val selectionDimensions = Vector2f(
                    48f,
                    8f
                ).mul(0.8f)
                vbo += SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        selectionDimensions.mul(0f, -0.5f, Vector2f()),
                        selectionDimensions.mul(1f, 0.5f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }


            spriteProgram.setUpVBO(glCtx, vbo)


            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

        // arrow head
        also {
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    false,
                    Vector4f(0.3f, 0.3f, 1f, 1f),
                    textureStorage.arrowHead,
                    -1
                )
            )
            val vbo = arrayListOf<SpriteProgram.BufferData>()
            positionsAndAngles.forEach { (pos, angle) ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(pos.x, pos.y, 0f))
                positionMatrix.setRotationXYZ(0f, 0f, angle)
                val selectionDimensions = Vector2f(
                    48f,
                    8f
                ).mul(0.8f)
                val arrowDimensions = Vector2f(
                    32f
                ).mul(0.5f)
                vbo += SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        selectionDimensions.mul(0.8f, 0f, Vector2f())
                            .add(arrowDimensions.mul(0f, -0.5f, Vector2f())),
                        selectionDimensions.mul(0.8f, 0f, Vector2f())
                            .add(arrowDimensions.mul(1f, 0.5f, Vector2f())),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(glCtx, vbo)


            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

    }

    private fun PresetRenderContext.renderUnit() {
        val unitsToRender: Map<PlayerTeam, Map<Pair<GameUnitType, UnitFormation?>, List<GameUnit>>> = scenario.units
            .groupBy { it.owner.getValue(scenario.players::get).team }
            .mapValues { (_, value) ->
                value.groupBy { it.type to it.formation }
            }

        also {
            val selectionsToRender = selectedUnits.toList()
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    false,
                    true,
                    Vector4f(0f, 0f, 0f, 0.6f),
                    -1,
                    textureStorage.selectionTexture
                )
            )
            val vbo = selectionsToRender.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                val selectionDimensions = Vector2f(
                    32f
                )
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        selectionDimensions.div(-2f, Vector2f()),
                        selectionDimensions.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(glCtx, vbo)


            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

        renderUnitArrows()


        val unitShadowsToRender: MutableMap<String, MutableList<GameUnit>> = mutableMapOf()
        val preparedUnitsToRender: MutableMap<Triple<PlayerTeam, GameUnitType, UnitFormation?>, MutableList<GameUnit>> =
            mutableMapOf()


        unitsToRender.forEach { (team, units) ->
            units.forEach { (pair, unitInfo) ->
                val unitType = pair.first
                val formation = pair.second
                val maskTexture = when (val texture = unitType.texture) {
                    is UnitTypeTexture.Formation -> {
                        texture.map[formation]!!.maskTexture
                    }

                    is UnitTypeTexture.MaskAndOverlay -> texture.maskTexture
                    is UnitTypeTexture.MaskOnly -> texture.maskTexture
                }
                unitShadowsToRender.computeIfAbsent(maskTexture) { mutableListOf() }.addAll(unitInfo)
                preparedUnitsToRender.computeIfAbsent(Triple(team, unitType, formation)) { mutableListOf() }
                    .addAll(unitInfo)
            }
        }

        unitShadowsToRender.forEach {
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    false,
                    Vector4f(0f, 0f, 0f, 0.6f),
                    textureStorage.textures[it.key]!!,
                    -1
                )
            )
            val vbo = it.value.filter { unit -> unit.status != UnitStatus.ROUTING }.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(unit.position.x + 2, unit.position.y + 2, 0f))
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        (unit.formation?.dimensions ?: UNIT_DIMENSIONS).div(-2f, Vector2f()),
                        (unit.formation?.dimensions ?: UNIT_DIMENSIONS).div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(glCtx, vbo)

            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)


        }

        preparedUnitsToRender.forEach { (triple, units) ->
            val unitType = triple.second
            val team = triple.first
            val formation = triple.third
            val overlayTexture = when (val texture = unitType.texture) {
                is UnitTypeTexture.Formation -> texture.map[formation]!!.overlayTexture
                is UnitTypeTexture.MaskAndOverlay -> texture.overlayTexture
                is UnitTypeTexture.MaskOnly -> null
            }

            val maskTexture = when (val texture = unitType.texture) {
                is UnitTypeTexture.Formation -> texture.map[formation]!!.maskTexture
                is UnitTypeTexture.MaskAndOverlay -> texture.maskTexture
                is UnitTypeTexture.MaskOnly -> texture.maskTexture
            }

            // not routing
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    overlayTexture != null,
                    Vector4f(
                        team.color.red,
                        team.color.green,
                        team.color.blue,
                        1f
                    ),
                    textureStorage.textures[maskTexture]!!,
                    overlayTexture?.let { textureStorage.textures[it]!! } ?: -1
                ))

            val vboInput = units.filter { it.status != UnitStatus.ROUTING }.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        (unit.formation?.dimensions ?: UNIT_DIMENSIONS).div(-2f, Vector2f()),
                        (unit.formation?.dimensions ?: UNIT_DIMENSIONS).div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            if (!vboInput.isEmpty()) {
                spriteProgram.setUpVBO(glCtx, vboInput)

                glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vboInput.size)
            }


            // routing
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    overlayTexture != null,
                    Vector4f(
                        team.color.red,
                        team.color.green,
                        team.color.blue,
                        0.5f
                    ),
                    textureStorage.textures[maskTexture]!!,
                    overlayTexture?.let { textureStorage.textures[it]!! } ?: -1
                ))

            val vboInput2 = units.filter { it.status == UnitStatus.ROUTING }.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        (unit.formation?.dimensions ?: UNIT_DIMENSIONS).div(-2f, Vector2f()),
                        (unit.formation?.dimensions ?: UNIT_DIMENSIONS).div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }
            if (!vboInput2.isEmpty()) {
                spriteProgram.setUpVBO(glCtx, vboInput2)

                glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vboInput2.size)
            }


        }
    }

    override fun RenderContext<*>.draw0() {
        glCtx.glUseProgram(spriteProgram.program)
        glCtx.glBindVertexArray(spriteProgram.vao)
        glCtx.glBindVBO(spriteProgram.vbo)


        if (this is PresetRenderContext) {
            renderUnit()
        }

        if (this is HybridRenderContext) {
            renderZoneArrows()
        }

        val objectiveShadowsToRender: List<Objective> = scenario.objectives

        val objectivesToRender: Map<Optional<PlayerTeam>, List<Objective>> = objectiveShadowsToRender.groupBy {
            Optional.ofNullable(it.owner?.let { owner ->
                if (this is PresetRenderContext) {
                    this.scenario.players[owner].team
                } else PlayerTeam.entries[owner]
            })
        }


        val objectiveScale = max((2.5f / viewMatrix.getScale(Vector3f()).x), 1f)


        val objectiveDimensions = Vector2f(
            GameConstants.TILE_SIZE.toFloat()
        ).mul(1.3f)

        val smallObjectiveDimensions = Vector2f(
            GameConstants.TILE_SIZE.toFloat()
        ).mul(0.7f)

        also {
            val selectionsToRender = selectedObjectives.toList()
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    false,
                    true,
                    Vector4f(0f, 0f, 0f, 0.6f),
                    -1,
                    textureStorage.selectionTexture
                )
            )
            val vbo = selectionsToRender.map { objective ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(objective.position.x, objective.position.y, 0f))
                positionMatrix.scale(objectiveScale)
                val selectionDimensions = Vector2f(
                    32f
                )
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        selectionDimensions.div(-2f, Vector2f()),
                        selectionDimensions.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(glCtx, vbo)


            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }

        spriteProgram.setUpVAO(glCtx)
        spriteProgram.applyUniform(
            glCtx, SpriteProgram.Uniform(
                projectionMatrix,
                viewMatrix,
                true,
                false,
                Vector4f(0f, 0f, 0f, 0.6f),
                textureStorage.objectiveMaskTexture,
                -1
            )
        )

        val objectiveShadowsVbo = objectiveShadowsToRender.map { unit ->
            val positionMatrix = Matrix4f()
            positionMatrix.setTranslation(Vector3f(unit.position.x + 1, unit.position.y + 1, 0f))
            positionMatrix.scale(objectiveScale)
            val currentObjective = if (unit.type == ObjectiveType.BIG) objectiveDimensions else smallObjectiveDimensions
            SpriteProgram.BufferData(
                RectanglePoints.fromPoints(
                    currentObjective.div(-2f, Vector2f()),
                    currentObjective.div(2f, Vector2f()),
                ),
                RectanglePoints.TEXTURE_CORDS,
                positionMatrix
            )
        }


        spriteProgram.setUpVBO(glCtx, objectiveShadowsVbo)

        glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * objectiveShadowsVbo.size)

        objectivesToRender.forEach { (teamOpt, objectives) ->
            val color = teamOpt.getOrNull()?.color ?: Color(0.6f, 0.6f, 0.6f, 1f)
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    true,
                    Vector4f(
                        color.red,
                        color.green,
                        color.blue,
                        color.alpha
                    ),
                    textureStorage.objectiveMaskTexture,
                    textureStorage.objectiveOverlayTexture
                )
            )

            val vbo = objectives.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                positionMatrix.scale(objectiveScale)
                val currentObjective =
                    if (unit.type == ObjectiveType.BIG) objectiveDimensions else smallObjectiveDimensions
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        currentObjective.div(-2f, Vector2f()),
                        currentObjective.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }


            spriteProgram.setUpVBO(glCtx, vbo)

            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)
        }
    }
}