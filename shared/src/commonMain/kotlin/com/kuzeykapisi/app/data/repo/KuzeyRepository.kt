package com.kuzeykapisi.app.data.repo

import com.kuzeykapisi.app.data.model.Katalog
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

    suspend fun rotaOlustur(enlem: Double, boylam: Double, mesaj: String): RotaYaniti =
        api.rotaOlustur(enlem, boylam, mesaj)

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
