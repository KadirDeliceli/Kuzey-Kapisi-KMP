package com.kuzeykapisi.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminGirisIstek(@SerialName("kullanici_adi") val kullaniciAdi: String, val sifre: String)

@Serializable
data class AdminGirisYaniti(val token: String)

@Serializable
data class PersonaEkleYaniti(val kod: String, val kategori: String, val ad: String, val durum: String)

@Serializable
data class RotaYerEkleIstek(
    val ad: String,
    val enlem: Double,
    val boylam: Double,
    @SerialName("sure_dk") val sureDk: Int,
    val aciklama: String,
)

@Serializable
data class RotaYerEkleYaniti(val id: Int, val durum: String)

/** Kategori kodu (backend'in beklediği) -> ekranda gösterilecek ad. */
val ADMIN_PERSONA_KATEGORILERI: List<Pair<String, String>> = listOf(
    "kisiler" to "Tarihi Kişilik",
    "mekanlar" to "Kültürel Mekan",
    "lezzetler" to "Yöresel Lezzet",
    "doga" to "Doğal Güzellik",
    "tescil" to "Tescilli Ürün",
)
