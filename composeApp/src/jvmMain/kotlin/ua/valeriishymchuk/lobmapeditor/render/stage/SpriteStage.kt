package ua.valeriishymchuk.lobmapeditor.render.stage

import androidx.compose.ui.graphics.Color
import com.jogamp.opengl.GL.GL_TRIANGLES
import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import ua.valeriishymchuk.lobmapeditor.domain.Objective
import ua.valeriishymchuk.lobmapeditor.domain.player.PlayerTeam
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit.Companion.UNIT_DIMENSIONS
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnitType
import ua.valeriishymchuk.lobmapeditor.render.context.RenderContext
import ua.valeriishymchuk.lobmapeditor.render.geometry.RectanglePoints
import ua.valeriishymchuk.lobmapeditor.render.helper.glBindVBO
import ua.valeriishymchuk.lobmapeditor.render.program.SpriteProgram
import ua.valeriishymchuk.lobmapeditor.render.resource.ResourceLoader.loadShaderSource
import ua.valeriishymchuk.lobmapeditor.shared.GameConstants
import java.util.Optional
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max

class SpriteStage(
    ctx: GL3
): RenderStage {

    private val spriteProgram = SpriteProgram(
        ctx,
        loadShaderSource("vsprite"),
        loadShaderSource("fsprite")
    )

    override fun RenderContext.draw0() {
        glCtx.glUseProgram(spriteProgram.program)
        glCtx.glBindVertexArray(spriteProgram.vao)
        glCtx.glBindVBO(spriteProgram.vbo)


        val unitsToRender: Map<PlayerTeam, Map<GameUnitType, List<GameUnit>>> = scenario.units
            .groupBy { it.owner.getValue(scenario.players::get).team }
            .mapValues { (_, value) ->
                value.groupBy { it.type }
            }

        // rendering selection
        val selectionsToRender = selectedUnits.toList()


        also {
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


        val unitShadowsToRender: MutableMap<String, MutableList<GameUnit>> = mutableMapOf()
        val preparedUnitsToRender: MutableMap<Pair<PlayerTeam, GameUnitType>, MutableList<GameUnit>> = mutableMapOf()


        unitsToRender.forEach { (team, units) ->
            units.forEach { (unitType, unitInfo) ->
                unitShadowsToRender.computeIfAbsent(unitType.maskTexture) { mutableListOf() }.addAll(unitInfo)
                preparedUnitsToRender.computeIfAbsent(team to unitType) { mutableListOf() }.addAll(unitInfo)
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
            val vbo = it.value.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(unit.position.x + 2, unit.position.y + 2, 0f))
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        UNIT_DIMENSIONS.div(-2f, Vector2f()),
                        UNIT_DIMENSIONS.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }

            spriteProgram.setUpVBO(glCtx, vbo)

            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vbo.size)


        }

        preparedUnitsToRender.forEach { (teamUnitType, units) ->
            spriteProgram.setUpVAO(glCtx)
            spriteProgram.applyUniform(
                glCtx, SpriteProgram.Uniform(
                    projectionMatrix,
                    viewMatrix,
                    true,
                    teamUnitType.second.overlayTexture != null,
                    Vector4f(
                        teamUnitType.first.color.red,
                        teamUnitType.first.color.green,
                        teamUnitType.first.color.blue,
                        1f
                    ),
                    textureStorage.textures[teamUnitType.second.maskTexture]!!,
                    teamUnitType.second.overlayTexture?.let { textureStorage.textures[it]!! } ?: -1
                ))

            val vboInput = units.map { unit ->
                val positionMatrix = Matrix4f()
                positionMatrix.setRotationXYZ(0f, 0f, unit.rotationRadians)
                positionMatrix.setTranslation(Vector3f(unit.position.x, unit.position.y, 0f))
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        UNIT_DIMENSIONS.div(-2f, Vector2f()),
                        UNIT_DIMENSIONS.div(2f, Vector2f()),
                    ),
                    RectanglePoints.TEXTURE_CORDS,
                    positionMatrix
                )
            }


            spriteProgram.setUpVBO(glCtx, vboInput)

            glCtx.glDrawArrays(GL_TRIANGLES, 0, 6 * vboInput.size)
        }

        val objectiveShadowsToRender: List<Objective> = scenario.objectives
        val objectivesToRender: Map<Optional<PlayerTeam>, List<Objective>> = objectiveShadowsToRender.groupBy {
            Optional.ofNullable(it.owner?.getValue(scenario.players::get)?.team)
        }


        val objectiveScale = max((2.5f / viewMatrix.getScale(Vector3f()).x), 1f)


        val objectiveDimensions = Vector2f(
            GameConstants.TILE_SIZE.toFloat()
        ).mul(1.3f)

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
            SpriteProgram.BufferData(
                RectanglePoints.fromPoints(
                    objectiveDimensions.div(-2f, Vector2f()),
                    objectiveDimensions.div(2f, Vector2f()),
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
                SpriteProgram.BufferData(
                    RectanglePoints.fromPoints(
                        objectiveDimensions.div(-2f, Vector2f()),
                        objectiveDimensions.div(2f, Vector2f()),
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