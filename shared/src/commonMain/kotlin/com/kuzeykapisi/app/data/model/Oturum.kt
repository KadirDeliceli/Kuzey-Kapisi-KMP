package com.kuzeykapisi.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OturumBaslatIstek(val kategori: String, val oge: String)

@Serializable
data class OturumBaslatYaniti(
    @SerialName("session_id") val sessionId: String,
    val baslik: String,
    val karsilama: String,
)

@Serializable
data class SohbetIstek(@SerialName("session_id") val sessionId: String, val mesaj: String)

@Serializable
data class SohbetYaniti(@SerialName("session_id") val sessionId: String, val cevap: String)

@Serializable
data class OturumKapatIstek(@SerialName("session_id") val sessionId: String)
