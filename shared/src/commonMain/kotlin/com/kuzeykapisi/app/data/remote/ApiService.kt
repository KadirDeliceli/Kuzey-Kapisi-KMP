package com.kuzeykapisi.app.data.remote

import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.data.model.AdminGirisIstek
import com.kuzeykapisi.app.data.model.AdminGirisYaniti
import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.model.OturumBaslatIstek
import com.kuzeykapisi.app.data.model.OturumBaslatYaniti
import com.kuzeykapisi.app.data.model.OturumKapatIstek
import com.kuzeykapisi.app.data.model.PersonaEkleYaniti
import com.kuzeykapisi.app.data.model.RotaIstek
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.model.RotaYerEkleIstek
import com.kuzeykapisi.app.data.model.RotaYerEkleYaniti
import com.kuzeykapisi.app.data.model.SecilenResim
import com.kuzeykapisi.app.data.model.SohbetIstek
import com.kuzeykapisi.app.data.model.SohbetYaniti
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
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

    suspend fun rotaOlustur(enlem: Double, boylam: Double, mesaj: String): RotaYaniti {
        val istek = RotaIstek(enlem, boylam, mesaj)
        println("[KuzeyKapisi] POST /rota/olustur istek gövdesi: ${apiJson.encodeToString(istek)}")
        val hamCevap = calVeHamMetniAl {
            client.post("${Config.BASE_URL}rota/olustur") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
        println("[KuzeyKapisi] POST /rota/olustur ham cevap: $hamCevap")
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun adminGiris(kullaniciAdi: String, sifre: String): String {
        val istek = AdminGirisIstek(kullaniciAdi, sifre)
        return try {
            val hamCevap = calVeHamMetniAl {
                client.post("${Config.BASE_URL}admin/giris") {
                    contentType(ContentType.Application.Json)
                    setBody(istek)
                }
            }
            apiJson.decodeFromString<AdminGirisYaniti>(hamCevap).token
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun personaEkle(
        token: String,
        kategori: String,
        ad: String,
        kod: String,
        karsilama: String,
        icerik: String,
        gorsel: SecilenResim,
    ): PersonaEkleYaniti {
        return try {
            val yanit = client.submitFormWithBinaryData(
                url = "${Config.BASE_URL}admin/persona-ekle",
                formData = formData {
                    append("kategori", kategori)
                    append("ad", ad)
                    append("kod", kod)
                    append("karsilama", karsilama)
                    append("icerik", icerik)
                    append(
                        "gorsel",
                        gorsel.bytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, mimeTipiIcin(gorsel.uzanti))
                            append(HttpHeaders.ContentDisposition, "filename=\"${gorsel.dosyaAdi}\"")
                        },
                    )
                },
            ) {
                header("X-Admin-Token", token)
            }
            apiJson.decodeFromString(yanit.bodyAsText())
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun rotaYerEkle(token: String, istek: RotaYerEkleIstek): RotaYerEkleYaniti {
        return try {
            val hamCevap = calVeHamMetniAl {
                client.post("${Config.BASE_URL}admin/rota-yer-ekle") {
                    contentType(ContentType.Application.Json)
                    header("X-Admin-Token", token)
                    setBody(istek)
                }
            }
            apiJson.decodeFromString(hamCevap)
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    private fun mimeTipiIcin(uzanti: String): String = when (uzanti.lowercase()) {
        ".png" -> "image/png"
        ".webp" -> "image/webp"
        ".jpg", ".jpeg" -> "image/jpeg"
        else -> "application/octet-stream"
    }

    /** Backend'in FastAPI HTTPException'ları hep {"detail": "..."} şeklinde döner. */
    private suspend fun adminHataFirlat(e: ClientRequestException): Nothing {
        val govde = runCatching { e.response.bodyAsText() }.getOrNull()
        val detay = govde
            ?.let { runCatching { apiJson.decodeFromString<HataYaniti>(it).detail }.getOrNull() }
            ?: "Bir hata oluştu, lütfen tekrar deneyin."
        throw AdminApiHatasi(e.response.status.value, detay)
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

@Serializable
private data class HataYaniti(val detail: String)
