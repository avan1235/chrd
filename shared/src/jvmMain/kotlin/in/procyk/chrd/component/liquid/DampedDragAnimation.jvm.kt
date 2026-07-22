package `in`.procyk.chrd.component.liquid

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val FRAME_DELAY = (1000L / 60L).milliseconds

internal actual suspend fun awaitFrame() {
    delay(FRAME_DELAY)
}
