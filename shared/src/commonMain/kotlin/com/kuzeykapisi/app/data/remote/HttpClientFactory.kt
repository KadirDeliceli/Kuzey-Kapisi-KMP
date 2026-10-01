package com.kuzeykapisi.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun httpEngine(): HttpClientEngineFactory<*>

/**
 * true: bu platformun HTTP motoru bağlantı hatasını Exception olarak DEĞİL,
 * kotlin.Error olarak fırlatır (Js motoru, başarısız fetch için
 * `Error("Fail to fetch")`). ViewModel'ler Exception yakaladığı için bu
 * dönüştürülmezse hata yakalanmaz, coroutine ölür, ekran sonsuza dek
 * "yükleniyor"da kalır. Android/iOS'ta false: oradaki Error'lar (bellek
 * yetersizliği gibi) gerçek hatadır, ağ hatası gibi gösterilmez.
 */
internal expect val motorAgHatasiniErrorOlarakFirlatir: Boolean

val apiJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * [adminOturumu]: admin token'ının tek kaynağı. /admin/... isteklerine token'ı
 * [adminTokenEklentisi] ekler; bir admin isteğine 401 dönerse oturum burada,
 * merkezi olarak kapatılır (ekranlar ayrıca bir şey yapmak zorunda değildir).
 */
fun createHttpClient(adminOturumu: AdminOturumu): HttpClient = HttpClient(httpEngine()) {
    expectSuccess = true
    install(adminTokenEklentisi(adminOturumu))
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, istek ->
            if (cause is ResponseException &&
                cause.response.status.value == 401 &&
                tokenGerektirenAdminYolu(istek.url.encodedPath)
            ) {
                adminOturumu.sonlandir(OturumSonlanmaNedeni.YETKISIZ)
            }
            if (motorAgHatasiniErrorOlarakFirlatir && cause !is Exception) throw BaglantiHatasi(cause)
        }
    }
    install(ContentNegotiation) {
        json(apiJson)
    }
}
