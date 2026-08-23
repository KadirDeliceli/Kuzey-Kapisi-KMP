package com.kuzeykapisi.app.data.tts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Eski (legacy) Kotlin/JS hedefi — asıl "web" hedefi wasmJs'tir (bkz.
// AnlatimOynatici.wasmJs.kt / CLAUDE.md §0). Burada güvenli varsayılana düşülür.
actual class AnlatimOynatici actual constructor() {
    private val _durum = MutableStateFlow(AnlatimDurumu.DURDU)
    actual val durum: StateFlow<AnlatimDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>("Bu platformda seslendirme desteklenmiyor.")
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    actual fun oynat(metin: String) {
        _hata.value = "Bu platformda seslendirme desteklenmiyor."
    }

    actual fun duraklat() {}

    actual fun devamEt() {}

    actual fun durdur() {
        _durum.value = AnlatimDurumu.DURDU
    }

    actual fun serbestBirak() {}
}
