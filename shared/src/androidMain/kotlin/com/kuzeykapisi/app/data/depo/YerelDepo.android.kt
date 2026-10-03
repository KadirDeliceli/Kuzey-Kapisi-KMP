package com.kuzeykapisi.app.data.depo

import android.content.Context
import com.kuzeykapisi.app.platform.AndroidContextHolder

private const val TERCIH_DOSYASI = "kuzey_kapisi_yerel_depo"

actual class YerelDepo actual constructor() {
    private val tercihler = AndroidContextHolder.appContext
        .getSharedPreferences(TERCIH_DOSYASI, Context.MODE_PRIVATE)

    actual fun getBoolean(anahtar: String, varsayilan: Boolean): Boolean =
        tercihler.getBoolean(anahtar, varsayilan)

    actual fun setBoolean(anahtar: String, deger: Boolean) {
        tercihler.edit().putBoolean(anahtar, deger).apply()
    }
}
