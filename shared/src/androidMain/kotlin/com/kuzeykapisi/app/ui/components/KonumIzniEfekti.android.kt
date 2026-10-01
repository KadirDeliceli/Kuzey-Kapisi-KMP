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
actual fun KonumIzniEfekti(onSonuc: (verildi: Boolean) -> Unit) {
    val context = LocalContext.current
    val guncelOnSonuc by rememberUpdatedState(onSonuc)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { verildi -> guncelOnSonuc(verildi) }

    LaunchedEffect(Unit) {
        val izinli = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        // İzin zaten varsa sonuç hemen bildirilir; yoksa diyalog sonucu beklenir.
        if (izinli) guncelOnSonuc(true) else launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
