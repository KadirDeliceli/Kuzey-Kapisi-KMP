package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Rota haritası şu an yüklenemiyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    HtmlElementView(
        factory = {
            runCatching {
                (document.createElement("iframe") as HTMLIFrameElement).apply {
                    style.border = "none"
                    style.width = "100%"
                    style.height = "100%"
                    srcdoc = html
                }
            }.getOrElse {
                hata = true
                document.createElement("iframe") as HTMLIFrameElement
            }
        },
        modifier = modifier,
        update = { iframe ->
            runCatching { iframe.srcdoc = html }.onFailure { hata = true }
        },
    )
}
