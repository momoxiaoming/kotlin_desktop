
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "脚本集合-XM",
        state = rememberWindowState(width = 900.dp, height = 700.dp),
    ) {
        App()
    }
}