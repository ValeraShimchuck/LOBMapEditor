package ua.valeriishymchuk.lobmapeditor.ui.component.project.objects

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.component.styling.ButtonColors
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
import org.jetbrains.jewel.ui.theme.defaultButtonStyle
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.commands.Command.Companion.applyAllCompound
import ua.valeriishymchuk.lobmapeditor.domain.property.NameProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.ObjectiveProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.PositionProperty
import ua.valeriishymchuk.lobmapeditor.domain.property.UnitProperty
import ua.valeriishymchuk.lobmapeditor.domain.reference.ScenarioReference
import ua.valeriishymchuk.lobmapeditor.services.project.editor.EditorService
import kotlin.getValue

@Composable
fun PropertyDock() { // TODO continue adding new properties and add selection dock from old dock
    val editorService by rememberInstance<EditorService<*>>()

    val scenarioNullable by editorService.scenario.collectAsState()
    val scenario = scenarioNullable ?: return

    val selectedObjectReferences by editorService.selectedObjects.collectAsState()
    if (selectedObjectReferences.isEmpty()) return

    VerticallyScrollableContainer {
        Column {
            val flusher = {
                editorService.flushCompound()
            }

            NameProperty.Component(
                selectedObjectReferences,
                { updater ->
                    ScenarioReference.updateList(NameProperty::class, selectedObjectReferences, scenario, updater)
                        .applyAllCompound(editorService)
                },
                flusher
            )

            PositionProperty.Component(
                selectedObjectReferences,
                { updater ->
                    ScenarioReference.updateList(PositionProperty::class, selectedObjectReferences,scenario, updater)
                        .applyAllCompound(editorService)
                },
                flusher
            )

            UnitProperty.Component(
                selectedObjectReferences,
                { updater ->
                    ScenarioReference.updateList(UnitProperty::class, selectedObjectReferences, scenario, updater)
                        .applyAllCompound(editorService)
                },
                flusher
            )

            ObjectiveProperty.Component(
                selectedObjectReferences,
                { updater ->
                    ScenarioReference.updateList(ObjectiveProperty::class, selectedObjectReferences, scenario, updater)
                        .applyAllCompound(editorService)
                },
                flusher
            )

            DefaultButton(
                style = JewelTheme.defaultButtonStyle.let { style ->
                    val color = Color(196, 27, 27, 255)
                    val color2 = Color(182, 25, 25, 255)
                    val color3 = Color(165, 21, 21, 255)
                    ButtonStyle(
                        colors = ButtonColors(
                            Brush.linearGradient(listOf(color, color)),
                            style.colors.backgroundDisabled,
                            Brush.linearGradient(listOf(color, color)),
                            Brush.linearGradient(listOf(color3, color3)),
                            Brush.linearGradient(listOf(color2, color2)),
                            style.colors.content,
                            style.colors.contentDisabled,
                            style.colors.contentFocused,
                            style.colors.contentPressed,
                            style.colors.contentHovered,
                            style.colors.border,
                            style.colors.borderDisabled,
                            style.colors.borderFocused,
                            style.colors.borderPressed,
                            style.colors.borderHovered
                        ),
                        metrics = style.metrics,
                        focusOutlineAlignment = style.focusOutlineAlignment
                    )
                },
                onClick = {
                    editorService.deleteObjects(selectedObjectReferences)
                },
            ) {
                Text(if (selectedObjectReferences.size > 1) "Delete Objects" else "Delete Object")
            }
        }
    }



}

