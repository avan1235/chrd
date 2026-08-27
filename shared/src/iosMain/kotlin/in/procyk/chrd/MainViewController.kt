package `in`.procyk.chrd

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    ChrdApp(topPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding())
}