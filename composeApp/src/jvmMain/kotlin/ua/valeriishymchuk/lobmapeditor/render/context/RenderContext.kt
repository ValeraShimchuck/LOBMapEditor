package ua.valeriishymchuk.lobmapeditor.render.context

import com.jogamp.opengl.GL3
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.domain.unit.GameUnit
import ua.valeriishymchuk.lobmapeditor.render.texture.TextureStorage

data class RenderContext(
    val glCtx: GL3,
    val windowDimensions: Vector2i,
    val textureStorage: TextureStorage,
    val viewMatrix: Matrix4f,
    val projectionMatrix: Matrix4f,
    val scenario: GameScenario.Preset,
    val selectedUnits: List<GameUnit>,
    val selection: SelectionContext
) {
    fun getMvp(model: Matrix4f): Matrix4f {
        return Matrix4f(projectionMatrix)
            .mul(viewMatrix)
            .mul(model)
    }

    data class SelectionContext(
        val enabled: Boolean,
        val selectionStart: Vector2f,
        val selectionEnd: Vector2f
    )

}
