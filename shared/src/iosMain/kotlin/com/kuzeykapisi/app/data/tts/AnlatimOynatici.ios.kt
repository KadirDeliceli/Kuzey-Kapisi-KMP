package com.kuzeykapisi.app.data.tts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.darwin.NSObject

actual class AnlatimOynatici actual constructor() {
    private val _durum = MutableStateFlow(AnlatimDurumu.DURDU)
    actual val durum: StateFlow<AnlatimDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    private val delegate = object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didStartSpeechUtterance: AVSpeechUtterance) {
            _durum.value = AnlatimDurumu.OYNUYOR
        }

        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
            _durum.value = AnlatimDurumu.DURDU
        }

        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didCancelSpeechUtterance: AVSpeechUtterance) {
            _durum.value = AnlatimDurumu.DURDU
        }

        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didPauseSpeechUtterance: AVSpeechUtterance) {
            _durum.value = AnlatimDurumu.DURAKLATILDI
        }

        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didContinueSpeechUtterance: AVSpeechUtterance) {
            _durum.value = AnlatimDurumu.OYNUYOR
        }
    }

    private val synthesizer = AVSpeechSynthesizer().apply { delegate = this@AnlatimOynatici.delegate }

    actual fun oynat(metin: String) {
        _hata.value = null
        val ses = AVSpeechSynthesisVoice.voiceWithLanguage("tr-TR")
        if (ses == null) {
            _hata.value = "Bu cihazda Türkçe seslendirme desteklenmiyor."
            return
        }
        if (synthesizer.speaking || synthesizer.paused) {
            synthesizer.stopSpeaking(atBoundary = AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
        val utterance = AVSpeechUtterance(string = metin).apply { voice = ses }
        synthesizer.speakUtterance(utterance)
    }

    // AVSpeechSynthesizer native pause/resume destekler — konum kendisi tutulur.
    actual fun duraklat() {
        synthesizer.pauseSpeaking(atBoundary = AVSpeechBoundary.AVSpeechBoundaryWord)
    }

    actual fun devamEt() {
        synthesizer.continueSpeaking()
    }

    actual fun durdur() {
        synthesizer.stopSpeaking(atBoundary = AVSpeechBoundary.AVSpeechBoundaryImmediate)
        _durum.value = AnlatimDurumu.DURDU
    }

    actual fun serbestBirak() {
        durdur()
    }
}
