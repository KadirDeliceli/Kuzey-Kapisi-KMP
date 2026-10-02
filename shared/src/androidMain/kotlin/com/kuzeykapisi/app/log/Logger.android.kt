package com.kuzeykapisi.app.log

import android.content.pm.ApplicationInfo
import android.util.Log
import com.kuzeykapisi.app.platform.AndroidContextHolder

// Paylaşılan modül bir kütüphane olduğu için kendi BuildConfig.DEBUG'ı
// uygulamanın derleme türünü yansıtmaz; APK'nın "debuggable" bayrağı yansıtır.
// Context henüz set edilmediyse (çok erken çağrı) güvenli taraf: yazma.
internal actual val hataAyiklamaModu: Boolean
    get() = runCatching {
        (AndroidContextHolder.appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }.getOrDefault(false)

internal actual fun platformaYaz(etiket: String, mesaj: String) {
    Log.d(etiket, mesaj)
}
