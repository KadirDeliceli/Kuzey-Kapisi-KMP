package com.kuzeykapisi.app.data.model

data class SohbetSonuc(val cevap: String, val sessionId: String, val yenilendi: Boolean)

data class SesliSohbetSonuc(
    val kullaniciMetni: String,
    val cevap: String,
    val sessionId: String,
    val yenilendi: Boolean,
)
