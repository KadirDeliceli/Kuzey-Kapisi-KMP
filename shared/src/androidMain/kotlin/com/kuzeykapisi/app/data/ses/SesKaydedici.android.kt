package com.kuzeykapisi.app.data.ses

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.kuzeykapisi.app.data.location.AndroidContextHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * MediaRecorder(Context) kurucusu API 31 gerektirir; minSdk 24 olduğu için
 * eski (deprecated ama 24-30 arası TEK çalışan) no-arg kurucu kullanılır —
 * AnlatimOynatici.android.kt'deki TextToSpeech ile aynı gerekçe.
 */
@Suppress("DEPRECATION")
actual class SesKaydedici actual constructor() {
    private val _durum = MutableStateFlow(KayitDurumu.BOSTA)
    actual val durum: StateFlow<KayitDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    private var aktifKaydedici: MediaRecorder? = null
    private var aktifDosya: File? = null

    actual fun kayidaBasla() {
        if (_durum.value != KayitDurumu.BOSTA) return
        _hata.value = null

        val context = AndroidContextHolder.appContext
        val izinli = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!izinli) {
            _hata.value = "Mikrofon izni verilmedi."
            return
        }

        val dosya = runCatching { File.createTempFile("ses_kaydi", ".m4a", context.cacheDir) }.getOrNull()
        if (dosya == null) {
            _hata.value = "Ses kaydı başlatılamadı."
            return
        }

        val kaydedici = runCatching {
            MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(dosya.absolutePath)
                prepare()
                start()
            }
        }.getOrNull()

        if (kaydedici == null) {
            dosya.delete()
            _hata.value = "Ses kaydı başlatılamadı."
            return
        }

        aktifKaydedici = kaydedici
        aktifDosya = dosya
        _durum.value = KayitDurumu.KAYIT_YAPILIYOR
    }

    actual suspend fun kayidiDurdurVeAl(): KaydedilenSes? {
        if (_durum.value != KayitDurumu.KAYIT_YAPILIYOR) return null
        _durum.value = KayitDurumu.ISLENIYOR

        val kaydedici = aktifKaydedici
        val dosya = aktifDosya
        aktifKaydedici = null
        aktifDosya = null

        val veri = runCatching {
            kaydedici?.apply {
                stop()
                release()
            }
            dosya?.readBytes()
        }.getOrNull()

        dosya?.delete()
        _durum.value = KayitDurumu.BOSTA

        if (veri == null || veri.isEmpty()) {
            _hata.value = "Ses kaydedilemedi."
            return null
        }
        return KaydedilenSes(veri, "ses_kaydi.m4a", "audio/m4a")
    }
}
