package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun MikrofonIzniEfekti(istekNo: Int, onSonuc: (Boolean) -> Unit) {
    // no-op — bkz. SesKaydedici.js.kt (bu hedefte ses kaydı desteklenmiyor).
    LaunchedEffect(istekNo) {
        if (istekNo != 0) onSonuc(true)
    }
}
