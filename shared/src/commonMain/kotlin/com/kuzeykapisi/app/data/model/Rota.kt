package com.kuzeykapisi.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RotaIstek(
    val enlem: Double,
    val boylam: Double,
    @SerialName("sure_saat") val sureSaat: Int,
    val turler: List<String>,
)

@Serializable
data class RotaDurak(
    val sira: Int,
    val id: Int,
    val ad: String,
    val tur: String,
    val aciklama: String,
    val enlem: Double,
    val boylam: Double,
    @SerialName("onceki_noktadan_yol_dk") val oncekiNoktadanYolDk: Int,
    @SerialName("ziyaret_suresi_dk") val ziyaretSuresiDk: Int,
    @SerialName("varis_toplam_dk") val varisToplamDk: Int,
    @SerialName("google_maps_url") val googleMapsUrl: String,
    @SerialName("anlatim_var") val anlatimVar: Boolean = false,
)

@Serializable
data class RotaYaniti(
    @SerialName("sure_saat") val sureSaat: Int,
    @SerialName("toplam_sure_dk") val toplamSureDk: Int,
    @SerialName("kullanilan_sure_dk") val kullanilanSureDk: Int,
    @SerialName("tercih_kategorisi") val tercihKategorisi: List<String>,
    val rota: List<RotaDurak>,
    val ozet: String,
)

@Serializable
data class VarsayilanRotalarYaniti(val rotalar: List<RotaYaniti>)

@Serializable
data class KategoriBilgi(val ad: String, val aciklama: String)
