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
import io.ktor.client.plugins.ClientRequestException

data class SohbetSonuc(val cevap: String, val sessionId: String, val yenilendi: Boolean)

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
        token: String,
        kategori: String,
        ad: String,
        kod: String,
        karsilama: String,
        icerik: String,
        anlatim: String?,
        gorsel: SecilenResim,
    ): PersonaEkleYaniti = api.personaEkle(token, kategori, ad, kod, karsilama, icerik, anlatim, gorsel)

    suspend fun personaGuncelle(
        token: String,
        kategori: String,
        kod: String,
        ad: String,
        karsilama: String,
        icerik: String,
        anlatim: String?,
        gorsel: SecilenResim?,
    ) = api.personaGuncelle(token, kategori, kod, ad, karsilama, icerik, anlatim, gorsel)

    suspend fun personaSil(token: String, kategori: String, kod: String) =
        api.personaSil(token, kategori, kod)

    suspend fun personaGetir(kategori: String, kod: String, token: String): PersonaDetay =
        api.personaGetir(token, kategori, kod)

    suspend fun rotaYerEkle(token: String, istek: RotaYerEkleIstek): RotaYerEkleYaniti =
        api.rotaYerEkle(token, istek)

    suspend fun rotaYerleriListele(token: String): List<RotaMekaniAdmin> =
        api.rotaYerleriListele(token)

    suspend fun rotaYeriGuncelle(token: String, mekanId: Int, istek: RotaYerEkleIstek) =
        api.rotaYeriGuncelle(token, mekanId, istek)

    suspend fun rotaYeriSil(token: String, mekanId: Int) =
        api.rotaYeriSil(token, mekanId)

    suspend fun rotaYeriGetir(mekanId: Int, token: String): RotaYeriDetay =
        api.rotaYeriGetir(token, mekanId)

    /** 404'te (backend'de bu öge için anlatım yoksa) anlaşılır bir hata fırlatır. */
    suspend fun anlatimGetir(kategori: String, kod: String): String {
        return try {
            api.anlatimGetir(kategori, kod).metin
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) {
                throw IllegalStateException("Bu içerik için anlatım bulunamadı.")
            } else {
                throw e
            }
        }
    }

    /** 404'te (backend'de bu durak için anlatım yoksa) anlaşılır bir hata fırlatır. */
    suspend fun rotaAnlatimGetir(mekanId: Int): String {
        return try {
            api.rotaAnlatimGetir(mekanId).metin
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) {
                throw IllegalStateException("Bu durak için anlatım bulunamadı.")
            } else {
                throw e
            }
        }
    }

    suspend fun guvenliSohbet(kategori: String, oge: String, sessionId: String, mesaj: String): SohbetSonuc {
        return try {
            val y = api.sohbet(sessionId, mesaj)
            SohbetSonuc(y.cevap, sessionId, yenilendi = false)
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) {
                println("[KuzeyKapisi] guvenliSohbet: sessionId=$sessionId için 404 alındı, oturum yeniden başlatılıyor (kategori=$kategori, oge=$oge)")
                val yeni = api.oturumBaslat(kategori, oge)
                println("[KuzeyKapisi] guvenliSohbet: yeni sessionId=${yeni.sessionId}")
                val y = api.sohbet(yeni.sessionId, mesaj)
                SohbetSonuc(y.cevap, yeni.sessionId, yenilendi = true)
            } else throw e
        }
    }
}
