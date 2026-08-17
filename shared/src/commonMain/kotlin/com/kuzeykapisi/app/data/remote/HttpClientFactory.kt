package com.kuzeykapisi.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun httpEngine(): HttpClientEngineFactory<*>

val apiJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun createHttpClient(): HttpClient = HttpClient(httpEngine()) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(apiJson)
    }
}
