package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun KonumIzniEfekti(onSonuc: (verildi: Boolean) -> Unit) {
    // Ayrı bir izin adımı yok — tarayıcı, guncelKonumAl() içindeki getCurrentPosition çağrısında
    // izin diyaloğunu kendisi gösterir ve cevabı bekler. Akış hemen devam eder.
    val guncelOnSonuc by rememberUpdatedState(onSonuc)
    LaunchedEffect(Unit) { guncelOnSonuc(true) }
}
