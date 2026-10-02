package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement

/**
 * Compose Multiplatform'un resmi web interop API'si (HtmlElementView) ile
 * bir <iframe> oluşturur. Bu API, konumlandırma/boyutlandırmayı VE canvas
 * dışına taşan kısımların kırpılmasını (clip-path) Compose'un kendi layout
 * sistemine göre HER layout değişiminde (scroll, resize, liste kayması)
 * otomatik senkronlar — elle window.scrollX/Y hesaplamaya gerek kalmaz ve
 * canvas'ın sayfadaki konumundan bağımsız çalışır.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun RotaHaritasiWebView(html: String, modifier: Modifier) {
    var hata by remember { mutableStateOf(false) }

    if (hata) {
        RotaHaritasiHataMesaji(modifier) // ortak hata kutusu — bkz. commonMain/RotaHaritasi.kt
        return
    }

    HtmlElementView(
        factory = {
            runCatching {
                (document.createElement("iframe") as HTMLIFrameElement).apply {
                    // Yalnızca betik çalıştırma izni: allow-same-origin YOK, yani
                    // harita belgesi opak bir origin'de çalışır — ana sayfanın
                    // DOM'una, çerezlerine/depolamasına ve Kotlin uygulamasına
                    // erişemez. Bu, srcdoc'tan ÖNCE ayarlanmalıdır.
                    setAttribute("sandbox", "allow-scripts")
                    style.border = "none"
                    style.width = "100%"
                    style.height = "100%"
                    // İlk yükleme update'te yapılır (factory'nin hemen ardından çağrılır).
                }
            }.getOrElse {
                hata = true
                (document.createElement("iframe") as HTMLIFrameElement).apply {
                    setAttribute("sandbox", "allow-scripts")
                }
            }
        },
        modifier = modifier,
        update = { iframe ->
            // srcdoc'a AYNI değeri yeniden atamak bile iframe'i baştan yükler
            // (harita titrer, yakınlaştırma sıfırlanır) — yalnızca değiştiyse ata.
            if (iframe.srcdoc != html) {
                runCatching { iframe.srcdoc = html }.onFailure { hata = true }
            }
        },
    )
}
