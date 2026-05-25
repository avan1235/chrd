package `in`.procyk.chrd.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.procyk.chrd.component.Screen
import `in`.procyk.chrd.component.ThemeSelector
import `in`.procyk.chrd.db.AppSettings
import `in`.procyk.chrd.db.AppSettingsRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: AppSettingsRepository,
) {
    val settings by settingsRepository.settings.collectAsState(initial = AppSettings())
    val scope = rememberCoroutineScope()

    Screen(title = "Settings") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            ThemeSelector(
                selected = settings.themeMode,
                onSelect = { mode -> scope.launch { settingsRepository.setThemeMode(mode) } },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
    }
}
