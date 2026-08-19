@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.data.model

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private external interface JsGeolocationCoordinates : JsAny {
    val latitude: Double
    val longitude: Double
}

private external interface JsGeolocationPosition : JsAny {
    val coords: JsGeolocationCoordinates
}

private external interface JsGeolocation : JsAny {
    fun getCurrentPosition(success: (JsGeolocationPosition) -> Unit, error: (JsAny) -> Unit)
}

private external interface JsNavigatorGeolocation : JsAny {
    val geolocation: JsGeolocation?
}

private fun jsNavigator(): JsNavigatorGeolocation = js("navigator")

actual suspend fun guncelKonumAl(): Konum {
    return try {
        val geo = jsNavigator().geolocation ?: return VARSAYILAN_KONUM
        withTimeoutOrNull(5000L) {
            suspendCancellableCoroutine<Konum> { cont ->
                geo.getCurrentPosition(
                    success = { pos ->
                        if (cont.isActive) cont.resume(Konum(pos.coords.latitude, pos.coords.longitude))
                    },
                    error = {
                        if (cont.isActive) cont.resume(VARSAYILAN_KONUM)
                    },
                )
            }
        } ?: VARSAYILAN_KONUM
    } catch (e: Throwable) {
        VARSAYILAN_KONUM
    }
}
