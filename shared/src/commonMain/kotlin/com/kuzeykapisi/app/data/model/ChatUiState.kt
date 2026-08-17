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
    val hata: String? = null,
)
