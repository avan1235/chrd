package `in`.procyk.chrd.http

import `in`.procyk.chrd.shared.ChrdSharedConfig
import io.ktor.client.plugins.api.OnRequestContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom

private const val CHRD_ORIGIN = "chrd"

internal actual suspend fun OnRequestContext.modifyRequestForCorsProxy(
    request: HttpRequestBuilder,
    content: Any
) {
    val originalUrl = request.url.build().toString()
    request.header(HttpHeaders.Origin, CHRD_ORIGIN)
    request.url.takeFrom("${ChrdSharedConfig.CORS_URL}/$originalUrl")
}