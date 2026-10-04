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
    // Non-zero only when the page is drawn under the status bar (e.g. a legacy iOS web clip installed with
    // `black-translucent`); with `apple-mobile-web-app-status-bar-style=default` the viewport already starts
    // below the status bar and this resolves to 0.
    val topPadding = if (isMobileClient()) safeAreaInsetTopPx().dp else 0.dp
    ComposeViewport(
        configure = {
            isA11YEnabled = false
        }
    ) {
        WithFontResourcesLoaded {
            ChrdApp(
                topPadding = topPadding,
                onThemeChanged = ::applyPageTheme,
            )
        }
    }
}

private fun navigatorUserAgent(): String = js("navigator.userAgent")
private fun navigatorMaxTouchPoints(): Int = js("navigator.maxTouchPoints")
private fun windowHasCoarsePointer(): Boolean = js("window.matchMedia('(pointer: coarse)').matches")
private fun safeAreaInsetTopPx(): Double = js(
    "(function () {" +
        "var probe = document.createElement('div');" +
        "probe.style.cssText = 'position:fixed;top:0;left:0;width:0;height:0;visibility:hidden;pointer-events:none;padding-top:env(safe-area-inset-top, 0px)';" +
        "document.body.appendChild(probe);" +
        "var value = parseFloat(getComputedStyle(probe).paddingTop) || 0;" +
        "document.body.removeChild(probe);" +
        "return value;" +
    "})()"
)

/**
 * Keeps the host page in sync with the Compose theme. The iOS status bar color (`#ios-status-bar-backdrop` and the
 * page root background in styles.css) is driven by the `--app-background` CSS variable, which by default follows
 * `prefers-color-scheme`; when the user forces light/dark in the app settings the system preference no longer
 * matches, so the resolved colors are pushed here explicitly. `theme-color` is updated as well for Android/older iOS.
 */
private fun applyPageTheme(isDark: Boolean, background: Color) {
    val argb = background.toArgb()
    val hex = (argb and 0xFFFFFF).toString(16).padStart(6, '0')
    setPageTheme(if (isDark) "dark" else "light", "#$hex")
}

private fun setPageTheme(colorScheme: String, background: String): Unit = js(
    "(function () {" +
        "var root = document.documentElement;" +
        "root.style.setProperty('--app-background', background);" +
        "root.style.colorScheme = colorScheme;" +
        "var metas = document.querySelectorAll('meta[name=\"theme-color\"]');" +
        "for (var i = 0; i < metas.length; i++) {" +
            "metas[i].removeAttribute('media');" +
            "metas[i].setAttribute('content', background);" +
        "}" +
    "})()"
)

private fun isMobileClient(): Boolean {
    val userAgent = navigatorUserAgent()
    val maxTouchPoints = navigatorMaxTouchPoints()
    val hasCoarsePointer = windowHasCoarsePointer()

    val mobileUserAgent = userAgent.contains(Regex("Android|iPhone|iPad|iPod|Mobile", RegexOption.IGNORE_CASE))
    return mobileUserAgent || (hasCoarsePointer && maxTouchPoints > 0)
}

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