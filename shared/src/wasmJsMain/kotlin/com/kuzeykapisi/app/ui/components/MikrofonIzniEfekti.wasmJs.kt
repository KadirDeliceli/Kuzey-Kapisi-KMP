package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun MikrofonIzniEfekti(istekNo: Int, onSonuc: (Boolean) -> Unit) {
    // no-op — tarayıcı, SesKaydedici.wasmJs.kt içindeki getUserMedia çağrısında
    // izin diyaloğunu kendisi gösterir.
    LaunchedEffect(istekNo) {
        if (istekNo != 0) onSonuc(true)
    }
}
