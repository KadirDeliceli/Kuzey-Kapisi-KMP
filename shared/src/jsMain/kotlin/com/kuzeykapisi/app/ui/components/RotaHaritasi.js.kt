package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Eski (legacy) Kotlin/JS hedefi — asıl "web" hedefi wasmJs'tir (bkz.
// RotaHaritasi.wasmJs.kt / CLAUDE.md §0). Burada nazik bir hata mesajına
// düşülür (ortak hata kutusu — bkz. commonMain/RotaHaritasi.kt).
@Composable
actual fun RotaHaritasiWebView(html: String, modifier: Modifier) {
    RotaHaritasiHataMesaji(modifier)
}
