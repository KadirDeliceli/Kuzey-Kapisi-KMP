package com.kuzeykapisi.app.data.tts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.kuzeykapisi.app.data.location.AndroidContextHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Google'ın kendi TTS motoru — kurulu ise OEM (Samsung vb.) motorlarından genelde daha kaliteli sesler sunar. */
private const val GOOGLE_TTS_PAKET_ADI = "com.google.android.tts"

/** Ses hızı/perdesi — kolayca ayarlanabilir sabitler. */
private const val KONUSMA_HIZI = 0.92f
private const val KONUSMA_PERDESI = 1.0f

@Suppress("DEPRECATION")
actual class AnlatimOynatici actual constructor() {
    private val _durum = MutableStateFlow(AnlatimDurumu.DURDU)
    actual val durum: StateFlow<AnlatimDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    private val anaThread = Handler(Looper.getMainLooper())

    // TextToSpeech'in ilerleme geri çağrıları (onStart/onDone/onRangeStart)
    // bir binder thread'inden gelir; oynat/duraklat/devamEt ise UI
    // thread'inden çağrılır. Paylaşılan tüm değişkenler bu yüzden @Volatile.
    @Volatile private var tts: TextToSpeech? = null
    @Volatile private var hazir = false
    @Volatile private var bekleyenEylem: (() -> Unit)? = null

    // TextToSpeech native pause/resume desteklemez — konum burada simüle
    // edilir. orijinalMetin: son oynat() ile verilen tam metin. sonKarakterKonumu:
    // duraklatıldığında/tamamlandığında kalınan mutlak karakter konumu.
    // akisOfseti: o an oynatılmakta olan speak() çağrısının orijinalMetin
    // içindeki başlangıç ofseti (onRangeStart'ın göreli konumunu mutlağa
    // çevirmek için).
    @Volatile private var orijinalMetin: String = ""
    @Volatile private var sonKarakterKonumu: Int = 0
    @Volatile private var akisOfseti: Int = 0

    // Her speak() çağrısına benzersiz bir kimlik verilir. stop() sonrası
    // gecikmeli gelen onRangeStart/onDone geri çağrıları GÜNCEL konuşmanın
    // konumunu/durumunu bozmasın diye yalnızca kimliği tutan çağrılar
    // dikkate alınır (bkz. duraklat: kimlik stop()'tan ÖNCE düşürülür).
    @Volatile private var aktifSeslendirmeId: String? = null
    private var seslendirmeSayaci = 0

    private val ilerlemeDinleyicisi = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            if (utteranceId != aktifSeslendirmeId) return
            _durum.value = AnlatimDurumu.OYNUYOR
        }

        // Kesintisiz TAMAMLANMA: konum sıfırlanır, bir sonraki "Dinle" baştan başlar.
        override fun onDone(utteranceId: String?) {
            if (utteranceId != aktifSeslendirmeId) return
            sonKarakterKonumu = 0
            _durum.value = AnlatimDurumu.DURDU
        }

        override fun onError(utteranceId: String?) {
            if (utteranceId != aktifSeslendirmeId) return
            sonKarakterKonumu = 0
            _durum.value = AnlatimDurumu.DURDU
            _hata.value = "Anlatım okunamadı."
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            if (utteranceId != aktifSeslendirmeId) return
            sonKarakterKonumu = akisOfseti + start
        }
    }

    init {
        // Motor, ekran açılır açılmaz (kullanıcı butona basmadan çok önce)
        // kurulmaya başlar: bu nesne AnlatimViewModel tarafından AnlatimEkranı
        // ilk kez oluşturulurken yaratılır.
        motoruBaslat(AndroidContextHolder.appContext, googleMotoruDene = true)
    }

    private fun motoruBaslat(context: Context, googleMotoruDene: Boolean) {
        val dinleyici = TextToSpeech.OnInitListener { sonuc ->
            // onInit, motor bağlanamadığında TextToSpeech KURUCUSUNUN İÇİNDEN
            // senkron olarak çağrılabilir — o anda 'tts' alanı henüz atanmamış
            // olur. Ayrıca speak()'i doğrudan onInit içinde çağırmak bazı
            // motorlarda sessizce yok sayılır. Her iki tuzağı da atlamak için
            // işlem ana thread'in BİR SONRAKİ mesajına ertelenir.
            anaThread.post { initTamamlandi(sonuc, context, googleMotoruDene) }
        }
        val yeniTts = runCatching {
            if (googleMotoruDene) {
                TextToSpeech(context, dinleyici, GOOGLE_TTS_PAKET_ADI)
            } else {
                TextToSpeech(context, dinleyici)
            }
        }.getOrNull()
        tts = yeniTts
        yeniTts?.setOnUtteranceProgressListener(ilerlemeDinleyicisi)
    }

    /** Motor/dil/ses seçiminin TAMAMI init tamamlandıktan SONRA yapılır. */
    private fun initTamamlandi(sonuc: Int, context: Context, googleMotoruDenendi: Boolean) {
        val motor = tts
        if (sonuc != TextToSpeech.SUCCESS || motor == null) {
            // Google motoru istendi ama açılamadı (kurulu değil / bağlanamadı).
            // TextToSpeech, paket adı verilen kurucuda varsayılan motora KENDİ
            // BAŞINA düşmez — bu yüzden burada varsayılan motorla bir kez daha
            // denenir; ancak o da başarısız olursa hata gösterilir.
            if (googleMotoruDenendi) {
                runCatching { motor?.shutdown() }
                tts = null
                motoruBaslat(context, googleMotoruDene = false)
            } else {
                _hata.value = "Ses motoru başlatılamadı."
            }
            return
        }

        val dilSonucu = runCatching { motor.setLanguage(Locale("tr", "TR")) }.getOrNull()
        if (dilSonucu == TextToSpeech.LANG_MISSING_DATA || dilSonucu == TextToSpeech.LANG_NOT_SUPPORTED) {
            _hata.value = "Bu cihazda Türkçe seslendirme desteklenmiyor."
            return
        }

        enIyiTrSesiSecVeUygula(motor)
        runCatching {
            motor.setSpeechRate(KONUSMA_HIZI)
            motor.setPitch(KONUSMA_PERDESI)
        }

        hazir = true
        bekleyenEylem?.let { eylem ->
            bekleyenEylem = null
            eylem()
        }
    }

    /**
     * tr-TR sesleri arasından en iyisini seçer: VERY_HIGH/HIGH kalite şartı
     * korunur ve AĞ TABANLI (bulut/nöral) sesler yerel/gömülü seslere TERCİH
     * EDİLİR — ses kalitesi bu seslerde belirgin şekilde daha iyidir. Uygun
     * ses yoksa varsayılan sesle sessizce devam edilir.
     *
     * Not: Ağ tabanlı sesler metni daha büyük parçalar hâlinde işleyip
     * onRangeStart'ı oynatmanın ilerisinden bildirebildiği için "kaldığı
     * yerden devam" hassasiyetini düşürme riski taşır. Bu risk bilinerek
     * kalite lehine tercih edilmiştir; hassasiyet bozulursa öncelik yeniden
     * değerlendirilmelidir.
     */
    private fun enIyiTrSesiSecVeUygula(motor: TextToSpeech) {
        val sesler = runCatching { motor.voices }.getOrNull() ?: return
        val enIyiSes = sesler
            .filter { it.locale.language == "tr" && it.quality >= Voice.QUALITY_HIGH }
            .sortedWith(
                // true (ağ tabanlı) önce gelir, sonra kalite büyükten küçüğe.
                compareByDescending<Voice> { it.isNetworkConnectionRequired }
                    .thenByDescending { it.quality },
            )
            .firstOrNull()
        if (enIyiSes != null) {
            runCatching { motor.voice = enIyiSes }
        }
    }

    /** Verilen parçayı seslendirir; [ofset] parçanın orijinalMetin içindeki mutlak başlangıcıdır. */
    private fun seslendir(parca: String, ofset: Int) {
        val motor = tts ?: return
        seslendirmeSayaci++
        val id = "anlatim-$seslendirmeSayaci"
        // Ofset ve kimlik, speak()'ten ÖNCE yazılır ki ilk onRangeStart bile
        // doğru mutlak konumu üretsin.
        akisOfseti = ofset
        aktifSeslendirmeId = id
        motor.speak(parca, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    actual fun oynat(metin: String) {
        _hata.value = null
        orijinalMetin = metin
        sonKarakterKonumu = 0
        calistirYaDaBeklet { seslendir(metin, ofset = 0) }
    }

    actual fun duraklat() {
        // Kimlik stop()'tan ÖNCE düşürülür: durdurulan konuşmadan gecikmeli
        // gelen onRangeStart çağrıları konumu duyulan noktanın ilerisine
        // taşımasın.
        aktifSeslendirmeId = null
        bekleyenEylem = null
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
        _hata.value = null
        calistirYaDaBeklet { seslendir(orijinalMetin.substring(baslangic), ofset = baslangic) }
    }

    private fun calistirYaDaBeklet(eylem: () -> Unit) {
        if (hazir && tts != null) {
            eylem()
        } else {
            // Motor henüz hazır değil: istek YOK SAYILMAZ, hazır olunca
            // initTamamlandi() tarafından çalıştırılır.
            bekleyenEylem = eylem
        }
    }

    actual fun durdur() {
        aktifSeslendirmeId = null
        bekleyenEylem = null
        tts?.stop()
        orijinalMetin = ""
        sonKarakterKonumu = 0
        akisOfseti = 0
        _durum.value = AnlatimDurumu.DURDU
    }

    actual fun serbestBirak() {
        aktifSeslendirmeId = null
        bekleyenEylem = null
        hazir = false
        tts?.shutdown()
        tts = null
    }
}
