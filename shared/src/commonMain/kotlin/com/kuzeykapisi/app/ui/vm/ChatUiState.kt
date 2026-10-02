package com.kuzeykapisi.app.ui.vm

/**
 * [id]: listedeki KONUMDAN bağımsız, kalıcı kimlik (bkz. ChatViewModel'in
 * artan sayacı) — ileride mesaj silme/ekleme gelirse ses oynatma eşleştirmesi
 * (oynatilanMesajId) yanlış mesajı çalmasın diye indeks YERİNE bu kullanılır.
 */
data class Mesaj(
    val id: Long,
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
