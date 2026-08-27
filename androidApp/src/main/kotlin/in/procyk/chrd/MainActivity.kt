package `in`.procyk.chrd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val view = LocalView.current
            val density = LocalDensity.current
            val topPadding = remember(view, density) {
                val insets = ViewCompat.getRootWindowInsets(view)
                val topInsetPx = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
                with(density) { topInsetPx.toDp() }
            }
            ChrdApp(topPadding = topPadding)
        }
    }
}
