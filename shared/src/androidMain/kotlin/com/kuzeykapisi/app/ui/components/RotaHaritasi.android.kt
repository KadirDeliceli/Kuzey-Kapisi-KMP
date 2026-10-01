package com.kuzeykapisi.app.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.log.Logger

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun RotaHaritasiWebView(html: String, modifier: Modifier) {
    var yuklemeHatasi by remember { mutableStateOf(false) }

    if (yuklemeHatasi) {
        RotaHaritasiHataMesaji(modifier)
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            runCatching {
                WebView(context).apply {
                    // AndroidView, oluşturulan View'ın Compose tarafından ölçülen
                    // boyutu (300dp'lik kart) alması için MATCH_PARENT layoutParams
                    // bekler — belirtilmezse WebView varsayılan WRAP_CONTENT ile
                    // kendi içeriğine göre büyüyüp kutudan taşabilir.
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    // İlk yükleme burada DEĞİL, hemen ardından çağrılan update'te:
                    // eskiden ikisi de yüklüyordu ve harita açılışta iki kez çiziliyordu.
                }
            }.getOrElse {
                yuklemeHatasi = true
                WebView(context)
            }
        },
        update = { webView ->
            // Yalnızca HTML GERÇEKTEN değiştiyse yeniden yükle: aynı HTML ile
            // gelen yeniden kompozisyonlar (scroll, durum değişimi) haritayı
            // titretip kullanıcının yakınlaştırma/kaydırmasını sıfırlamasın.
            // Son yüklenen HTML, WebView'ın kendi tag'inde tutulur.
            if (webView.tag != html) {
                runCatching {
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                    webView.tag = html
                    Logger.d { "rota haritası HTML yüklendi" }
                }.onFailure { yuklemeHatasi = true }
            }
        },
        // Görünüm kompozisyondan çıkınca WebView ve renderer kaynakları
        // (JS motoru, karo indirmeleri) hemen bırakılır.
        onRelease = { webView ->
            runCatching {
                webView.stopLoading()
                webView.tag = null
                webView.destroy()
            }
            Logger.d { "rota haritası WebView yok edildi" }
        },
    )
}

@Composable
private fun RotaHaritasiHataMesaji(modifier: Modifier) {
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
}
