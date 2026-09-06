package com.kuzeykapisi.app.data.ses

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Eski (legacy) Kotlin/JS hedefi — asıl "web" hedefi wasmJs'tir (bkz.
// SesKaydedici.wasmJs.kt / AnlatimOynatici.js.kt). Burada güvenli varsayılana düşülür.
actual class SesKaydedici actual constructor() {
    private val _durum = MutableStateFlow(KayitDurumu.BOSTA)
    actual val durum: StateFlow<KayitDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>("Bu platformda ses kaydı desteklenmiyor.")
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    actual fun kayidaBasla() {
        _hata.value = "Bu platformda ses kaydı desteklenmiyor."
    }

    actual suspend fun kayidiDurdurVeAl(): KaydedilenSes? = null
}
