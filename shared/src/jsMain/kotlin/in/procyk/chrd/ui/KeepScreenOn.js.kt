package `in`.procyk.chrd.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
private var webWakeLock: JsAny? = null

private fun requestBrowserWakeLock(): Unit = js(
    """{
        if ('wakeLock' in navigator) {
            navigator.wakeLock.request('screen')
                .then(function(lock) {
                    window.webWakeLock = lock;
                })
                .catch(function(err) {
                    console.warn('Wake lock request failed: ', err);
                });
        }
    }"""
)

private fun releaseBrowserWakeLock(): Unit = js(
    """{
        if (window.webWakeLock) {
            window.webWakeLock.release()
                .then(function() {
                    window.webWakeLock = null;
                });
        }
    }"""
)

@Composable
actual fun KeepScreenOn() {
    DisposableEffect(Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            requestBrowserWakeLock()
        }
        
        onDispose {
            releaseBrowserWakeLock()
        }
    }
}
