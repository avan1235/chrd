package `in`.procyk.chrd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val topPadding = if (isMobileClient()) 40.dp else 0.dp
    ComposeViewport {
        WithFontResourcesLoaded {
            ChrdApp(topPadding = topPadding)
        }
    }
}

private fun isMobileClient(): Boolean {
    val userAgent = js("navigator.userAgent") as? String ?: ""
    val maxTouchPoints = (js("navigator.maxTouchPoints") as? Number)?.toInt() ?: 0
    val hasCoarsePointer = js("window.matchMedia('(pointer: coarse)').matches") as? Boolean ?: false

    val mobileUserAgent = userAgent.contains(Regex("Android|iPhone|iPad|iPod|Mobile", RegexOption.IGNORE_CASE))
    return mobileUserAgent || (hasCoarsePointer && maxTouchPoints > 0)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal inline fun WithFontResourcesLoaded(
    content: @Composable () -> Unit
) {
    val jetBrainsMonoBold by preloadFont(Res.font.JetBrainsMono_Bold)
    val jetBrainsMonoBoldItalic by preloadFont(Res.font.JetBrainsMono_BoldItalic)
    val jetBrainsMonoExtraBold by preloadFont(Res.font.JetBrainsMono_ExtraBold)
    val jetBrainsMonoExtraBoldItalic by preloadFont(Res.font.JetBrainsMono_ExtraBoldItalic)
    val jetBrainsMonoExtraLight by preloadFont(Res.font.JetBrainsMono_ExtraLight)
    val jetBrainsMonoExtraLightItalic by preloadFont(Res.font.JetBrainsMono_ExtraLightItalic)
    val jetBrainsMonoItalic by preloadFont(Res.font.JetBrainsMono_Italic)
    val jetBrainsMonoLight by preloadFont(Res.font.JetBrainsMono_Light)
    val jetBrainsMonoLightItalic by preloadFont(Res.font.JetBrainsMono_LightItalic)
    val jetBrainsMonoMedium by preloadFont(Res.font.JetBrainsMono_Medium)
    val jetBrainsMonoMediumItalic by preloadFont(Res.font.JetBrainsMono_MediumItalic)
    val jetBrainsMonoRegular by preloadFont(Res.font.JetBrainsMono_Regular)
    val jetBrainsMonoSemiBold by preloadFont(Res.font.JetBrainsMono_SemiBold)
    val jetBrainsMonoSemiBoldItalic by preloadFont(Res.font.JetBrainsMono_SemiBoldItalic)
    val jetBrainsMonoThin by preloadFont(Res.font.JetBrainsMono_Thin)
    val jetBrainsMonoThinItalic by preloadFont(Res.font.JetBrainsMono_ThinItalic)

    var fontFallbackInitialized by remember { mutableStateOf(false) }
    val fontFamilyResolver = LocalFontFamilyResolver.current

    LaunchedEffect(
        fontFamilyResolver,
        jetBrainsMonoBold,
        jetBrainsMonoBoldItalic,
        jetBrainsMonoExtraBold,
        jetBrainsMonoExtraBoldItalic,
        jetBrainsMonoExtraLight,
        jetBrainsMonoExtraLightItalic,
        jetBrainsMonoItalic,
        jetBrainsMonoLight,
        jetBrainsMonoLightItalic,
        jetBrainsMonoMedium,
        jetBrainsMonoMediumItalic,
        jetBrainsMonoRegular,
        jetBrainsMonoSemiBold,
        jetBrainsMonoSemiBoldItalic,
        jetBrainsMonoThin,
        jetBrainsMonoThinItalic,
    ) {
        val fonts = listOf(
            jetBrainsMonoBold,
            jetBrainsMonoBoldItalic,
            jetBrainsMonoExtraBold,
            jetBrainsMonoExtraBoldItalic,
            jetBrainsMonoExtraLight,
            jetBrainsMonoExtraLightItalic,
            jetBrainsMonoItalic,
            jetBrainsMonoLight,
            jetBrainsMonoLightItalic,
            jetBrainsMonoMedium,
            jetBrainsMonoMediumItalic,
            jetBrainsMonoRegular,
            jetBrainsMonoSemiBold,
            jetBrainsMonoSemiBoldItalic,
            jetBrainsMonoThin,
            jetBrainsMonoThinItalic,
        )
        val nonNullFonts = fonts.filterNotNull()
        if (nonNullFonts.size != fonts.size) return@LaunchedEffect

        nonNullFonts.forEach { fontFamilyResolver.preload(FontFamily(it)) }
        fontFallbackInitialized = true
    }

    if (fontFallbackInitialized) {
        content()
    }
}