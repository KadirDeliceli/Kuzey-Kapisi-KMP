@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.kuzeykapisi.app.data.location

import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

/** Konum isteğinin en fazla beklediği süre — Android/web'deki güvenlik ağıyla aynı fikir. */
private const val KONUM_GUVENLIK_SINIRI_MS = 5000L

/**
 * CLLocationManager ile gerçek cihaz konumu. İzin zaten verilmemişse (izin
 * isteme akışı [com.kuzeykapisi.app.ui.components.KonumIzniEfekti]'nde, BU
 * fonksiyondan ÖNCE tamamlanmış olmalı) hiç istek atılmaz, doğrudan
 * [VARSAYILAN_KONUM]'a düşülür — izin penceresi burada AÇILMAZ.
 */
actual suspend fun guncelKonumAl(): Konum {
    val yonetici = CLLocationManager()
    val durum = yonetici.authorizationStatus
    val izinli = durum == kCLAuthorizationStatusAuthorizedWhenInUse || durum == kCLAuthorizationStatusAuthorizedAlways
    if (!izinli) return VARSAYILAN_KONUM

    val konum = withTimeoutOrNull(KONUM_GUVENLIK_SINIRI_MS) {
        suspendCancellableCoroutine<Konum?> { cont ->
            val delege = KonumDelegesi(cont)
            yonetici.delegate = delege
            cont.invokeOnCancellation { yonetici.delegate = null }
            yonetici.requestLocation()
        }
    }
    return konum ?: VARSAYILAN_KONUM
}

private class KonumDelegesi(
    private val devam: CancellableContinuation<Konum?>,
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        if (!devam.isActive) return
        val konum = (didUpdateLocations.lastOrNull() as? CLLocation)?.let {
            it.coordinate.useContents { Konum(enlem = latitude, boylam = longitude) }
        }
        devam.resume(konum)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        if (devam.isActive) devam.resume(null)
    }
}
