package com.kuzeykapisi.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KatalogOge(
    val kod: String,
    val ad: String,
    @SerialName("anlatim_var") val anlatimVar: Boolean = false,
)

@Serializable
data class KatalogKategori(val ad: String, val ogeler: List<KatalogOge>)

typealias Katalog = Map<String, KatalogKategori>
