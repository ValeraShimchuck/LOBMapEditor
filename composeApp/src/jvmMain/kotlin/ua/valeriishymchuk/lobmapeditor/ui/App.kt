package ua.valeriishymchuk.lobmapeditor.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import cafe.adriel.voyager.navigator.CurrentScreen
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.util.FPSAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextArea
import org.jetbrains.jewel.ui.theme.popupContainerStyle
import org.kodein.di.bindProvider
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import ua.valeriishymchuk.lobmapeditor.domain.GameScenario
import ua.valeriishymchuk.lobmapeditor.render.EditorRenderer
import ua.valeriishymchuk.lobmapeditor.render.InputListener
import ua.valeriishymchuk.lobmapeditor.services.ErrorService
import ua.valeriishymchuk.lobmapeditor.services.ToastService
import ua.valeriishymchuk.lobmapeditor.services.project.EditorService
import java.awt.Dimension


@Composable
@Preview
fun App() {
    val errorService by rememberInstance<ErrorService>()
    val currentError by errorService.error.collectAsState()
    val toastService by rememberInstance<ToastService>()
    val toasts by toastService.toasts.collectAsState()


    Box {
        CurrentScreen()

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
                .widthIn(min = 100.dp, max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            toasts.forEach {
                it()
            }
        }

        if (currentError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // напівпрозорий фон
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { currentError!!.onDismiss() }, // клік по фону закриває
                contentAlignment = Alignment.Center,

                ) {
                val style = JewelTheme.popupContainerStyle
                val colors = style.colors
                val popupShape = RoundedCornerShape(style.metrics.cornerSize)


                Column(
                    modifier =
                        Modifier
                            .shadow(
                                elevation = style.metrics.shadowSize,
                                shape = popupShape,
                                ambientColor = colors.shadow,
                                spotColor = colors.shadow,
                            )
                            .border(Stroke.Alignment.Inside, style.metrics.borderWidth, colors.border, popupShape)
                            .background(colors.background, popupShape)
                            .padding(10.dp)
                            .widthIn(min = 400.dp, max = 500.dp)
                            .heightIn(min = 220.dp, max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        when (currentError!!.severity) {
                            ErrorService.AppError.Severity.Error -> "Error"
                            ErrorService.AppError.Severity.Warning -> "Warning"
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 1.2.em
                    )
                    currentError?.message?.let { Text(it) }
                    currentError?.cause?.let {
                        val rememberState by remember { mutableStateOf(TextFieldState(initialText = it.stackTraceToString())) }

                        TextArea(
                            state = rememberState,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            readOnly = true,
                            textStyle = JewelTheme.editorTextStyle.copy(color = JewelTheme.globalColors.text.error)
                        )
                    }
                    Spacer(Modifier.weight(0.1f))
                    currentError?.onDismiss?.let {
                        OutlinedButton(
                            { if (it()) errorService.error.value = null },
                            Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Dismiss")
                        }
                    }

                }
            }
        }
    }

}


@Composable
fun JoglCanvas(canvasRefSet: (GLCanvas) -> Unit) {
    val di = localDI()
    val editorService by di.instance<EditorService<GameScenario.Preset>>()
    val updaterObserver by editorService.openglUpdateState.collectAsState()
    var glListener by remember { mutableStateOf(
        EditorRenderer(di)
    ) }

    val shouldRunError by editorService.throwTestError.collectAsState()

    if (shouldRunError) {
        throw Exception("Test error")
    }

    val canvas = remember {
        println("Initializing factory")
        val profile = GLProfile.get(GLProfile.GL3)
        val capabilities = GLCapabilities(profile)
//        capabilities.isPBuffer = true
        capabilities.apply {
            doubleBuffered = true
            depthBits = 24
        }



        GLCanvas(capabilities).apply {
            name = "MainGLCanvas $updaterObserver"  // For debugging
            setSize(800, 600)
            preferredSize = Dimension(800, 600)
            GLProfile.initSingleton()
            println("Initializing GLProfile singleton")

            addGLEventListener(glListener)
            val inputListener = InputListener(di)
            addMouseMotionListener(inputListener)
            addMouseListener(inputListener)
            addMouseWheelListener(inputListener)
            addKeyListener(inputListener)

            isVisible = true
            canvasRefSet(this)

            val animator = FPSAnimator(this, 60)
            animator.start()


        }
    }
    LaunchedEffect(updaterObserver) {
        println("render jogl canvas $updaterObserver")
        val oldListener = glListener
        glListener = EditorRenderer(di)
        canvas.removeGLEventListener(oldListener)
        canvas.addGLEventListener(glListener)
    }
    SwingPanel(
        factory = {
            canvas
        },
        modifier = Modifier.fillMaxSize()

    )
}