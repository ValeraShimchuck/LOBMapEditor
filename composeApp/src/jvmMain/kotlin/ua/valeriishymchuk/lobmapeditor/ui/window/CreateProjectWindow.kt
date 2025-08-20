package ua.valeriishymchuk.lobmapeditor.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.konform.validation.Validation
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import ua.valeriishymchuk.lobmapeditor.ui.TitleBarView
import ua.valeriishymchuk.lobmapeditor.ui.composable.WindowScope



data class LoginForm(val email: String = "", val password: String = "")

val validator = Validation<LoginForm> {
    LoginForm::email {
        minLength(5) hint "Email мінімум 5 символів"
        pattern(".+@.+\\..+") hint "Некоректний email"
    }
    LoginForm::password {
        minLength(6) hint "Пароль мінімум 6 символів"
    }
}

@Composable
fun CreateProjectWindow() {
    var form by remember { mutableStateOf(LoginForm()) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    fun validate() {
        val result = validator.validate(form)
        errors = result.errors.associate { error ->
            val key = error.dataPath.lastOrNull()?.toString() ?: ""
            val message = error.message ?: ""
            key to message // явно Pair<String, String>
        }
    }

    Column(Modifier.padding(16.dp)) {
        TextField(
            value = TextFieldValue(form.email),
            onValueChange = { form = form.copy(email = it.text) },
        )


        Spacer(Modifier.height(8.dp))

        errors.forEach { (s, s2) ->
            Text("$s: $s2")
        }

        DefaultButton(onClick = { validate() }) { Text("Увійти") }
    }
}