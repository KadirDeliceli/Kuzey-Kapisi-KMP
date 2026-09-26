package com.kuzeykapisi.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpResponseValidator
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

fun createHttpClient(): HttpClient = HttpClient(httpEngine()) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            if (motorAgHatasiniErrorOlarakFirlatir && cause !is Exception) throw BaglantiHatasi(cause)
        }
    }
    install(ContentNegotiation) {
        json(apiJson)
    }
}
