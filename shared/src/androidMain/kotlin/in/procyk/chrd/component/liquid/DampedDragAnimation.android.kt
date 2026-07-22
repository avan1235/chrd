package `in`.procyk.chrd.component.liquid

internal actual suspend fun awaitFrame() {
    kotlinx.coroutines.android.awaitFrame()
}
