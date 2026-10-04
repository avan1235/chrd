@file:OptIn(ExperimentalWasmJsInterop::class)

package `in`.procyk.chrd

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import chrd.shared.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(
        configure = {
            isA11YEnabled = false
        }
    ) {
        var safeAreaInsetTop by remember { mutableStateOf(safeAreaInsetTopPx()) }
        DisposableEffect(Unit) {
            val listener = onWindowResize { safeAreaInsetTop = safeAreaInsetTopPx() }
            onDispose { removeWindowResizeListener(listener) }
        }
        WithFontResourcesLoaded {
            ChrdApp(
                topPadding = safeAreaInsetTop.dp,
                onBackgroundColorChanged = { setPageBackgroundColor(it.toCssHex()) },
            )
        }
    }
}

/**
 * Real `env(safe-area-inset-top)` measured by the `#chrd-safe-area-probe` element from `index.html`.
 * One CSS pixel is one dp in Compose for Web.
 */
private fun safeAreaInsetTopPx(): Float =
    js("document.getElementById('chrd-safe-area-probe')?.getBoundingClientRect().height ?? 0")

private fun onWindowResize(callback: () -> Unit): JsAny =
    js("{ const l = () => callback(); window.addEventListener('resize', l); window.addEventListener('orientationchange', l); return l; }")

private fun removeWindowResizeListener(listener: JsAny): Unit =
    js("{ window.removeEventListener('resize', listener); window.removeEventListener('orientationchange', listener); }")

/**
 * iOS 26 tints the status bar from the `body` background, iOS 27 installed web apps take it from the fixed
 * `.chrd-status-bar` strip (see `styles.css`). Both read `--chrd-background`.
 */
private fun setPageBackgroundColor(color: String): Unit =
    js("{ document.documentElement.style.setProperty('--chrd-background', color); document.querySelectorAll('meta[name=\"theme-color\"]').forEach((m) => m.setAttribute('content', color)); }")

private fun Color.toCssHex(): String =
    "#" + (toArgb() and 0xFFFFFF).toString(16).padStart(6, '0')

@OptIn(ExperimentalResourceApi::class)
@Composable
internal inline fun WithFontResourcesLoaded(
    content: @Composable () -> Unit,
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