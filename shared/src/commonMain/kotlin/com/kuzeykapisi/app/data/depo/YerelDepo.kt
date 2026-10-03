package com.kuzeykapisi.app.data.depo

/**
 * Cihazda kalıcı, küçük bir anahtar-değer deposu — uygulama kapatılıp açılsa
 * (web'de sayfa yenilense) bile değerler korunur. Yalnızca "görüldü/kabul
 * edildi" gibi küçük bayraklar içindir; hassas veri (ör. admin token'ı)
 * buraya YAZILMAZ.
 *
 * androidMain: SharedPreferences, iosMain: NSUserDefaults, wasmJsMain:
 * window.localStorage. Depolama erişilemezse (ör. tarayıcıda site verisi
 * engelliyse) okuma [varsayilan]ı döndürür, yazma sessizce yok sayılır.
 */
expect class YerelDepo() {
    fun getBoolean(anahtar: String, varsayilan: Boolean): Boolean
    fun setBoolean(anahtar: String, deger: Boolean)
}

/** İlk açılıştaki Kullanım Koşulları'nın kabul edildiğini tutan bayrak. */
const val KULLANIM_KOSULLARI_ANAHTARI = "kullanim_kosullari_kabul_edildi"
