package com.kuzeykapisi.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class KatalogOge(val kod: String, val ad: String)

@Serializable
data class KatalogKategori(val ad: String, val ogeler: List<KatalogOge>)

typealias Katalog = Map<String, KatalogKategori>
