package com.kuzeykapisi.app.data.remote

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun httpEngine(): HttpClientEngineFactory<*> = OkHttp

internal actual val motorAgHatasiniErrorOlarakFirlatir: Boolean = false
