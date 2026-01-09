package xyz.kgy_production.res_mgr

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "res_mgr",
    ) {
        App()
    }
}