package com.kuzeykapisi.app.data.remote

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun httpEngine(): HttpClientEngineFactory<*> = Js

internal actual val motorAgHatasiniErrorOlarakFirlatir: Boolean = true
