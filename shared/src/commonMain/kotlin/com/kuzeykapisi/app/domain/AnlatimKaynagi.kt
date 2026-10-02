package com.kuzeykapisi.app.domain

import kotlinx.serialization.Serializable

/** Anlatım ekranının metni nereden çekeceğini belirten kaynak — persona (kategori+kod) ya da rota durağı (mekan id). */
@Serializable
sealed interface AnlatimKaynagi {
    @Serializable
    data class Persona(val kategori: String, val kod: String) : AnlatimKaynagi

    @Serializable
    data class RotaDuragi(val mekanId: Int) : AnlatimKaynagi
}
