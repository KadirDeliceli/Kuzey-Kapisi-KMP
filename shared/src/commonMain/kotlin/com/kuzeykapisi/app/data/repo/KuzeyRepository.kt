package com.kuzeykapisi.app.data.repo

import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.model.KategoriBilgi
import com.kuzeykapisi.app.data.model.OturumBaslatYaniti
import com.kuzeykapisi.app.data.model.PersonaEkleYaniti
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.model.RotaYerEkleIstek
import com.kuzeykapisi.app.data.model.RotaYerEkleYaniti
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
        gorsel: SecilenResim,
    ): PersonaEkleYaniti = api.personaEkle(token, kategori, ad, kod, karsilama, icerik, gorsel)

    suspend fun rotaYerEkle(token: String, istek: RotaYerEkleIstek): RotaYerEkleYaniti =
        api.rotaYerEkle(token, istek)

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
