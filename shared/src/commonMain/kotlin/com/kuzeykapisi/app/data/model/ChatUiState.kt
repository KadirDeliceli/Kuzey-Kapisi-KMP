package com.kuzeykapisi.app.data.model

data class Mesaj(
    val metin: String,
    val benden: Boolean,
    val sistemNotu: Boolean = false,
)

data class ChatUiState(
    val baslik: String = "",
    val sessionId: String? = null,
    val mesajlar: List<Mesaj> = emptyList(),
    val yaziyor: Boolean = false,
    val yukleniyor: Boolean = true,
    /** Oturum HİÇ başlatılamadıysa gösterilen hata (sohbet yok, kalıcı). */
    val hata: String? = null,
    /**
     * Bir mesaj gönderilemediğinde gösterilen GEÇİCİ uyarı. Mesaj listesine
     * girmez (sohbet geçmişinde iz bırakmaz); yeni gönderim denemesinde,
     * deneme başarılı olunca ya da birkaç saniye sonra kendiliğinden temizlenir.
     */
    val agUyarisi: String? = null,
)
