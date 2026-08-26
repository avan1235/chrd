package `in`.procyk.chrd.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    title: String? = null,
    topBarVisible: Boolean = true,
    topBar: @Composable () -> Unit = {
        if (title != null) {
            TopAppBar(
                title = { Text(title) }
            )
        }
    },
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = topBarVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + expandVertically() + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically() + fadeOut(),
            ) {
                topBar()
            }
        },
        floatingActionButton = floatingActionButton,
        modifier = modifier.fillMaxSize(),
        content = content,
    )
}
