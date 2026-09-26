package com.kuzeykapisi.app.log

import kotlin.experimental.ExperimentalNativeApi

// Xcode Debug yapılandırması framework'ü debug binary olarak derler; Release
// yapılandırması optimize (release) binary üretir.
@OptIn(ExperimentalNativeApi::class)
internal actual val hataAyiklamaModu: Boolean = Platform.isDebugBinary

internal actual fun platformaYaz(etiket: String, mesaj: String) {
    println("[$etiket] $mesaj")
}
