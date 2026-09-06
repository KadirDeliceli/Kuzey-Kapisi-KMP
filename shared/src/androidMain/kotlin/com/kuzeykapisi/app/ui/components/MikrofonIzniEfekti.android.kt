package com.kuzeykapisi.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun MikrofonIzniEfekti(istekNo: Int, onSonuc: (Boolean) -> Unit) {
    val context = LocalContext.current
    val guncelOnSonuc by rememberUpdatedState(onSonuc)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { izinVerildi -> guncelOnSonuc(izinVerildi) }

    LaunchedEffect(istekNo) {
        if (istekNo == 0) return@LaunchedEffect
        val izinli = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (izinli) guncelOnSonuc(true) else launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
}
