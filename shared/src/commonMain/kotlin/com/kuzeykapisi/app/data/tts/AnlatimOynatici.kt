package com.kuzeykapisi.app.data.tts

import kotlinx.coroutines.flow.StateFlow

enum class AnlatimDurumu { DURDU, OYNUYOR, DURAKLATILDI }

/**
 * Metin-okuma (TTS) sarmalayıcısı — baştan oynatma + duraklatma/devam etme.
 * Platforma özel gerçek implementasyon androidMain (TextToSpeech — native
 * pause yok, konum simüle edilir), iosMain (AVSpeechSynthesizer — native
 * pause/resume) ve wasmJsMain (window.speechSynthesis — native pause/resume)
 * altında.
 */
expect class AnlatimOynatici() {
    /** BAŞTAN oynatır (yeni metin ya da "Baştan Başla"). */
    fun oynat(metin: String)

    /** Duraklatır, KONUMU KORUR (devamEt() ile resume edilebilir). */
    fun duraklat()

    /** Duraklatılan yerden devam eder. */
    fun devamEt()

    /** TAMAMEN durdurur, konumu SIFIRLAR — resume edilemez. Ekrandan çıkarken kullanılır. */
    fun durdur()

    /** Ses motoru kaynaklarını serbest bırakır — ekran kompozisyondan çıkarken çağrılır. */
    fun serbestBirak()

    val durum: StateFlow<AnlatimDurumu>

    /** TR ses desteklenmiyorsa ya da oynatma başarısız olursa kısa, kullanıcıya gösterilebilir mesaj. */
    val hata: StateFlow<String?>
}
