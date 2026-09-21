package `in`.procyk.chrd.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chrd.shared.generated.resources.Res
import chrd.shared.generated.resources.settings_liquid_glass_navigation
import chrd.shared.generated.resources.settings_title
import `in`.procyk.chrd.component.Screen
import `in`.procyk.chrd.component.ThemeSelector
import `in`.procyk.chrd.component.liquid.LiquidBottomTabsSpacer
import `in`.procyk.chrd.viewmodel.AppSettingsViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel,
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val useLiquidNavigation by viewModel.useLiquidNavigation.collectAsState()

    Screen(title = stringResource(Res.string.settings_title)) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            ThemeSelector(
                selected = themeMode,
                onSelect = { mode -> viewModel.setThemeMode(mode) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(Res.string.settings_liquid_glass_navigation))
                Switch(
                    checked = useLiquidNavigation,
                    onCheckedChange = { use -> viewModel.setUseLiquidNavigation(use) },
                )
            }

            LiquidBottomTabsSpacer(useLiquidNavigation)
        }
    }
}
