package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun MikrofonIzniEfekti(istekNo: Int, onSonuc: (Boolean) -> Unit) {
    // no-op — izin akışı SesKaydedici.ios.kt içinde AVAudioApplication ile yürütülür.
    LaunchedEffect(istekNo) {
        if (istekNo != 0) onSonuc(true)
    }
}
