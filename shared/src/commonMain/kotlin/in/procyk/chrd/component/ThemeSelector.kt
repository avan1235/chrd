package `in`.procyk.chrd.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chrd.shared.generated.resources.*
import `in`.procyk.chrd.db.ThemeMode
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.settings_appearance),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        val options = ThemeMode.entries
        SingleChoiceSegmentedButtonRow(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            options.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selected,
                    onClick = { if (mode != selected) onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                ) {
                    val labelRes = when (mode) {
                        ThemeMode.SYSTEM -> Res.string.theme_system
                        ThemeMode.LIGHT -> Res.string.theme_light
                        ThemeMode.DARK -> Res.string.theme_dark
                    }
                    Text(stringResource(labelRes))
                }
            }
        }
    }
}
