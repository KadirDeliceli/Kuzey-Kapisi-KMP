package com.kuzeykapisi.app.ui.theme

import android.provider.Settings
import com.kuzeykapisi.app.data.location.AndroidContextHolder

/** Mobilde glow yok — yalnızca basma geri bildirimi. */
actual val fenerHalesiDestekli: Boolean = false

/**
 * Sistem animasyon ölçeği 0 ise ("Animasyonları kapat" / erişilebilirlik),
 * hareket azaltılmış sayılır. Context henüz kurulmadıysa güvenli varsayılan
 * olarak false döner.
 */
actual val hareketAzaltilsin: Boolean
    get() = runCatching {
        Settings.Global.getFloat(
            AndroidContextHolder.appContext.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }.getOrDefault(false)
