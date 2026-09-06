@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.kuzeykapisi.app.data.ses

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioApplication
import platform.AVFAudio.AVAudioQuality
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.posix.memcpy

private const val MESAJ_KALICI_RET = "Mikrofon izni kapalı. Ayarlardan açabilirsiniz."
private const val MESAJ_BASLATILAMADI = "Ses kaydı başlatılamadı."
private const val MESAJ_KAYDEDILEMEDI = "Ses kaydedilemedi."

private fun NSData.tumBaytlariAl(): ByteArray {
    val boy = length.toInt()
    if (boy == 0) return ByteArray(0)
    val veri = ByteArray(boy)
    veri.usePinned { pin ->
        memcpy(pin.addressOf(0), bytes, length)
    }
    return veri
}

/**
 * AVAudioRecorder kurucusu (initWithURL:settings:error:) ObjC tarafında nil
 * dönebilen "failable" bir init'tir; Kotlin/Native tarafında bu durum
 * çökmeye (crash) yol açabildiği için kurucu çağrısı runCatching ile
 * SARMALANIR — AnlatimOynatici.ios.kt'deki "asla çökme" ilkesiyle aynı.
 *
 * iOS'ta "ilk kez soruluyor" / "kalıcı reddedilmiş" ayrımı Android'deki gibi
 * belirsiz değildir: AVAudioSession.recordPermission senkron ve KESİN bir
 * cevap verir — .denied HER ZAMAN kalıcıdır, çünkü iOS bir kez reddedilince
 * bir daha KENDİLİĞİNDEN sistem izin diyaloğu göstermez (REDDEDILDI ara
 * durumu bu platformda hiç kullanılmaz, doğrudan KALICI_REDDEDILDI'ya geçilir).
 */
actual class SesKaydedici actual constructor() {
    private val _durum = MutableStateFlow(KayitDurumu.BOSTA)
    actual val durum: StateFlow<KayitDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    private val _izinDurumu = MutableStateFlow(MikrofonIzniDurumu.SORULMADI)
    actual val izinDurumu: StateFlow<MikrofonIzniDurumu> = _izinDurumu.asStateFlow()

    actual val ayarlarDestekleniyor: Boolean = true

    private var aktifKaydedici: AVAudioRecorder? = null
    private var aktifUrl: NSURL? = null

    init {
        // Kullanıcı daha mikrofon butonuna hiç basmadan (kalıcı reddedilmişse)
        // durumu bilinsin — AVAudioSession.recordPermission senkron okunabilir.
        if (kaliciReddedilmisMi()) {
            _izinDurumu.value = MikrofonIzniDurumu.KALICI_REDDEDILDI
            _hata.value = MESAJ_KALICI_RET
        }
    }

    private fun kaliciReddedilmisMi(): Boolean =
        runCatching { AVAudioSession.sharedInstance().recordPermission }.getOrNull() == AVAudioSessionRecordPermissionDenied

    actual fun kayidaBasla() {
        if (_durum.value != KayitDurumu.BOSTA) return
        _hata.value = null

        runCatching {
            val oturum = AVAudioSession.sharedInstance()
            oturum.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
            oturum.setActive(true, error = null)
        }

        // Önceden bilinen izin durumu (KALICI_REDDEDILDI dahil) burada ASLA
        // erken çıkış için kullanılmaz — izin gerçekten verilene kadar HER
        // basışta gerçek AVAudioApplication API'si yeniden çağrılır. iOS,
        // önceki bir reddedişten sonra kendi UI'ını göstermeyebilir (kendi
        // platform kısıtı) ama çağrının kendisi yine de yapılır.
        runCatching {
            AVAudioApplication.requestRecordPermissionWithCompletionHandler { izinVerildi ->
                if (!izinVerildi) {
                    // İlk (ve tek) reddediş — iOS'ta bu doğrudan kalıcıdır.
                    _izinDurumu.value = MikrofonIzniDurumu.KALICI_REDDEDILDI
                    _hata.value = MESAJ_KALICI_RET
                } else {
                    _izinDurumu.value = MikrofonIzniDurumu.SORULMADI
                    gercekKaydiBaslat()
                }
            }
        }.onFailure {
            _izinDurumu.value = MikrofonIzniDurumu.KALICI_REDDEDILDI
            _hata.value = MESAJ_KALICI_RET
        }
    }

    private fun gercekKaydiBaslat() {
        if (_durum.value != KayitDurumu.BOSTA) return

        val gecici = NSFileManager.defaultManager.temporaryDirectory
        val url = gecici.URLByAppendingPathComponent(NSUUID().UUIDString() + ".m4a")
        if (url == null) {
            _hata.value = MESAJ_BASLATILAMADI
            return
        }

        val ayarlar: Map<Any?, Any?> = mapOf(
            AVFormatIDKey to kAudioFormatMPEG4AAC,
            AVSampleRateKey to 44100.0,
            AVNumberOfChannelsKey to 1,
            AVEncoderAudioQualityKey to AVAudioQuality.AVAudioQualityHigh.value,
        )

        val kaydedici = runCatching { AVAudioRecorder(uRL = url, settings = ayarlar, error = null) }.getOrNull()
        if (kaydedici == null || !kaydedici.record()) {
            _hata.value = MESAJ_BASLATILAMADI
            return
        }

        aktifKaydedici = kaydedici
        aktifUrl = url
        _durum.value = KayitDurumu.KAYIT_YAPILIYOR
    }

    actual suspend fun kayidiDurdurVeAl(): KaydedilenSes? {
        if (_durum.value != KayitDurumu.KAYIT_YAPILIYOR) return null
        _durum.value = KayitDurumu.ISLENIYOR

        val kaydedici = aktifKaydedici
        val url = aktifUrl
        aktifKaydedici = null
        aktifUrl = null

        val veri = runCatching {
            kaydedici?.stop()
            url?.let { NSData.dataWithContentsOfURL(it) }?.tumBaytlariAl()
        }.getOrNull()

        runCatching { url?.path?.let { NSFileManager.defaultManager.removeItemAtPath(it, error = null) } }

        _durum.value = KayitDurumu.BOSTA

        if (veri == null || veri.isEmpty()) {
            _hata.value = MESAJ_KAYDEDILEMEDI
            return null
        }
        return KaydedilenSes(veri, "ses_kaydi.m4a", "audio/m4a")
    }

    actual fun ayarlariAc() {
        runCatching {
            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
            UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
    }
}
