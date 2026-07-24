package `in`.procyk.chrd

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import chrd.shared.generated.resources.*
import `in`.procyk.chrd.db.ThemeMode
import org.jetbrains.compose.resources.Font

private val primaryLight = Color(0xFF4355B9)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFDEE0FF)
private val onPrimaryContainerLight = Color(0xFF00105C)
private val secondaryLight = Color(0xFF5B5D72)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFE0E1F9)
private val onSecondaryContainerLight = Color(0xFF181A2C)
private val tertiaryLight = Color(0xFF77536D)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFFD7F1)
private val onTertiaryContainerLight = Color(0xFF2D1228)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF410002)
private val backgroundLight = Color(0xFFFEFBFF)
private val onBackgroundLight = Color(0xFF1B1B1F)
private val surfaceLight = Color(0xFFFEFBFF)
private val onSurfaceLight = Color(0xFF1B1B1F)
private val surfaceVariantLight = Color(0xFFE3E1EC)
private val onSurfaceVariantLight = Color(0xFF46464F)
private val outlineLight = Color(0xFF767680)
private val outlineVariantLight = Color(0xFFC7C5D0)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF303034)
private val inverseOnSurfaceLight = Color(0xFFF2F0F4)
private val inversePrimaryLight = Color(0xFFBAC3FF)
private val surfaceDimLight = Color(0xFFDCD9DE)
private val surfaceBrightLight = Color(0xFFFEFBFF)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF5F3F7)
private val surfaceContainerLight = Color(0xFFEFEDF1)
private val surfaceContainerHighLight = Color(0xFFE9E7EC)
private val surfaceContainerHighestLight = Color(0xFFE3E1E6)

private val primaryDark = Color(0xFFBAC3FF)
private val onPrimaryDark = Color(0xFF08218A)
private val primaryContainerDark = Color(0xFF293CA0)
private val onPrimaryContainerDark = Color(0xFFDEE0FF)
private val secondaryDark = Color(0xFFC3C5DD)
private val onSecondaryDark = Color(0xFF2D2F42)
private val secondaryContainerDark = Color(0xFF434559)
private val onSecondaryContainerDark = Color(0xFFE0E1F9)
private val tertiaryDark = Color(0xFFE6BACC)
private val onTertiaryDark = Color(0xFF45263D)
private val tertiaryContainerDark = Color(0xFF5D3C54)
private val onTertiaryContainerDark = Color(0xFFFFD7F1)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF1B1B1F)
private val onBackgroundDark = Color(0xFFE3E1E6)
private val surfaceDark = Color(0xFF1B1B1F)
private val onSurfaceDark = Color(0xFFE3E1E6)
private val surfaceVariantDark = Color(0xFF46464F)
private val onSurfaceVariantDark = Color(0xFFC7C5D0)
private val outlineDark = Color(0xFF90909A)
private val outlineVariantDark = Color(0xFF46464F)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFE3E1E6)
private val inverseOnSurfaceDark = Color(0xFF303034)
private val inversePrimaryDark = Color(0xFF4355B9)
private val surfaceDimDark = Color(0xFF1B1B1F)
private val surfaceBrightDark = Color(0xFF3B3B3F)
private val surfaceContainerLowestDark = Color(0xFF0E0E12)
private val surfaceContainerLowDark = Color(0xFF1B1B1F)
private val surfaceContainerDark = Color(0xFF202023)
private val surfaceContainerHighDark = Color(0xFF2A2A2E)
private val surfaceContainerHighestDark = Color(0xFF353539)


private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val baseline = Typography()

internal object ChrdFonts {
    val mono: FontFamily
        @Composable
        get() = FontFamily(
            Font(Res.font.JetBrainsMono_Bold),
            Font(Res.font.JetBrainsMono_BoldItalic),
            Font(Res.font.JetBrainsMono_ExtraBold),
            Font(Res.font.JetBrainsMono_ExtraBoldItalic),
            Font(Res.font.JetBrainsMono_ExtraLight),
            Font(Res.font.JetBrainsMono_ExtraLightItalic),
            Font(Res.font.JetBrainsMono_Italic),
            Font(Res.font.JetBrainsMono_Light),
            Font(Res.font.JetBrainsMono_LightItalic),
            Font(Res.font.JetBrainsMono_Medium),
            Font(Res.font.JetBrainsMono_MediumItalic),
            Font(Res.font.JetBrainsMono_Regular),
            Font(Res.font.JetBrainsMono_SemiBold),
            Font(Res.font.JetBrainsMono_SemiBoldItalic),
            Font(Res.font.JetBrainsMono_Thin),
            Font(Res.font.JetBrainsMono_ThinItalic),
        )

}

@Composable
internal fun ChrdTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable() () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
