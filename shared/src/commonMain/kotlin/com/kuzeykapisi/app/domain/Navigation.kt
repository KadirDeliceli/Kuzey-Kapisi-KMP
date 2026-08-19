package com.kuzeykapisi.app.domain

data class SubCard(val id: String, val ad: String, val kategori: String, val kapak: String)

enum class MainCardType { SUBMENU, WIP, DIRECT, ROTA_PLANLAYICI }

data class MainCard(
    val id: String,
    val ad: String,
    val altBaslik: String,
    val kapak: String,
    val type: MainCardType,
    val subs: List<SubCard> = emptyList(),
)

val MAIN_CARDS = listOf(
    MainCard("tarih-kultur", "Tarih ve Kültür", "Tarihî Keşif", "tarih-kultur", MainCardType.SUBMENU, listOf(
        SubCard("tarih", "Tarih", "kisiler", "tarih"),
        SubCard("kultur", "Kültür", "mekanlar", "kultur"),
    )),
    MainCard("lezzet-doga", "Lezzet ve Doğa", "Tat & Tabiat", "lezzet-doga", MainCardType.SUBMENU, listOf(
        SubCard("lezzetler", "Lezzetler", "lezzetler", "lezzetler"),
        SubCard("doga", "Doğa", "doga", "doga"),
    )),
    MainCard("akilli-rota", "Akıllı Zaman ve Rota Düzenleyici", "Planlayıcı", "akilli-rota", MainCardType.ROTA_PLANLAYICI),
    MainCard("tescil", "Tescilli Ürünler", "Sinop'un İmzası", "tescil", MainCardType.DIRECT),
)
