package com.kuzeykapisi.app.data.repo

import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.model.KategoriBilgi
import com.kuzeykapisi.app.data.model.OturumBaslatYaniti
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.model.PersonaEkleYaniti
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.model.RotaYerEkleIstek
import com.kuzeykapisi.app.data.model.RotaYerEkleYaniti
import com.kuzeykapisi.app.data.model.RotaYeriDetay
import com.kuzeykapisi.app.data.model.SecilenResim
import com.kuzeykapisi.app.data.remote.ApiService
import com.kuzeykapisi.app.data.ses.KaydedilenSes
import com.kuzeykapisi.app.log.Logger
import io.ktor.client.plugins.ClientRequestException

data class SohbetSonuc(val cevap: String, val sessionId: String, val yenilendi: Boolean)

data class SesliSohbetSonuc(
    val kullaniciMetni: String,
    val cevap: String,
    val sessionId: String,
    val yenilendi: Boolean,
)

class KuzeyRepository(private val api: ApiService) {
    suspend fun katalog(): Katalog = api.katalog()

    suspend fun oturumBaslat(kategori: String, oge: String): OturumBaslatYaniti =
        api.oturumBaslat(kategori, oge)

    suspend fun oturumKapat(sessionId: String) = runCatching { api.oturumKapat(sessionId) }

    suspend fun rotaKategorileriGetir(): Map<String, KategoriBilgi> = api.rotaKategorileriGetir()

    suspend fun varsayilanRotalariGetir(enlem: Double, boylam: Double): List<RotaYaniti> =
        api.varsayilanRotalariGetir(enlem, boylam)

    suspend fun rotaOlustur(enlem: Double, boylam: Double, sureSaat: Int, turler: List<String>): RotaYaniti =
        api.rotaOlustur(enlem, boylam, sureSaat, turler)

    suspend fun adminGiris(kullaniciAdi: String, sifre: String): String =
        api.adminGiris(kullaniciAdi, sifre)

    suspend fun personaEkle(
        kategori: String,
        ad: String,
        kod: String,
        karsilama: String,
        icerik: String,
        anlatim: String?,
        gorsel: SecilenResim,
    ): PersonaEkleYaniti = api.personaEkle(kategori, ad, kod, karsilama, icerik, anlatim, gorsel)

    suspend fun personaGuncelle(
        kategori: String,
        kod: String,
        ad: String,
        karsilama: String,
        icerik: String,
        anlatim: String,
        anlatimKaldir: Boolean,
        gorsel: SecilenResim?,
    ) = api.personaGuncelle(kategori, kod, ad, karsilama, icerik, anlatim, anlatimKaldir, gorsel)

    suspend fun personaSil(kategori: String, kod: String) =
        api.personaSil(kategori, kod)

    suspend fun personaGetir(kategori: String, kod: String): PersonaDetay =
        api.personaGetir(kategori, kod)

    suspend fun rotaYerEkle(istek: RotaYerEkleIstek): RotaYerEkleYaniti =
        api.rotaYerEkle(istek)

    suspend fun rotaYerleriListele(): List<RotaMekaniAdmin> =
        api.rotaYerleriListele()

    suspend fun rotaYeriGuncelle(mekanId: Int, istek: RotaYerEkleIstek) =
        api.rotaYeriGuncelle(mekanId, istek)

    suspend fun rotaYeriSil(mekanId: Int) =
        api.rotaYeriSil(mekanId)

    suspend fun rotaYeriGetir(mekanId: Int): RotaYeriDetay =
        api.rotaYeriGetir(mekanId)

    /**
     * Anlatım metni; backend 404 dönerse (bu öge için anlatım GERÇEKTEN yok)
     * null. Ağ/sunucu hataları null'a ÇEVRİLMEZ, olduğu gibi fırlatılır —
     * böylece arayüz "anlatım yok" ile "yüklenemedi"yi ayırt edebilir.
     */
    suspend fun anlatimGetir(kategori: String, kod: String): String? =
        yoksaNull { api.anlatimGetir(kategori, kod).metin }

    /** [anlatimGetir] ile aynı sözleşme: 404 → null (bu durak için anlatım yok), diğer hatalar fırlatılır. */
    suspend fun rotaAnlatimGetir(mekanId: Int): String? =
        yoksaNull { api.rotaAnlatimGetir(mekanId).metin }

    private inline fun <T> yoksaNull(istek: () -> T): T? = try {
        istek()
    } catch (e: ClientRequestException) {
        if (e.response.status.value == 404) null else throw e
    }

    suspend fun guvenliSohbet(kategori: String, oge: String, sessionId: String, mesaj: String): SohbetSonuc {
        val r = oturumYenileyerekDene(kategori, oge, "guvenliSohbet", sessionId) { sid -> api.sohbet(sid, mesaj) }
        return SohbetSonuc(r.deger.cevap, r.sessionId, r.yenilendi)
    }

    /**
     * guvenliSohbet ile AYNI desen: 404'te (sohbet süresi dolmuşsa) sessizce
     * yeni oturum açıp AYNI ses baytlarıyla bir kez daha dener. 400/502
     * olduğu gibi yukarı fırlatılır — ChatViewModel duruma göre mesaj gösterir.
     */
    suspend fun guvenliSesliSohbet(
        kategori: String,
        oge: String,
        sessionId: String,
        ses: KaydedilenSes,
    ): SesliSohbetSonuc {
        val r = oturumYenileyerekDene(kategori, oge, "guvenliSesliSohbet", sessionId) { sid -> api.sesliSohbet(sid, ses) }
        return SesliSohbetSonuc(r.deger.kullaniciMetni, r.deger.cevap, r.sessionId, r.yenilendi)
    }

    /**
     * guvenliSohbet/guvenliSesliSohbet'in paylaştığı ORTAK desen: [cagir]
     * [sessionId] ile denenir; 404 alırsa (oturum süresi dolmuş) sessizce
     * yeni oturum açılıp AYNI çağrı yeni sessionId ile bir kez daha yapılır.
     * Başka bir HTTP hatası olduğu gibi yukarı fırlatılır.
     */
    private suspend fun <T> oturumYenileyerekDene(
        kategori: String,
        oge: String,
        etiket: String,
        sessionId: String,
        cagir: suspend (sessionId: String) -> T,
    ): OturumYenilemeSonucu<T> = try {
        OturumYenilemeSonucu(cagir(sessionId), sessionId, yenilendi = false)
    } catch (e: ClientRequestException) {
        if (e.response.status.value == 404) {
            Logger.d { "$etiket: oturum süresi dolmuş (404), yeniden başlatılıyor (kategori=$kategori, oge=$oge)" }
            val yeni = api.oturumBaslat(kategori, oge)
            OturumYenilemeSonucu(cagir(yeni.sessionId), yeni.sessionId, yenilendi = true)
        } else {
            throw e
        }
    }
}

private data class OturumYenilemeSonucu<T>(val deger: T, val sessionId: String, val yenilendi: Boolean)
