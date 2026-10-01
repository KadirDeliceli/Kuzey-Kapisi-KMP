package com.kuzeykapisi.app.data.remote

import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.log.Logger
import com.kuzeykapisi.app.data.model.AdminGirisIstek
import com.kuzeykapisi.app.data.model.AdminGirisYaniti
import com.kuzeykapisi.app.data.model.AnlatimYaniti
import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.model.KategoriBilgi
import com.kuzeykapisi.app.data.model.OturumBaslatIstek
import com.kuzeykapisi.app.data.model.OturumBaslatYaniti
import com.kuzeykapisi.app.data.model.OturumKapatIstek
import com.kuzeykapisi.app.data.model.PersonaEkleYaniti
import com.kuzeykapisi.app.data.model.RotaIstek
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.model.RotaYerEkleIstek
import com.kuzeykapisi.app.data.model.RotaYerEkleYaniti
import com.kuzeykapisi.app.data.model.RotaYeriDetay
import com.kuzeykapisi.app.data.model.RotaYerleriYaniti
import com.kuzeykapisi.app.data.model.SecilenResim
import com.kuzeykapisi.app.data.model.SohbetIstek
import com.kuzeykapisi.app.data.model.SohbetYaniti
import com.kuzeykapisi.app.data.model.VarsayilanRotalarYaniti
import com.kuzeykapisi.app.data.model.VoiceChatYaniti
import com.kuzeykapisi.app.data.ses.KaydedilenSes
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class ApiService(private val client: HttpClient) {

    suspend fun katalog(): Katalog {
        Logger.d { "GET /katalog isteği gönderiliyor" }
        val hamCevap = calVeHamMetniAl { client.get("${Config.BASE_URL}katalog") }
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun oturumBaslat(kategori: String, oge: String): OturumBaslatYaniti {
        val istek = OturumBaslatIstek(kategori, oge)
        val hamCevap = calVeHamMetniAl {
            client.post("${Config.BASE_URL}oturum/baslat") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun sohbet(sessionId: String, mesaj: String): SohbetYaniti {
        val istek = SohbetIstek(sessionId, mesaj)
        val hamCevap = calVeHamMetniAl {
            client.post("${Config.BASE_URL}sohbet") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
        return apiJson.decodeFromString(hamCevap)
    }

    /**
     * multipart/form-data POST /voice-chat. 400/404/502'de ResponseException
     * fırlatır (expectSuccess=true) — repo katmanı 404'ü yakalayıp yeniden
     * dener, 400/502 ise ChatViewModel'e kadar yükselir.
     */
    suspend fun sesliSohbet(sessionId: String, ses: KaydedilenSes): VoiceChatYaniti {
        Logger.d { "POST /voice-chat isteği gönderiliyor" }
        val yanit = client.submitFormWithBinaryData(
            url = "${Config.BASE_URL}voice-chat",
            formData = formData {
                append("session_id", sessionId)
                append(
                    "ses",
                    ses.bytes,
                    Headers.build {
                        append(HttpHeaders.ContentType, ses.mimeTipi)
                        append(HttpHeaders.ContentDisposition, "filename=\"${ses.dosyaAdi}\"")
                    },
                )
            },
        )
        val hamCevap = yanit.bodyAsText()
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun oturumKapat(sessionId: String) {
        val istek = OturumKapatIstek(sessionId)
        calVeHamMetniAl {
            client.post("${Config.BASE_URL}oturum/kapat") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
    }

    suspend fun rotaKategorileriGetir(): Map<String, KategoriBilgi> {
        Logger.d { "GET /rota/kategoriler isteği gönderiliyor" }
        val hamCevap = calVeHamMetniAl { client.get("${Config.BASE_URL}rota/kategoriler") }
        return apiJson.decodeFromString(hamCevap)
    }

    suspend fun varsayilanRotalariGetir(enlem: Double, boylam: Double): List<RotaYaniti> {
        Logger.d { "GET /rota/varsayilanlar isteği gönderiliyor" }
        val hamCevap = calVeHamMetniAl {
            client.get("${Config.BASE_URL}rota/varsayilanlar") {
                parameter("enlem", enlem)
                parameter("boylam", boylam)
            }
        }
        return apiJson.decodeFromString<VarsayilanRotalarYaniti>(hamCevap).rotalar
    }

    suspend fun rotaOlustur(enlem: Double, boylam: Double, sureSaat: Int, turler: List<String>): RotaYaniti {
        val istek = RotaIstek(enlem, boylam, sureSaat, turler)
        val hamCevap = calVeHamMetniAl {
            client.post("${Config.BASE_URL}rota/olustur") {
                contentType(ContentType.Application.Json)
                setBody(istek)
            }
        }
        return apiJson.decodeFromString(hamCevap)
    }

    /** 404'te (backend'de anlatım yoksa) ClientRequestException fırlatır — repo katmanı bunu yakalar. */
    suspend fun anlatimGetir(kategori: String, kod: String): AnlatimYaniti {
        Logger.d { "GET /anlatim/$kategori/$kod isteği gönderiliyor" }
        val hamCevap = calVeHamMetniAl { client.get("${Config.BASE_URL}anlatim/$kategori/$kod") }
        return apiJson.decodeFromString(hamCevap)
    }

    /** 404'te (backend'de bu durak için anlatım yoksa) ClientRequestException fırlatır — repo katmanı bunu yakalar. */
    suspend fun rotaAnlatimGetir(mekanId: Int): AnlatimYaniti {
        Logger.d { "GET /rota-anlatim/$mekanId isteği gönderiliyor" }
        val hamCevap = calVeHamMetniAl { client.get("${Config.BASE_URL}rota-anlatim/$mekanId") }
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
        kategori: String,
        ad: String,
        kod: String,
        karsilama: String,
        icerik: String,
        anlatim: String?,
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
                    if (!anlatim.isNullOrBlank()) append("anlatim", anlatim)
                    append(
                        "gorsel",
                        gorsel.bytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, mimeTipiIcin(gorsel.uzanti))
                            append(HttpHeaders.ContentDisposition, "filename=\"${gorsel.dosyaAdi}\"")
                        },
                    )
                },
            )
            apiJson.decodeFromString(yanit.bodyAsText())
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    /**
     * gorsel null ise multipart'a hiç eklenmez — backend bunu "mevcut görsele
     * dokunma" olarak yorumluyor.
     */
    suspend fun personaGuncelle(
        kategori: String,
        kod: String,
        ad: String,
        karsilama: String,
        icerik: String,
        anlatim: String,
        anlatimKaldir: Boolean,
        gorsel: SecilenResim?,
    ) {
        try {
            val parcalar = formData {
                append("ad", ad)
                append("karsilama", karsilama)
                append("icerik", icerik)
                // Backend artık ikisini de HER İSTEKTE bekliyor — boş string'e
                // güvenilmiyor, "kaldır" niyeti ayrı bir alanla taşınıyor.
                append("anlatim", anlatim)
                append("anlatim_kaldir", if (anlatimKaldir) "true" else "false")
                if (gorsel != null) {
                    append(
                        "gorsel",
                        gorsel.bytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, mimeTipiIcin(gorsel.uzanti))
                            append(HttpHeaders.ContentDisposition, "filename=\"${gorsel.dosyaAdi}\"")
                        },
                    )
                }
            }
            client.submitFormWithBinaryData(
                url = "${Config.BASE_URL}admin/persona-guncelle/$kategori/$kod",
                formData = parcalar,
            ) {
                method = HttpMethod.Put
            }
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun personaGetir(kategori: String, kod: String): PersonaDetay {
        return try {
            val hamCevap = calVeHamMetniAl {
                client.get("${Config.BASE_URL}admin/persona/$kategori/$kod")
            }
            apiJson.decodeFromString(hamCevap)
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun personaSil(kategori: String, kod: String) {
        try {
            client.delete("${Config.BASE_URL}admin/persona-sil/$kategori/$kod")
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun rotaYerEkle(istek: RotaYerEkleIstek): RotaYerEkleYaniti {
        return try {
            val hamCevap = calVeHamMetniAl {
                client.post("${Config.BASE_URL}admin/rota-yer-ekle") {
                    contentType(ContentType.Application.Json)
                    setBody(istek)
                }
            }
            apiJson.decodeFromString(hamCevap)
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun rotaYerleriListele(): List<RotaMekaniAdmin> {
        return try {
            val hamCevap = calVeHamMetniAl {
                client.get("${Config.BASE_URL}admin/rota-yerleri")
            }
            apiJson.decodeFromString<RotaYerleriYaniti>(hamCevap).mekanlar
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun rotaYeriGetir(mekanId: Int): RotaYeriDetay {
        return try {
            val hamCevap = calVeHamMetniAl {
                client.get("${Config.BASE_URL}admin/rota-yeri/$mekanId")
            }
            apiJson.decodeFromString(hamCevap)
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    /** anlatim null ise gövdeye hiç eklenmez (encodeDefaults=false) — backend bunu "mevcut anlatıma dokunma" olarak yorumluyor. */
    suspend fun rotaYeriGuncelle(mekanId: Int, istek: RotaYerEkleIstek) {
        try {
            calVeHamMetniAl {
                client.put("${Config.BASE_URL}admin/rota-yer-guncelle/$mekanId") {
                    contentType(ContentType.Application.Json)
                    setBody(istek)
                }
            }
        } catch (e: ClientRequestException) {
            adminHataFirlat(e)
        }
    }

    suspend fun rotaYeriSil(mekanId: Int) {
        try {
            client.delete("${Config.BASE_URL}admin/rota-yer-sil/$mekanId")
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

    /**
     * Backend'in FastAPI HTTPException'ları hep {"detail": "..."} şeklinde döner.
     * detail tek bir metin değilse (ör. 422 doğrulama listesi) null bırakılır;
     * kullanıcıya gösterilecek metni Metinler.adminHataMesaji seçer.
     */
    private suspend fun adminHataFirlat(e: ClientRequestException): Nothing {
        val govde = runCatching { e.response.bodyAsText() }.getOrNull()
        val detay = govde
            ?.let { runCatching { apiJson.decodeFromString<HataYaniti>(it).detail }.getOrNull() }
            ?.takeIf { it.isNotBlank() }
        throw AdminApiHatasi(e.response.status.value, detay)
    }

    /**
     * İsteği yapar ve HAM (decode edilmemiş) yanıt metnini döner.
     * expectSuccess=true olduğu için 2xx dışı durumlarda çağrı burada
     * ResponseException fırlatır; o durumda yalnızca HTTP kodunu loglayıp
     * (gövde ASLA loglanmaz) yeniden fırlatıyoruz — asla hata gövdesini başarı tipiyle decode
     * etmeye ÇALIŞMIYORUZ.
     */
    private suspend inline fun calVeHamMetniAl(
        crossinline istek: suspend () -> io.ktor.client.statement.HttpResponse,
    ): String {
        return try {
            val yanit = istek()
            yanit.bodyAsText()
        } catch (e: ResponseException) {
            Logger.d { "İstek başarısız — HTTP ${e.response.status.value}" }
            throw e
        }
    }
}

@Serializable
private data class HataYaniti(val detail: String)
