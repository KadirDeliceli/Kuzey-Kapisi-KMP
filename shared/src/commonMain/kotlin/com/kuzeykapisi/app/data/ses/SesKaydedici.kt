package com.kuzeykapisi.app.data.ses

import kotlinx.coroutines.flow.StateFlow

enum class KayitDurumu { BOSTA, KAYIT_YAPILIYOR, ISLENIYOR }

/**
 * Mikrofon izninin üç hâli — yalnızca "reddedildi" demek yetmiyor, kullanıcıya
 * NE YAPMASI gerektiğini söylemek için hangi reddediş olduğunu bilmek gerekiyor:
 *  - SORULMADI: henüz hiç sorulmadı, normal akışta platformun izin diyaloğu çıkar.
 *  - REDDEDILDI: bir kez reddedildi ama platform tekrar sorabilir (yalnızca Android'de
 *    anlamlı bir ARA durum — iOS ve tarayıcı bir reddedilişte doğrudan KALICI'ya geçer).
 *  - KALICI_REDDEDILDI: platform/tarayıcı bir daha KENDİLİĞİNDEN izin penceresi
 *    göstermeyecek — kullanıcı ayarlardan elle açmalı.
 */
enum class MikrofonIzniDurumu { SORULMADI, REDDEDILDI, KALICI_REDDEDILDI }

/** Kaydedilen ses — ham bayt + backend'e multipart olarak gönderilecek dosya adı/mime tipi. */
data class KaydedilenSes(val bytes: ByteArray, val dosyaAdi: String, val mimeTipi: String) {
    // Kotlin'in otomatik ürettiği equals/hashCode, ByteArray'i İÇERİK değil
    // REFERANS olarak karşılaştırır — aynı baytları taşıyan iki KaydedilenSes
    // "eşit değil" görünür (ör. Compose recomposition atlama mantığını
    // yanıltır). contentEquals/contentHashCode ile elle düzeltilir.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KaydedilenSes) return false
        return bytes.contentEquals(other.bytes) && dosyaAdi == other.dosyaAdi && mimeTipi == other.mimeTipi
    }

    override fun hashCode(): Int {
        var sonuc = bytes.contentHashCode()
        sonuc = 31 * sonuc + dosyaAdi.hashCode()
        sonuc = 31 * sonuc + mimeTipi.hashCode()
        return sonuc
    }
}

/**
 * Mikrofon ses kaydı sarmalayıcısı — platforma özel gerçek implementasyon
 * androidMain (MediaRecorder), iosMain (AVAudioRecorder) ve wasmJsMain
 * (MediaRecorder/getUserMedia) altında. Eski (legacy) jsMain hedefinde
 * güvenli varsayılana (kayıt desteklenmiyor) düşülür — asıl web hedefi
 * wasmJs'tir.
 *
 * İzin reddi ya da herhangi bir kayıt hatası ASLA çökmeye yol açmaz: durum
 * BOSTA'da kalır/kalır hâle döner ve [hata] kullanıcıya gösterilebilir kısa
 * bir mesajla doldurulur.
 */
expect class SesKaydedici() {
    /** Kayda başlar (durum BOSTA ise). İzin akışı asenkron olabilir; başarılı olursa durum KAYIT_YAPILIYOR'a döner. */
    fun kayidaBasla()

    /**
     * Kaydı durdurur ve sonucu döner. Çağrıldığı an durum ISLENIYOR'a geçer,
     * işlem bitince BOSTA'ya döner. İzin yoksa/hata varsa ya da kayıt zaten
     * sürmüyorsa null döner.
     */
    suspend fun kayidiDurdurVeAl(): KaydedilenSes?

    val durum: StateFlow<KayitDurumu>

    /** İzin reddi ya da kayıt hatası olduğunda kullanıcıya gösterilebilir, platforma göre biçimlenmiş kısa mesaj. */
    val hata: StateFlow<String?>

    /** Mikrofon izninin güncel hâli — bkz. [MikrofonIzniDurumu]. */
    val izinDurumu: StateFlow<MikrofonIzniDurumu>

    /**
     * KALICI_REDDEDILDI durumunda kullanıcıyı platformun ayarlar sayfasına
     * yönlendirir (Android: uygulama detay ayarları, iOS: Ayarlar uygulaması).
     * Yalnızca [ayarlarDestekleniyor] true iken bir şey yapar; web'de no-op'tur
     * (tarayıcıdan ayarlara deep-link yapılamaz — bkz. wasmJs implementasyonu).
     */
    fun ayarlariAc()

    /** true ise [ayarlariAc] gerçekten bir ayarlar sayfası açar (Android/iOS); web'de false. */
    val ayarlarDestekleniyor: Boolean

    /**
     * Bu örneğin kurduğu platforma özel kaynakları/dinleyicileri serbest
     * bırakır — ekran kapsamı temizlenince (bkz. ChatViewModel.onCleared)
     * çağrılır. web'de izin durumu dinleyicisini devre dışı bırakır (yoksa
     * her sohbet açılışında yeni bir dinleyici birikir); Android/iOS'ta
     * no-op'tur (saklanan bir kaynak yok).
     */
    fun serbestBirak()
}
