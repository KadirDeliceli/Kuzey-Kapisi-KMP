package com.kuzeykapisi.app.domain

import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import kotlinx.serialization.Serializable

// @Serializable: Screen.SubMenu bu kartı taşıyor ve ekran yığını
// yapılandırma değişikliğinde JSON olarak saklanıyor (bkz. ui/nav/EkranYigini).
@Serializable
data class SubCard(val id: String, val ad: String, val kategori: String, val kapak: String)

@Serializable
enum class MainCardType { SUBMENU, DIRECT, ROTA_PLANLAYICI }

@Serializable
data class MainCard(
    val id: String,
    val ad: String,
    val altBaslik: String,
    val kapak: String,
    val type: MainCardType,
    val subs: List<SubCard> = emptyList(),
    /** Ana sayfadaki zig-zag satırında görselin yanında gösterilen kısa tanıtım cümlesi. */
    val aciklama: String = "",
)

// @Serializable: ekran yığını yapılandırma değişikliğinde (döndürme, karanlık
// mod) JSON olarak saklanıp aynı derinlikte geri kurulur (bkz. ui/nav/EkranYigini).
@Serializable
sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data class SubMenu(val mainCard: MainCard) : Screen
    @Serializable data class BotList(val kategori: String, val baslik: String) : Screen
    @Serializable data class Anlatim(val kaynak: AnlatimKaynagi, val baslik: String) : Screen
    @Serializable data class PersonaOnizleme(
        val kategori: String,
        val kod: String,
        val ad: String,
        val anlatimVar: Boolean,
    ) : Screen
    @Serializable data object Rota : Screen
    @Serializable data object AdminAnaSayfa : Screen
    @Serializable data object AdminPersonaEkle : Screen
    @Serializable data object AdminRotaYerEkle : Screen
    @Serializable data object AdminPersonaYonet : Screen
    @Serializable data class AdminPersonaDuzenle(val detay: PersonaDetay) : Screen
    @Serializable data object AdminRotaYerYonet : Screen
    @Serializable data class AdminRotaYerDuzenle(val mekan: RotaMekaniAdmin, val mevcutAnlatim: String?) : Screen
}

/** Admin token'ı gerektiren ekranlar — token yokken yığında tutulmaz. */
internal val Screen.adminEkrani: Boolean
    get() = when (this) {
        Screen.AdminAnaSayfa, Screen.AdminPersonaEkle, Screen.AdminRotaYerEkle,
        Screen.AdminPersonaYonet, Screen.AdminRotaYerYonet,
        is Screen.AdminPersonaDuzenle, is Screen.AdminRotaYerDuzenle -> true
        else -> false
    }

data class BotRef(val kategori: String, val kod: String)

val MAIN_CARDS = listOf(
    MainCard(
        "tarih-kultur", "Tarih ve Kültür", "Tarihî Keşif", "tarih-kultur", MainCardType.SUBMENU,
        listOf(
            SubCard("tarih", "Tarih", "kisiler", "tarih"),
            SubCard("kultur", "Kültür", "mekanlar", "kultur"),
        ),
        aciklama = "Osmanlı paşalarından yerel ustalara, Sinop'un tarihini ve kültürünü kendi tanıklarından dinleyin.",
    ),
    MainCard(
        "lezzet-doga", "Lezzet ve Doğa", "Tat & Tabiat", "lezzet-doga", MainCardType.SUBMENU,
        listOf(
            SubCard("lezzetler", "Lezzetler", "lezzetler", "lezzetler"),
            SubCard("doga", "Doğa", "doga", "doga"),
        ),
        aciklama = "Yöresel sofralardan kıyı ormanlarına, Karadeniz'in tadını ve dokusunu birlikte keşfedin.",
    ),
    MainCard(
        "akilli-rota", "Akıllı Zaman ve Rota Düzenleyici", "Planlayıcı", "akilli-rota",
        MainCardType.ROTA_PLANLAYICI,
        aciklama = "Gününüzü ve saatinizi baz alan akıllı bir rota, sizi doğru sırayla doğru yerlere götürsün.",
    ),
    MainCard(
        "tescil", "Tescilli Ürünler", "Sinop'un İmzası", "tescil", MainCardType.DIRECT,
        aciklama = "Coğrafi işaretli Sinop ürünlerinin hikâyesini, üreticisinin ağzından öğrenin.",
    ),
)
