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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.kuzeykapisi.app.Metinler
import platform.CoreGraphics.CGRectZero
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

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
                text = Metinler.ROTA_HARITASI_YUKLENEMIYOR,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    // Son yüklenen HTML (UIView.tag yalnızca Int tutar; bu yüzden remember).
    val sonYuklenen = remember { SonYuklenenHtml() }

    UIKitView(
        factory = {
            runCatching {
                // İlk yükleme update'te yapılır (factory'nin hemen ardından çağrılır).
                WKWebView(frame = CGRectZero, configuration = WKWebViewConfiguration())
            }.getOrElse {
                hata = true
                WKWebView(frame = CGRectZero, configuration = WKWebViewConfiguration())
            }
        },
        update = { webView ->
            // Yalnızca HTML gerçekten değiştiyse yeniden yükle (titreme yok,
            // kullanıcının yakınlaştırması korunur).
            if (sonYuklenen.html != html) {
                runCatching {
                    webView.loadHTMLString(html, baseURL = null)
                    sonYuklenen.html = html
                }.onFailure { hata = true }
            }
        },
        onRelease = { webView ->
            runCatching {
                webView.stopLoading()
                webView.navigationDelegate = null
                webView.UIDelegate = null
            }
            sonYuklenen.html = null
        },
        modifier = modifier,
    )
}

private class SonYuklenenHtml(var html: String? = null)
