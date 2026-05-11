package `in`.procyk.chrd

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import chrd.shared.generated.resources.JetBrainsMono_Bold
import chrd.shared.generated.resources.JetBrainsMono_BoldItalic
import chrd.shared.generated.resources.JetBrainsMono_ExtraBold
import chrd.shared.generated.resources.JetBrainsMono_ExtraBoldItalic
import chrd.shared.generated.resources.JetBrainsMono_ExtraLight
import chrd.shared.generated.resources.JetBrainsMono_ExtraLightItalic
import chrd.shared.generated.resources.JetBrainsMono_Italic
import chrd.shared.generated.resources.JetBrainsMono_Light
import chrd.shared.generated.resources.JetBrainsMono_LightItalic
import chrd.shared.generated.resources.JetBrainsMono_Medium
import chrd.shared.generated.resources.JetBrainsMono_MediumItalic
import chrd.shared.generated.resources.JetBrainsMono_Regular
import chrd.shared.generated.resources.JetBrainsMono_SemiBold
import chrd.shared.generated.resources.JetBrainsMono_SemiBoldItalic
import chrd.shared.generated.resources.JetBrainsMono_Thin
import chrd.shared.generated.resources.JetBrainsMono_ThinItalic
import chrd.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

private val primaryLight = Color(0xFF904A44)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFFFDAD6)
private val onPrimaryContainerLight = Color(0xFF73332E)
private val secondaryLight = Color(0xFF775653)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFFFDAD6)
private val onSecondaryContainerLight = Color(0xFF5D3F3C)
private val tertiaryLight = Color(0xFF8C4A60)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFFD9E2)
private val onTertiaryContainerLight = Color(0xFF703348)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFFFF8F7)
private val onBackgroundLight = Color(0xFF231918)
private val surfaceLight = Color(0xFFFFF8F7)
private val onSurfaceLight = Color(0xFF231918)
private val surfaceVariantLight = Color(0xFFF5DDDB)
private val onSurfaceVariantLight = Color(0xFF534341)
private val outlineLight = Color(0xFF857371)
private val outlineVariantLight = Color(0xFFD8C2BF)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF392E2D)
private val inverseOnSurfaceLight = Color(0xFFFFEDEA)
private val inversePrimaryLight = Color(0xFFFFB4AC)
private val surfaceDimLight = Color(0xFFE8D6D4)
private val surfaceBrightLight = Color(0xFFFFF8F7)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFFF0EF)
private val surfaceContainerLight = Color(0xFFFCEAE8)
private val surfaceContainerHighLight = Color(0xFFF6E4E2)
private val surfaceContainerHighestLight = Color(0xFFF1DEDC)

private val primaryDark = Color(0xFFFFB4AC)
private val onPrimaryDark = Color(0xFF561E1A)
private val primaryContainerDark = Color(0xFF73332E)
private val onPrimaryContainerDark = Color(0xFFFFDAD6)
private val secondaryDark = Color(0xFFE7BDB8)
private val onSecondaryDark = Color(0xFF442927)
private val secondaryContainerDark = Color(0xFF5D3F3C)
private val onSecondaryContainerDark = Color(0xFFFFDAD6)
private val tertiaryDark = Color(0xFFFFB1C8)
private val onTertiaryDark = Color(0xFF541D32)
private val tertiaryContainerDark = Color(0xFF703348)
private val onTertiaryContainerDark = Color(0xFFFFD9E2)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF1A1110)
private val onBackgroundDark = Color(0xFFF1DEDC)
private val surfaceDark = Color(0xFF1A1110)
private val onSurfaceDark = Color(0xFFF1DEDC)
private val surfaceVariantDark = Color(0xFF534341)
private val onSurfaceVariantDark = Color(0xFFD8C2BF)
private val outlineDark = Color(0xFFA08C8A)
private val outlineVariantDark = Color(0xFF534341)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFF1DEDC)
private val inverseOnSurfaceDark = Color(0xFF392E2D)
private val inversePrimaryDark = Color(0xFF904A44)
private val surfaceDimDark = Color(0xFF1A1110)
private val surfaceBrightDark = Color(0xFF423735)
private val surfaceContainerLowestDark = Color(0xFF140C0B)
private val surfaceContainerLowDark = Color(0xFF231918)
private val surfaceContainerDark = Color(0xFF271D1C)
private val surfaceContainerHighDark = Color(0xFF322826)
private val surfaceContainerHighestDark = Color(0xFF3D3231)


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

@Composable
internal fun ChrdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    val jetBrainsMonoFont = FontFamily(
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

    val typography = remember(jetBrainsMonoFont) {
        Typography(
            displayLarge = baseline.displayLarge.copy(fontFamily = jetBrainsMonoFont),
            displayMedium = baseline.displayMedium.copy(fontFamily = jetBrainsMonoFont),
            displaySmall = baseline.displaySmall.copy(fontFamily = jetBrainsMonoFont),
            headlineLarge = baseline.headlineLarge.copy(fontFamily = jetBrainsMonoFont),
            headlineMedium = baseline.headlineMedium.copy(fontFamily = jetBrainsMonoFont),
            headlineSmall = baseline.headlineSmall.copy(fontFamily = jetBrainsMonoFont),
            titleLarge = baseline.titleLarge.copy(fontFamily = jetBrainsMonoFont),
            titleMedium = baseline.titleMedium.copy(fontFamily = jetBrainsMonoFont),
            titleSmall = baseline.titleSmall.copy(fontFamily = jetBrainsMonoFont),
            bodyLarge = baseline.bodyLarge.copy(fontFamily = jetBrainsMonoFont),
            bodyMedium = baseline.bodyMedium.copy(fontFamily = jetBrainsMonoFont),
            bodySmall = baseline.bodySmall.copy(fontFamily = jetBrainsMonoFont),
            labelLarge = baseline.labelLarge.copy(fontFamily = jetBrainsMonoFont),
            labelMedium = baseline.labelMedium.copy(fontFamily = jetBrainsMonoFont),
            labelSmall = baseline.labelSmall.copy(fontFamily = jetBrainsMonoFont),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
