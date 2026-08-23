@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.data.tts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// window.speechSynthesis native pause/resume destekler — konum kendisi tutulur.
actual class AnlatimOynatici actual constructor() {
    private val _durum = MutableStateFlow(AnlatimDurumu.DURDU)
    actual val durum: StateFlow<AnlatimDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    // cancel() sonrası eski utterance'ın gecikmiş "onend" olayı, yeni
    // başlatılan oynatmanın durumunu YANLIŞLIKLA DURDU'ya çekmesin diye her
    // oynat() çağrısına yeni bir jeton verilir; callback yalnızca hâlâ
    // GÜNCEL jetonsa durum günceller.
    private var etkinJeton = 0

    actual fun oynat(metin: String) {
        _hata.value = null
        etkinJeton++
        val jeton = etkinJeton
        runCatching {
            jsKonusBaslat(
                metin = metin,
                dil = "tr-TR",
                bitti = {
                    if (jeton == etkinJeton) _durum.value = AnlatimDurumu.DURDU
                },
                hataOldu = {
                    if (jeton == etkinJeton) {
                        _durum.value = AnlatimDurumu.DURDU
                        _hata.value = "Bu tarayıcıda seslendirme desteklenmiyor."
                    }
                },
            )
            _durum.value = AnlatimDurumu.OYNUYOR
        }.onFailure {
            _hata.value = "Bu tarayıcıda seslendirme desteklenmiyor."
        }
    }

    actual fun duraklat() {
        runCatching { jsKonusDuraklat() }
        if (_durum.value == AnlatimDurumu.OYNUYOR) {
            _durum.value = AnlatimDurumu.DURAKLATILDI
        }
    }

    actual fun devamEt() {
        runCatching { jsKonusDevamEt() }
        if (_durum.value == AnlatimDurumu.DURAKLATILDI) {
            _durum.value = AnlatimDurumu.OYNUYOR
        }
    }

    actual fun durdur() {
        etkinJeton++
        runCatching { jsKonusDurdur() }
        _durum.value = AnlatimDurumu.DURDU
    }

    actual fun serbestBirak() {
        durdur()
    }
}

private fun jsKonusBaslat(
    metin: String,
    dil: String,
    bitti: () -> Unit,
    hataOldu: () -> Unit,
) {
    js(
        """
        (function() {
            if (!window.speechSynthesis) { hataOldu(); return; }
            window.speechSynthesis.cancel();
            var utterance = new SpeechSynthesisUtterance(metin);
            utterance.lang = dil;
            utterance.onend = function() { bitti(); };
            utterance.onerror = function() { hataOldu(); };
            window.speechSynthesis.speak(utterance);
        })();
        """,
    )
}

private fun jsKonusDuraklat() {
    js(
        """
        (function() {
            if (window.speechSynthesis) window.speechSynthesis.pause();
        })();
        """,
    )
}

private fun jsKonusDevamEt() {
    js(
        """
        (function() {
            if (window.speechSynthesis) window.speechSynthesis.resume();
        })();
        """,
    )
}

private fun jsKonusDurdur() {
    js(
        """
        (function() {
            if (window.speechSynthesis) window.speechSynthesis.cancel();
        })();
        """,
    )
}
