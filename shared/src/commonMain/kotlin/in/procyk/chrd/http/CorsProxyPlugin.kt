package `in`.procyk.chrd.http

import io.ktor.client.plugins.api.OnRequestContext
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder

internal val CorsProxyPlugin = createClientPlugin("CorsProxyPlugin") {
    onRequest { request, content -> modifyRequestForCorsProxy(request, content) }
}

internal expect suspend fun OnRequestContext.modifyRequestForCorsProxy(
    request: HttpRequestBuilder,
    content: Any
)