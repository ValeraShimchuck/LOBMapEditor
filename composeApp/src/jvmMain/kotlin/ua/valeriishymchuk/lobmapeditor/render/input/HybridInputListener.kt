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
import ua.valeriishymchuk.lobmapeditor.services.project.editor.HybridEditorService
import ua.valeriishymchuk.lobmapeditor.services.project.editor.PresetEditorService
import ua.valeriishymchuk.lobmapeditor.shared.refence.Reference
import java.awt.event.MouseEvent

class HybridInputListener(di: DI) : InputListener<GameScenario.Hybrid>(di) {



    private val hybridEditorService: HybridEditorService by lazy {
        editorService as HybridEditorService
    }

}