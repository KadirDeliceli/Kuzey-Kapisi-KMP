@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.log

// Web'de derleme türü çalışma anında okunamaz; yerel geliştirme sunucusu
// (localhost / 127.0.0.1) debug sayılır, yayındaki alan adında hiçbir şey yazılmaz.
internal actual val hataAyiklamaModu: Boolean by lazy {
    val host = runCatching { jsHostAdi() }.getOrDefault("")
    host == "localhost" || host == "127.0.0.1"
}

internal actual fun platformaYaz(etiket: String, mesaj: String) {
    println("[$etiket] $mesaj")
}

private fun jsHostAdi(): String = js("window.location.hostname")
