package com.kuzeykapisi.app.data.tts

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.kuzeykapisi.app.data.location.AndroidContextHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val UTTERANCE_ID = "anlatim"

@Suppress("DEPRECATION")
actual class AnlatimOynatici actual constructor() {
    private val _durum = MutableStateFlow(AnlatimDurumu.DURDU)
    actual val durum: StateFlow<AnlatimDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    private var hazir = false
    private var bekleyenEylem: (() -> Unit)? = null
    private var tts: TextToSpeech? = null

    // TextToSpeech native pause/resume desteklemez — konum burada simüle
    // edilir. orijinalMetin: son oynat() ile verilen tam metin. sonKarakterKonumu:
    // duraklatıldığında/tamamlandığında kalınan mutlak karakter konumu.
    // akisOfseti: o an oynatılmakta olan speak() çağrısının orijinalMetin
    // içindeki başlangıç ofseti (onRangeStart'ın göreli konumunu mutlağa
    // çevirmek için).
    private var orijinalMetin: String = ""
    private var sonKarakterKonumu: Int = 0
    private var akisOfseti: Int = 0

    init {
        tts = TextToSpeech(AndroidContextHolder.appContext) { sonuc ->
            if (sonuc == TextToSpeech.SUCCESS) {
                val dilSonucu = tts?.setLanguage(Locale("tr", "TR"))
                if (dilSonucu == TextToSpeech.LANG_MISSING_DATA || dilSonucu == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _hata.value = "Bu cihazda Türkçe seslendirme desteklenmiyor."
                } else {
                    hazir = true
                    bekleyenEylem?.let { eylem ->
                        bekleyenEylem = null
                        eylem()
                    }
                }
            } else {
                _hata.value = "Ses motoru başlatılamadı."
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _durum.value = AnlatimDurumu.OYNUYOR
            }

            // Kesintisiz TAMAMLANMA: konum sıfırlanır, bir sonraki "Dinle" baştan başlar.
            override fun onDone(utteranceId: String?) {
                sonKarakterKonumu = 0
                _durum.value = AnlatimDurumu.DURDU
            }

            override fun onError(utteranceId: String?) {
                sonKarakterKonumu = 0
                _durum.value = AnlatimDurumu.DURDU
                _hata.value = "Anlatım okunamadı."
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                sonKarakterKonumu = akisOfseti + start
            }
        })
    }

    actual fun oynat(metin: String) {
        _hata.value = null
        orijinalMetin = metin
        sonKarakterKonumu = 0
        akisOfseti = 0
        calistirYaDaBeklet { tts?.speak(metin, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID) }
    }

    actual fun duraklat() {
        tts?.stop()
        if (_durum.value == AnlatimDurumu.OYNUYOR) {
            _durum.value = AnlatimDurumu.DURAKLATILDI
        }
    }

    actual fun devamEt() {
        if (orijinalMetin.isEmpty()) return
        val baslangic = sonKarakterKonumu.coerceIn(0, orijinalMetin.length)
        if (baslangic >= orijinalMetin.length) {
            // Metin zaten bitmiş — baştan başlamak "devam"dan daha anlamlı.
            oynat(orijinalMetin)
            return
        }
        akisOfseti = baslangic
        val kalanMetin = orijinalMetin.substring(baslangic)
        calistirYaDaBeklet { tts?.speak(kalanMetin, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID) }
    }

    private fun calistirYaDaBeklet(eylem: () -> Unit) {
        if (!hazir) {
            bekleyenEylem = eylem
            return
        }
        eylem()
    }

    actual fun durdur() {
        bekleyenEylem = null
        tts?.stop()
        orijinalMetin = ""
        sonKarakterKonumu = 0
        akisOfseti = 0
        _durum.value = AnlatimDurumu.DURDU
    }

    actual fun serbestBirak() {
        tts?.shutdown()
        tts = null
    }
}
