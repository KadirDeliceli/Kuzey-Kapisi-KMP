package com.kuzeykapisi.app.data.ses

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.platform.AndroidContextHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

private const val TERCIHLER_ADI = "ses_kaydedici_izin_tercihleri"
private const val ANAHTAR_RET_SAYACI = "ret_sayaci"

private const val MESAJ_IZIN_YOK = Metinler.MIKROFON_IZNI_YOK
private const val MESAJ_KALICI_RET = Metinler.MIKROFON_IZNI_KAPALI
private const val MESAJ_BASLATILAMADI = Metinler.SES_KAYDI_BASLATILAMADI
private const val MESAJ_KAYDEDILEMEDI = Metinler.SES_KAYDEDILEMEDI

/**
 * MediaRecorder(Context) kurucusu API 31 gerektirir; minSdk 24 olduğu için
 * eski (deprecated ama 24-30 arası TEK çalışan) no-arg kurucu kullanılır —
 * AnlatimOynatici.android.kt'deki TextToSpeech ile aynı gerekçe.
 *
 * "İlk kez soruluyor" ile "kalıcı reddedilmiş" ayrımı: shouldShowRequestPermissionRationale
 * bir Activity gerektirir, AndroidContextHolder ise yalnızca Application context tutar
 * (bkz. AndroidContextHolder.kt) — o yüzden burada erişilemez. Üstelik bu API'nin
 * kendisi de "hiç sorulmadı" ile "kalıcı reddedildi" durumlarının İKİSİNDE de false
 * döner, tek başına yeterli değildir. Bunun yerine SharedPreferences'a yazılan bir
 * ret sayacı kullanılır: İLK reddediş REDDEDILDI (platform tekrar sorabilir),
 * İKİNCİ ve sonraki reddedişler KALICI_REDDEDILDI kabul edilir — "don't ask again"
 * işaretlenmişse ikinci deneme zaten platformdan hiç diyalog göstermeden anında
 * reddedilir, bu yüzden sayaç en geç ikinci denemede doğru sonuca yakınsar.
 */
@Suppress("DEPRECATION")
actual class SesKaydedici actual constructor() {
    private val _durum = MutableStateFlow(KayitDurumu.BOSTA)
    actual val durum: StateFlow<KayitDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    private val _izinDurumu = MutableStateFlow(MikrofonIzniDurumu.SORULMADI)
    actual val izinDurumu: StateFlow<MikrofonIzniDurumu> = _izinDurumu.asStateFlow()

    actual val ayarlarDestekleniyor: Boolean = true

    private var aktifKaydedici: MediaRecorder? = null
    private var aktifDosya: File? = null

    private fun tercihler(context: Context) = context.getSharedPreferences(TERCIHLER_ADI, Context.MODE_PRIVATE)

    /** İzin reddedildiğinde çağrılır — ret sayacıyla REDDEDILDI mi KALICI_REDDEDILDI mi olduğuna karar verir. */
    private fun izinReddiIsle(context: Context) {
        val prefs = tercihler(context)
        val sayac = prefs.getInt(ANAHTAR_RET_SAYACI, 0) + 1
        prefs.edit().putInt(ANAHTAR_RET_SAYACI, sayac).apply()

        val kaliciMi = sayac >= 2
        _izinDurumu.value = if (kaliciMi) MikrofonIzniDurumu.KALICI_REDDEDILDI else MikrofonIzniDurumu.REDDEDILDI
        _hata.value = if (kaliciMi) MESAJ_KALICI_RET else MESAJ_IZIN_YOK
    }

    actual fun kayidaBasla() {
        if (_durum.value != KayitDurumu.BOSTA) return
        _hata.value = null

        val context = AndroidContextHolder.appContext
        val izinli = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (!izinli) {
            izinReddiIsle(context)
            return
        }

        // İzin verildi — önceki ret sayacı/izin durumu artık geçersiz.
        tercihler(context).edit().putInt(ANAHTAR_RET_SAYACI, 0).apply()
        _izinDurumu.value = MikrofonIzniDurumu.SORULMADI

        val dosya = runCatching { File.createTempFile("ses_kaydi", ".m4a", context.cacheDir) }.getOrNull()
        if (dosya == null) {
            _hata.value = MESAJ_BASLATILAMADI
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
            _hata.value = MESAJ_BASLATILAMADI
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

        // stop(), çok kısa bir kayıtta (MediaRecorder kısıtı) RuntimeException
        // fırlatır. release() bu yüzden finally'de: stop() patlasa bile native
        // kaydedici HER ZAMAN serbest bırakılır, yoksa mikrofon kilitli kalır ve
        // sonraki kayıtlar başlatılamaz. stop() başarısızsa dosya geçersizdir,
        // veri okunmaz (önceki davranışla aynı: "Ses kaydedilemedi.").
        var durdurmaBasarili = kaydedici == null
        if (kaydedici != null) {
            try {
                kaydedici.stop()
                durdurmaBasarili = true
            } catch (e: RuntimeException) {
                // Yutulur — kısa kayıt ya da geçersiz durum; aşağıda veri null kalır.
            } finally {
                runCatching { kaydedici.release() }
            }
        }

        val veri = if (durdurmaBasarili) runCatching { dosya?.readBytes() }.getOrNull() else null

        dosya?.delete()
        _durum.value = KayitDurumu.BOSTA

        if (veri == null || veri.isEmpty()) {
            _hata.value = MESAJ_KAYDEDILEMEDI
            return null
        }
        return KaydedilenSes(veri, "ses_kaydi.m4a", "audio/m4a")
    }

    actual fun ayarlariAc() {
        val context = AndroidContextHolder.appContext
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    // Android'de bu sınıfın kurduğu, ekran kapanınca elle temizlenmesi
    // gereken bir global dinleyici/kaynak yok (web'deki permissions.onchange
    // dinleyicisinin karşılığı burada mevcut değil) — no-op.
    actual fun serbestBirak() {}
}
