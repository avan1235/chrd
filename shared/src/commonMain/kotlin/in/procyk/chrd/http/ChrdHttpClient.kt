package `in`.procyk.chrd.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

internal fun ChrdHttpClient(
    block: HttpClientConfig<*>.() -> Unit = {}
) = HttpClient {
    block()
    install(CorsProxyPlugin)
}
