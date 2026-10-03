package com.kuzeykapisi.app.data.depo

import kotlinx.browser.window

// localStorage, site verisi engellendiğinde ya da bazı gizli pencerelerde
// erişimde istisna fırlatabilir; bu durumda depo "boş" gibi davranır.
actual class YerelDepo actual constructor() {
    actual fun getBoolean(anahtar: String, varsayilan: Boolean): Boolean =
        runCatching { window.localStorage.getItem(anahtar) }.getOrNull()
            ?.toBooleanStrictOrNull()
            ?: varsayilan

    actual fun setBoolean(anahtar: String, deger: Boolean) {
        runCatching { window.localStorage.setItem(anahtar, deger.toString()) }
    }
}
