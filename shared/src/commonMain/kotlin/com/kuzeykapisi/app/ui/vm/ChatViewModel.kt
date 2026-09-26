package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.model.ChatUiState
import com.kuzeykapisi.app.data.model.Mesaj
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.data.ses.KayitDurumu
import com.kuzeykapisi.app.data.ses.KaydedilenSes
import com.kuzeykapisi.app.data.ses.MikrofonIzniDurumu
import com.kuzeykapisi.app.data.ses.SesKaydedici
import com.kuzeykapisi.app.data.tts.AnlatimDurumu
import com.kuzeykapisi.app.data.tts.AnlatimOynatici
import com.kuzeykapisi.app.log.Logger
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/** Yanlışlıkla dokunmayı yanlış anlamamak için bu süreden kısa kayıtlar gönderilmez, sessizce atılır. */
private val MIN_KAYIT_SURESI = 500.milliseconds

class ChatViewModel(
    private val repo: KuzeyRepository,
    private val kategori: String,
    private val oge: String,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val sesKaydedici = SesKaydedici()
    val kayitDurumu: StateFlow<KayitDurumu> = sesKaydedici.durum
    val kayitHatasi: StateFlow<String?> = sesKaydedici.hata
    val mikrofonIzniDurumu: StateFlow<MikrofonIzniDurumu> = sesKaydedici.izinDurumu
    val ayarlarDestekleniyor: Boolean = sesKaydedici.ayarlarDestekleniyor

    /** Mikrofon izni kalıcı reddedilmişse platformun ayarlar sayfasını açar (bkz. [ayarlarDestekleniyor]). */
    fun ayarlariAc() = sesKaydedici.ayarlariAc()

    // Sesli mesaj gönderimi + "tekrar dinle" TEK bir paylaşılan TTS motorunu
    // ve TEK bir "hangi mesaj çalıyor" kaynağını kullanır (bkz. mesajSesiCal).
    private val oynatici = AnlatimOynatici()
    private val _oynatilanMesajId = MutableStateFlow<Int?>(null)
    val oynatilanMesajId: StateFlow<Int?> = _oynatilanMesajId.asStateFlow()

    private var kayitBaslangic: TimeSource.Monotonic.ValueTimeMark? = null

    init {
        viewModelScope.launch {
            oynatici.durum.collect { d ->
                // Ses kendiliğinden bitince (kullanıcı durdurmadan) buton
                // otomatik eski hâline dönsün.
                if (d == AnlatimDurumu.DURDU && _oynatilanMesajId.value != null) {
                    _oynatilanMesajId.value = null
                }
            }
        }
        // Oturum ViewModel oluşurken bir kez açılır; döndürmede ViewModel
        // korunduğu için yeniden açılmaz (sohbet geçmişi de korunur).
        basla()
    }

    private fun basla() {
        viewModelScope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            Logger.d { "oturum başlatılıyor (kategori=$kategori, oge=$oge)" }
            try {
                val yanit = repo.oturumBaslat(kategori, oge)
                Logger.d { "oturum başlatıldı" }
                _state.value = _state.value.copy(
                    baslik = yanit.baslik,
                    sessionId = yanit.sessionId,
                    mesajlar = listOf(Mesaj(metin = yanit.karsilama, benden = false)),
                    yukleniyor = false,
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "oturum başlatılamadı: ${e.logOzeti()}" }
                _state.value = _state.value.copy(yukleniyor = false, hata = Metinler.hataMesaji(e))
            }
        }
    }

    fun gonder(mesaj: String) {
        val sessionId = _state.value.sessionId ?: return
        if (mesaj.isBlank()) return
        _state.value = _state.value.copy(
            mesajlar = _state.value.mesajlar + Mesaj(metin = mesaj, benden = true),
            yaziyor = true,
        )
        viewModelScope.launch {
            try {
                val sonuc = repo.guvenliSohbet(kategori, oge, sessionId, mesaj)
                var mesajlar = _state.value.mesajlar
                if (sonuc.yenilendi) {
                    mesajlar = mesajlar + Mesaj(
                        metin = Metinler.SOHBET_YENILENDI,
                        benden = false,
                        sistemNotu = true,
                    )
                }
                mesajlar = mesajlar + Mesaj(metin = sonuc.cevap, benden = false)
                _state.value = _state.value.copy(
                    sessionId = sonuc.sessionId,
                    mesajlar = mesajlar,
                    yaziyor = false,
                )
                Logger.d { "sohbet yanıtı alındı (yenilendi=${sonuc.yenilendi}, mesaj sayısı=${mesajlar.size})" }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "sohbet hatası: ${e.logOzeti()}" }
                _state.value = _state.value.copy(
                    yaziyor = false,
                    hata = Metinler.hataMesaji(e),
                )
            }
        }
    }

    /** Mikrofon butonuna basılınca çağrılır — durum BOSTA ise kayda başlar, KAYIT_YAPILIYOR ise durdurup gönderir. */
    fun mikrofonaBasildi() {
        when (sesKaydedici.durum.value) {
            KayitDurumu.BOSTA -> {
                kayitBaslangic = TimeSource.Monotonic.markNow()
                sesKaydedici.kayidaBasla()
            }
            KayitDurumu.KAYIT_YAPILIYOR -> {
                val baslangic = kayitBaslangic
                viewModelScope.launch {
                    val ses = sesKaydedici.kayidiDurdurVeAl()
                    val yeterinceUzun = baslangic == null || baslangic.elapsedNow() >= MIN_KAYIT_SURESI
                    if (ses != null && yeterinceUzun) {
                        gonderSesliMesaj(ses)
                    }
                }
            }
            KayitDurumu.ISLENIYOR -> Unit
        }
    }

    private suspend fun gonderSesliMesaj(ses: KaydedilenSes) {
        val sessionId = _state.value.sessionId ?: return
        Logger.d { "sesli mesaj gönderiliyor" }
        _state.value = _state.value.copy(yaziyor = true)
        try {
            val sonuc = repo.guvenliSesliSohbet(kategori, oge, sessionId, ses)
            var mesajlar = _state.value.mesajlar + Mesaj(metin = sonuc.kullaniciMetni, benden = true)
            if (sonuc.yenilendi) {
                mesajlar = mesajlar + Mesaj(
                    metin = Metinler.SOHBET_YENILENDI,
                    benden = false,
                    sistemNotu = true,
                )
            }
            val botMesajId = mesajlar.size
            mesajlar = mesajlar + Mesaj(metin = sonuc.cevap, benden = false)
            _state.value = _state.value.copy(
                sessionId = sonuc.sessionId,
                mesajlar = mesajlar,
                yaziyor = false,
            )
            mesajSesiCal(botMesajId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.d { "sesli sohbet hatası: ${e.logOzeti()}" }
            // 400: ses çözümlenemedi (sessiz/anlaşılmaz kayıt) — kullanıcının
            // yeniden konuşması gerekir. Diğerleri türüne göre.
            val hataMetni = if (e is ClientRequestException && e.response.status.value == 400) {
                Metinler.SOHBET_SES_ANLASILAMADI
            } else {
                Metinler.hataMesaji(e)
            }
            _state.value = _state.value.copy(
                yaziyor = false,
                mesajlar = _state.value.mesajlar + Mesaj(metin = hataMetni, benden = false, sistemNotu = true),
            )
        }
    }

    /**
     * TEK paylaşılan oynatma mekanizması — hem sesli mesajın otomatik
     * okunması (gonderSesliMesaj) hem de manuel "tekrar dinle" butonu
     * (ChatSheet) bunu çağırır.
     */
    fun mesajSesiCal(mesajId: Int) {
        val simdikiId = _oynatilanMesajId.value
        when {
            simdikiId == mesajId -> {
                oynatici.durdur()
                _oynatilanMesajId.value = null
            }
            // Başka bir mesaj çalıyor — bu bir savunma kontrolüdür, UI zaten
            // bu durumda ilgili butonu devre dışı bırakır.
            simdikiId != null -> Unit
            else -> {
                val metin = _state.value.mesajlar.getOrNull(mesajId)?.metin ?: return
                oynatici.oynat(metin)
                _oynatilanMesajId.value = mesajId
            }
        }
    }

    /**
     * Sohbet paneli kapanıp kapsamı temizlendiğinde çalışır (yapılandırma
     * değişikliğinde çalışmaz). viewModelScope burada zaten iptal edilmiş
     * olduğu için sunucudaki oturumu kapatma ve yarım kalan kaydı bırakma
     * işleri ekrandan bağımsız, "at ve unut" olarak GlobalScope'ta yapılır.
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        _state.value.sessionId?.let { id -> GlobalScope.launch { repo.oturumKapat(id) } }
        oynatici.durdur()
        oynatici.serbestBirak()
        _oynatilanMesajId.value = null
        if (sesKaydedici.durum.value != KayitDurumu.BOSTA) {
            // Devam eden kayıt varsa iptal edilip temizlenir — sonuç GÖNDERİLMEZ.
            GlobalScope.launch { sesKaydedici.kayidiDurdurVeAl() }
        }
    }
}
