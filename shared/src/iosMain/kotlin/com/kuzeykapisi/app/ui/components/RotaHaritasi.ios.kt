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
                text = "Rota haritası şu an yüklenemiyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    UIKitView(
        factory = {
            runCatching {
                WKWebView(frame = CGRectZero, configuration = WKWebViewConfiguration()).apply {
                    loadHTMLString(html, baseURL = null)
                }
            }.getOrElse {
                hata = true
                WKWebView(frame = CGRectZero, configuration = WKWebViewConfiguration())
            }
        },
        update = { webView ->
            runCatching { webView.loadHTMLString(html, baseURL = null) }.onFailure { hata = true }
        },
        modifier = modifier,
    )
}
