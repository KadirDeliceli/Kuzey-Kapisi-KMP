package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.Metinler

// Eski (legacy) Kotlin/JS hedefi — asıl "web" hedefi wasmJs'tir (bkz.
// RotaHaritasi.wasmJs.kt / CLAUDE.md §0). Burada nazik bir hata mesajına
// düşülür.
@Composable
actual fun RotaHaritasiWebView(html: String, modifier: Modifier) {
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
