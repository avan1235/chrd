package `in`.procyk.chrd.http

import io.ktor.client.plugins.api.OnRequestContext
import io.ktor.client.request.HttpRequestBuilder

internal actual suspend fun OnRequestContext.modifyRequestForCorsProxy(
    request: HttpRequestBuilder,
    content: Any
) {
}