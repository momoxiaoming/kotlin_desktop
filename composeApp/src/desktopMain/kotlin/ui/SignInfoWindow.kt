package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window

/**
 * SignInfoWindow
 *
 * @author mmxm
 * @date 2024/7/15 11:53
 */

@Composable
fun createSignInfoWindow(){
    Window(onCloseRequest = { }) {
        Column {
            Text("我是新弹窗")
        }
    }
}