package com.kuzeykapisi.app.data.media

/** Admin panelinde kullanıcının seçtiği görsel: ham bayt + dosya adı/uzantısı. */
data class SecilenResim(val bytes: ByteArray, val dosyaAdi: String, val uzanti: String) {
    // Kotlin'in otomatik ürettiği equals/hashCode, ByteArray'i İÇERİK değil
    // REFERANS olarak karşılaştırır — aynı baytları taşıyan iki SecilenResim
    // "eşit değil" görünür (ör. Compose recomposition atlama mantığını
    // yanıltır). contentEquals/contentHashCode ile elle düzeltilir.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SecilenResim) return false
        return bytes.contentEquals(other.bytes) && dosyaAdi == other.dosyaAdi && uzanti == other.uzanti
    }

    override fun hashCode(): Int {
        var sonuc = bytes.contentHashCode()
        sonuc = 31 * sonuc + dosyaAdi.hashCode()
        sonuc = 31 * sonuc + uzanti.hashCode()
        return sonuc
    }
}

/**
 * Platforma özel görsel seçici. Kullanıcı seçimi iptal ederse ya da bir hata
 * oluşursa null döner — çağıran taraf bunu sessizce ele alır, uygulama
 * çökmez. androidMain: ActivityResultContracts (GetContent). wasmJsMain:
 * gizli HTML <input type=file> + FileReader. iOS ve eski (legacy) js
 * hedeflerinde şimdilik güvenli varsayılana (null) düşülür.
 */
expect suspend fun resimSec(): SecilenResim?
