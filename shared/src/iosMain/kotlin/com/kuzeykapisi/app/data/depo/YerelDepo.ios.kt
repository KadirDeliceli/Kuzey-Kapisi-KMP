package com.kuzeykapisi.app.data.depo

import platform.Foundation.NSUserDefaults

actual class YerelDepo actual constructor() {
    private val tercihler = NSUserDefaults.standardUserDefaults

    // boolForKey hiç yazılmamış anahtar için de false döner; varsayılanın
    // anlamlı olması için önce anahtarın var olup olmadığına bakılır.
    actual fun getBoolean(anahtar: String, varsayilan: Boolean): Boolean =
        if (tercihler.objectForKey(anahtar) == null) varsayilan else tercihler.boolForKey(anahtar)

    actual fun setBoolean(anahtar: String, deger: Boolean) {
        tercihler.setBool(deger, forKey = anahtar)
    }
}
