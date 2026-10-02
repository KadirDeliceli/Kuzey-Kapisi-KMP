package com.kuzeykapisi.app.data.ses

import com.kuzeykapisi.app.Metinler
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

    // Bu bir izin sorunu değil, platform desteği eksikliğidir — izinDurumu
    // burada hiçbir zaman değişmez, yalnızca sözleşmeyi karşılamak için var.
    private val _izinDurumu = MutableStateFlow(MikrofonIzniDurumu.SORULMADI)
    actual val izinDurumu: StateFlow<MikrofonIzniDurumu> = _izinDurumu.asStateFlow()

    actual val ayarlarDestekleniyor: Boolean = false

    actual fun kayidaBasla() {
        _hata.value = Metinler.SES_KAYDI_DESTEKLENMIYOR
    }

    actual suspend fun kayidiDurdurVeAl(): KaydedilenSes? = null

    actual fun ayarlariAc() {
        // no-op — bu hedefte ses kaydı zaten desteklenmiyor.
    }

    actual fun serbestBirak() {
        // no-op — bu hedefte kurulan bir dinleyici/kaynak yok.
    }
}
