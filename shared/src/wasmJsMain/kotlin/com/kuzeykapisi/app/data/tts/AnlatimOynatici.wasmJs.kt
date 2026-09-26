@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.data.tts

import com.kuzeykapisi.app.Metinler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ses hızı/perdesi — kolayca ayarlanabilir sabitler. */
private const val KONUSMA_HIZI = 0.92
private const val KONUSMA_PERDESI = 1.0

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

    init {
        // Ses listesini ERKENDEN ısıt: getVoices() ilk çağrıda boş dönebilir
        // ve asenkron dolar. Bu nesne, kullanıcı "Dinle"ye basmadan çok önce
        // (Anlatım ekranı açılırken) oluşturulduğu için liste o ana kadar
        // hazır olur. Böylece oynat() içinde ASENKRON BEKLEMEYE GİRMEDEN
        // (bkz. oynat) kaliteli ses seçilebilir.
        runCatching { jsSesleriIsit() }
    }

    actual fun oynat(metin: String) {
        _hata.value = null
        etkinJeton++
        val jeton = etkinJeton
        runCatching {
            jsKonusBaslat(
                metin = metin,
                dil = "tr-TR",
                hiz = KONUSMA_HIZI,
                perde = KONUSMA_PERDESI,
                bitti = {
                    if (jeton == etkinJeton) _durum.value = AnlatimDurumu.DURDU
                },
                hataOldu = { kod ->
                    if (jeton == etkinJeton) {
                        _durum.value = AnlatimDurumu.DURDU
                        _hata.value = seslendirmeHataMesaji(kod)
                    }
                },
            )
            // Tarayıcıda API yoksa hataOldu senkron çağrılmış ve durum zaten
            // DURDU + hata doludur; OYNUYOR'a çekilmez.
            if (_hata.value == null) _durum.value = AnlatimDurumu.OYNUYOR
        }.onFailure {
            // speak() beklenmedik bir istisna fırlattı: API var ama bu deneme
            // başarısız — "desteklenmiyor" DEĞİL, geçici hata.
            _durum.value = AnlatimDurumu.DURDU
            _hata.value = Metinler.SESLENDIRME_GECICI_HATA
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

/**
 * Tarayıcının ses listesini önceden yükletir. getVoices() ilk çağrıda boş
 * dönebilir; listeyi bir kez okumak yüklemeyi tetikler, "voiceschanged"
 * dinleyicisi de liste hazır olduğunda tekrar okuyup tarayıcının kendi
 * önbelleğini sıcak tutar. Burada HİÇBİR ses referansı SAKLANMAZ — Chrome
 * bazı sürümlerde her getVoices() çağrısında yeni nesneler üretir ve eski
 * bir referansı utterance.voice'a atamak sessizce başarısız olur; bu yüzden
 * seçim her zaman oynat() içinde TAZE listeden yapılır.
 */
private fun jsSesleriIsit() {
    js(
        """
        (function() {
            var sentez = window.speechSynthesis;
            if (!sentez) return;
            sentez.getVoices();
            // onvoiceschanged'i EZMEK yerine dinleyici ekle (başka kod da
            // aynı olayı kullanıyor olabilir).
            sentez.addEventListener('voiceschanged', function() { sentez.getVoices(); });
        })();
        """,
    )
}

/**
 * Konuşmayı başlatır. TAMAMEN SENKRONDUR: speak() çağrısı kullanıcının
 * dokunma olayıyla AYNI görevde (task) yapılmalıdır — setTimeout /
 * voiceschanged gibi ertelenmiş bir görevden çağrılırsa Chrome, kullanıcı
 * etkileşimi bağlamı kaybolduğu için konuşmayı SESSİZCE reddeder (ne ses
 * duyulur, ne onstart/onerror tetiklenir; utterance kuyrukta asılı kalır).
 *
 * Ses seçimi güvenli bir düşüş zinciriyle yapılır:
 *  1) localService === false (ağ/bulut tabanlı, genelde daha kaliteli) tr sesi,
 *  2) yoksa herhangi bir tr sesi (masaüstü Chrome/Edge'de çoğu zaman TÜM
 *     sesler localService === true'dur — bu normaldir),
 *  3) hiç tr sesi yoksa voice HİÇ ATANMAZ, yalnızca lang verilip tarayıcının
 *     kendi varsayılanına bırakılır (liste henüz dolmadıysa da bu yola girilir
 *     ve ses yine de çıkar).
 */
/**
 * SpeechSynthesisErrorEvent.error koduna göre kullanıcı mesajı. null: kullanıcıya
 * gösterilecek bir şey yok (konuşma bilerek kesildi).
 */
private fun seslendirmeHataMesaji(kod: String): String? = when (kod) {
    // cancel()/yeni oynat() ile bilerek kesilen konuşma: hata değil.
    "interrupted", "canceled" -> null
    // Tarayıcıda Web Speech API hiç yok ya da sentez motoru kullanılamıyor.
    DESTEK_YOK, "synthesis-unavailable" -> Metinler.SESLENDIRME_TARAYICIDA_YOK
    // API var ama Türkçe ses yok.
    "language-unavailable", "voice-unavailable" -> Metinler.SESLENDIRME_TURKCE_YOK
    // audio-busy, audio-hardware, network, not-allowed, text-too-long...: bu
    // deneme başarısız, bir sonraki deneme çalışabilir.
    else -> Metinler.SESLENDIRME_GECICI_HATA
}

/** jsKonusBaslat'ın "API yok" durumunda hataOldu'ya verdiği kod (tarayıcı kodlarıyla çakışmaz). */
private const val DESTEK_YOK = "kuzey-destek-yok"

private fun jsKonusBaslat(
    metin: String,
    dil: String,
    hiz: Double,
    perde: Double,
    bitti: () -> Unit,
    hataOldu: (String) -> Unit,
) {
    js(
        """
        (function() {
            var sentez = window.speechSynthesis;
            if (!sentez || typeof SpeechSynthesisUtterance === 'undefined') {
                hataOldu('kuzey-destek-yok');
                return;
            }

            // Askıda kalmış bir konuşma yeni speak()'i sessizce engelleyebilir.
            // cancel() tek başına global "paused" bayrağını TEMİZLEMEZ: daha
            // önce Duraklat'a basılmışsa sentezleyici duraklatılmış kalır ve
            // sonraki her speak() sessiz olur — bu yüzden resume() da çağrılır.
            sentez.cancel();
            sentez.resume();

            var sesler = sentez.getVoices() || [];
            var trSesler = [];
            for (var i = 0; i < sesler.length; i++) {
                var lang = sesler[i].lang;
                if (lang && lang.toLowerCase().indexOf('tr') === 0) {
                    trSesler.push(sesler[i]);
                }
            }

            var secilen = null;
            for (var j = 0; j < trSesler.length; j++) {
                if (trSesler[j].localService === false) { secilen = trSesler[j]; break; }
            }
            if (secilen === null && trSesler.length > 0) { secilen = trSesler[0]; }

            var utterance = new SpeechSynthesisUtterance(metin);
            if (secilen !== null) {
                utterance.voice = secilen;
                utterance.lang = secilen.lang;
            } else {
                utterance.lang = dil;
            }
            utterance.rate = hiz;
            utterance.pitch = perde;
            utterance.onend = function() { bitti(); };
            utterance.onerror = function(olay) { hataOldu(String((olay && olay.error) || '')); };
            sentez.speak(utterance);
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
