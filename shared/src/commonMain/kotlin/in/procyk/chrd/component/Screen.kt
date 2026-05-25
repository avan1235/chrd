package `in`.procyk.chrd.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
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
