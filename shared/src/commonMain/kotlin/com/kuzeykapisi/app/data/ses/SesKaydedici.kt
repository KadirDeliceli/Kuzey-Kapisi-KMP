package com.kuzeykapisi.app.data.ses

import kotlinx.coroutines.flow.StateFlow

enum class KayitDurumu { BOSTA, KAYIT_YAPILIYOR, ISLENIYOR }

/** Kaydedilen ses — ham bayt + backend'e multipart olarak gönderilecek dosya adı/mime tipi. */
data class KaydedilenSes(val bytes: ByteArray, val dosyaAdi: String, val mimeTipi: String)

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

    /** İzin reddi ya da kayıt hatası olduğunda kullanıcıya gösterilebilir kısa mesaj. */
    val hata: StateFlow<String?>
}
