package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject

/**
 * CLLocationManager.requestWhenInUseAuthorization() ile gerçek sistem izin
 * penceresini açar — Android'deki ActivityResultContracts.RequestPermission
 * ile AYNI sözleşme: izin zaten varsa [onSonuc] hemen çağrılır, yoksa
 * kullanıcının kararı (delege geri çağrısı) beklenir.
 */
@Composable
actual fun KonumIzniEfekti(onSonuc: (verildi: Boolean) -> Unit) {
    val guncelOnSonuc by rememberUpdatedState(onSonuc)
    DisposableEffect(Unit) {
        var bildirildi = false
        fun bildir(izinli: Boolean) {
            if (bildirildi) return
            bildirildi = true
            guncelOnSonuc(izinli)
        }

        val yonetici = CLLocationManager()
        val delege = object : NSObject(), CLLocationManagerDelegateProtocol {
            // Eski (iOS 4+) delege yöntemi — yeni locationManagerDidChangeAuthorization
            // yalnızca iOS 14+'ta var; bu, tüm sürümlerde çağrılır (deprecated olsa da).
            override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
                when (didChangeAuthorizationStatus) {
                    kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> bildir(true)
                    kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> bildir(false)
                    // kCLAuthorizationStatusNotDetermined: kullanıcı henüz karar vermedi.
                    else -> Unit
                }
            }
        }
        yonetici.delegate = delege

        when (yonetici.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> bildir(true)
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> bildir(false)
            else -> yonetici.requestWhenInUseAuthorization()
        }

        onDispose { yonetici.delegate = null }
    }
}
