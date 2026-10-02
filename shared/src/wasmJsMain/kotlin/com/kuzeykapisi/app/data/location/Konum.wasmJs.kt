@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.data.location

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

private const val KONUM_GUVENLIK_SINIRI_MS = 25_000L

/**
 * Tarayıcının geolocation izin durumu: "granted" / "denied" / "prompt", ya da
 * Permissions API yoksa "desteklenmiyor". Konum İSTEMEZ, izin penceresi açmaz.
 */
private suspend fun izinDurumu(): String = suspendCancellableCoroutine { cont ->
    runCatching {
        jsIzinDurumunuSor { durum -> if (cont.isActive) cont.resume(durum) }
    }.onFailure { if (cont.isActive) cont.resume("desteklenmiyor") }
}

private fun jsIzinDurumunuSor(sonuc: (String) -> Unit): Unit = js(
    """
    (function() {
        if (!navigator.permissions || !navigator.permissions.query) { sonuc('desteklenmiyor'); return; }
        navigator.permissions.query({ name: 'geolocation' }).then(
            function(d) { sonuc(d.state); },
            function() { sonuc('desteklenmiyor'); }
        );
    })()
    """,
)

actual suspend fun guncelKonumAl(): Konum {
    return try {
        val geo = jsNavigator().geolocation ?: return VARSAYILAN_KONUM
        // Android'deki gibi: izin VERİLMEMİŞSE konum hiç istenmez, varsayılana
        // düşülür. Böylece izin kararı henüz verilmemişken (KonumIzniEfekti'nin
        // güvenlik sınırı dolduğunda) ikinci bir izin penceresi açılıp yeniden
        // beklenmez. Permissions API yoksa eski davranış: konum istenir.
        when (izinDurumu()) {
            "granted", "desteklenmiyor" -> Unit
            else -> return VARSAYILAN_KONUM
        }
        // İzin kararı artık KonumIzniEfekti'nde (gerçek sonuç) bekleniyor; buraya
        // gelindiğinde izin çoğunlukla zaten verilmiş/reddedilmiştir. Bu sınır
        // yalnızca konumun hiç gelmediği durum için bir güvenlik ağıdır (eskiden
        // 5 sn'ydi ve izin penceresindeki kullanıcıyı bekleyemiyordu).
        withTimeoutOrNull(KONUM_GUVENLIK_SINIRI_MS) {
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
