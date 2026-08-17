package com.kuzeykapisi.app.data.remote

import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.model.OturumBaslatIstek
import com.kuzeykapisi.app.data.model.OturumBaslatYaniti
import com.kuzeykapisi.app.data.model.OturumKapatIstek
import com.kuzeykapisi.app.data.model.SohbetIstek
import com.kuzeykapisi.app.data.model.SohbetYaniti
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString

class ApiService(private val client: HttpClient = createHttpClient()) {

    suspend fun katalog(): Katalog {
        println("[KuzeyKapisi] GET /katalog isteği gönderiliyor")
        val hamCevap = calVeHamMetniAl { client.get("${Config.BASE_URL}katalog") }
        println("[KuzeyKapisi] GET /katalog ham cevap: $hamCevap")
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun oturumBaslat(kategori: String, oge: String): OturumBaslatYaniti {
        val istek = OturumBaslatIstek(kategori, oge)
        println("[KuzeyKapisi] POST /oturum/baslat istek gövdesi: ${apiJson.encodeToString(istek)}")
        val hamCevap = calVeHamMetniAl {
            client.post("${Config.BASE_URL}oturum/baslat") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
        println("[KuzeyKapisi] POST /oturum/baslat ham cevap: $hamCevap")
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun sohbet(sessionId: String, mesaj: String): SohbetYaniti {
        val istek = SohbetIstek(sessionId, mesaj)
        println("[KuzeyKapisi] POST /sohbet istek gövdesi: ${apiJson.encodeToString(istek)}")
        val hamCevap = calVeHamMetniAl {
            client.post("${Config.BASE_URL}sohbet") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
        println("[KuzeyKapisi] POST /sohbet ham cevap: $hamCevap")
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun oturumKapat(sessionId: String) {
        val istek = OturumKapatIstek(sessionId)
        println("[KuzeyKapisi] POST /oturum/kapat istek gövdesi: ${apiJson.encodeToString(istek)}")
        val hamCevap = calVeHamMetniAl {
            client.post("${Config.BASE_URL}oturum/kapat") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
        println("[KuzeyKapisi] POST /oturum/kapat ham cevap: $hamCevap")
    }

    /**
     * İsteği yapar ve HAM (decode edilmemiş) yanıt metnini döner.
     * expectSuccess=true olduğu için 2xx dışı durumlarda çağrı burada
     * ResponseException fırlatır; o durumda hata gövdesini loglayıp
     * yeniden fırlatıyoruz — asla hata gövdesini başarı tipiyle decode
     * etmeye ÇALIŞMIYORUZ.
     */
    private suspend inline fun calVeHamMetniAl(
        crossinline istek: suspend () -> io.ktor.client.statement.HttpResponse,
    ): String {
        return try {
            val yanit = istek()
            yanit.bodyAsText()
        } catch (e: ResponseException) {
            val hataGovdesi = runCatching { e.response.bodyAsText() }.getOrNull()
            println(
                "[KuzeyKapisi] İstek başarısız — HTTP ${e.response.status.value}, " +
                    "hata gövdesi: $hataGovdesi",
            )
            throw e
        }
    }
}
