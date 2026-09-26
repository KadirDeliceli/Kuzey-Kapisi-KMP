package com.kuzeykapisi.app.data.remote

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun httpEngine(): HttpClientEngineFactory<*> = Darwin

internal actual val motorAgHatasiniErrorOlarakFirlatir: Boolean = false
