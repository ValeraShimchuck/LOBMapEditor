package ua.valeriishymchuk.lobmapeditor.ui.screen.project

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.jewel.ui.component.IconActionButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.SuccessInlineBanner
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.kodein.di.compose.rememberInstance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.services.ScenarioIOService
import ua.valeriishymchuk.lobmapeditor.services.ToastService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import ua.valeriishymchuk.lobmapeditor.shared.editor.ProjectRef
import ua.valeriishymchuk.lobmapeditor.ui.screen.HomeScreen
import ua.valeriishymchuk.lobmapeditor.ui.screen.TitleBarScreen
import java.awt.Desktop
import kotlin.getValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleBarScreen.ProjectTitleScreenProvider() {
    val editorService by rememberInstance<EditorService<GameScenario.Preset>>()
    val scenarioIO by rememberInstance<ScenarioIOService>()
    val toastService by rememberInstance<ToastService>()
    val ref by rememberInstance<ProjectRef>()

    val nav = LocalNavigator.currentOrThrow
    TitleBar.value = {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Start).padding(8.dp)
        ) {

            Tooltip(
                { Text("Back to home screen") }
            ) {
                IconActionButton(
                    AllIconsKeys.General.ChevronLeft,
                    null,
                    onClick = {
                        nav.push(HomeScreen)
                    }
                )
            }

            Spacer(Modifier.width(4.dp))


            Tooltip(
                { Text("Export map") }
            ) {
                IconActionButton(
                    AllIconsKeys.General.Export,
                    null,
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {

                            val file = FileKit.openFileSaver(
                                editorService.scenario.value?.name ?: "exported_map",
                                "json",
                                PlatformFile(ref.mapFile),
                            ) ?: return@launch

                            scenarioIO.save(editorService.scenario.value!!, file.file)

                            toastService.toast() {
                                SuccessInlineBanner(
                                    "Map exported at: ${file.file.absoluteFile}",
                                    actions = {
                                        OutlinedButton(onClick = {
                                            Desktop.getDesktop().open(file.file.parentFile)
                                        }) {
                                            Text("Open export path folder")
                                        }
                                    }
                                )
                            }

                            /*scenarioIO.save(editorService.scenario.value!!, ref.mapFile)
                            toastService.toast() {
                                SuccessInlineBanner(
                                    "Map saved at: ${ref.mapFile.absoluteFile}",
                                    actions = {
                                        OutlinedButton(onClick = {
                                            Desktop.getDesktop().open(ref.dirFile)
                                        }) {
                                            Text("Open project folder")
                                        }
                                    }
                                )
                            }*/


                        }
                    }
                )
            }

            Spacer(Modifier.width(4.dp))

            Tooltip(
                { Text("Import map") }
            ) {
                IconActionButton(
                    AllIconsKeys.ToolbarDecorator.Import,
                    null,
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {

                            val file = FileKit.openFilePicker(type = FileKitType.File("json")) ?: return@launch

                            val newScenario = scenarioIO.load(file.file) as? GameScenario.Preset ?: return@launch

                            editorService.importScenario(newScenario)
//                            editorService.scenario.value = newScenario
//                            editorService.save(true)

                            toastService.toast() {
                                SuccessInlineBanner(
                                    "Map imported from: ${file.file.absoluteFile}",
                                    actions = {
//                                        OutlinedButton(onClick = {
//                                            Desktop.getDesktop().open(file.file.parentFile)
//                                        }) {
//                                            Text("Open export path folder")
//                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.width(4.dp))

            Text(editorService.scenario.value?.name ?: "map")
        }
        Text("LOBMapEditor")

    }
}