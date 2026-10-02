package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** Yanlışlıkla dokunmayı yanlış anlamamak için bu süreden kısa kayıtlar gönderilmez, sessizce atılır. */
private val MIN_KAYIT_SURESI = 500.milliseconds

/** Gönderim uyarısı bu süre sonra (yeni deneme olmasa da) kendiliğinden kaybolur. */
private val AG_UYARISI_SURESI = 7.seconds

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
    // LAZY: motor, kullanıcı hiç bir ses butonuna basmadıkça hiç kurulmaz —
    // bkz. oynaticiyaEris.
    private var oynatici: AnlatimOynatici? = null
    private val _oynatilanMesajId = MutableStateFlow<Long?>(null)
    val oynatilanMesajId: StateFlow<Long?> = _oynatilanMesajId.asStateFlow()

    private var kayitBaslangic: TimeSource.Monotonic.ValueTimeMark? = null

    private var agUyarisiZamanlayici: Job? = null

    // Mesaj kimlikleri listedeki konumdan BAĞIMSIZ, kalıcı ve artan — bkz. [Mesaj.id].
    private var sonMesajId = 0L

    private fun yeniMesaj(metin: String, benden: Boolean, sistemNotu: Boolean = false): Mesaj =
        Mesaj(id = ++sonMesajId, metin = metin, benden = benden, sistemNotu = sistemNotu)

    /**
     * TTS motorunu İLK gerçek kullanımda (kullanıcı bir ses butonuna ilk
     * bastığında) kurar ve durumunu dinlemeye başlar — sohbet ekranı her
     * açılışında (sesli yanıt hiç kullanılmasa bile) motor kurulmasın diye.
     */
    private fun oynaticiyaEris(): AnlatimOynatici {
        oynatici?.let { return it }
        val yeni = AnlatimOynatici()
        oynatici = yeni
        viewModelScope.launch {
            yeni.durum.collect { d ->
                // Ses kendiliğinden bitince (kullanıcı durdurmadan) buton
                // otomatik eski hâline dönsün.
                if (d == AnlatimDurumu.DURDU && _oynatilanMesajId.value != null) {
                    _oynatilanMesajId.value = null
                }
            }
        }
        return yeni
    }

    /** Gönderim uyarısını gösterir ve [AG_UYARISI_SURESI] sonra kendiliğinden kaldırır. */
    private fun agUyarisiGoster(mesaj: String) {
        agUyarisiZamanlayici?.cancel()
        _state.update { it.copy(agUyarisi = mesaj) }
        agUyarisiZamanlayici = viewModelScope.launch {
            delay(AG_UYARISI_SURESI)
            _state.update { it.copy(agUyarisi = null) }
        }
    }

    private fun agUyarisiniKaldir() {
        agUyarisiZamanlayici?.cancel()
        agUyarisiZamanlayici = null
        _state.update { it.copy(agUyarisi = null) }
    }

    init {
        // Oturum ViewModel oluşurken bir kez açılır; döndürmede ViewModel
        // korunduğu için yeniden açılmaz (sohbet geçmişi de korunur).
        basla()
    }

    private fun basla() {
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null) }
            Logger.d { "oturum başlatılıyor (kategori=$kategori, oge=$oge)" }
            try {
                val yanit = repo.oturumBaslat(kategori, oge)
                Logger.d { "oturum başlatıldı" }
                _state.update {
                    it.copy(
                        baslik = yanit.baslik,
                        sessionId = yanit.sessionId,
                        mesajlar = listOf(yeniMesaj(metin = yanit.karsilama, benden = false)),
                        yukleniyor = false,
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "oturum başlatılamadı: ${e.logOzeti()}" }
                _state.update { it.copy(yukleniyor = false, hata = Metinler.hataMesaji(e)) }
            }
        }
    }

    fun gonder(mesaj: String) {
        val sessionId = _state.value.sessionId ?: return
        if (mesaj.isBlank()) return
        // Yeni deneme başlar başlamaz eski uyarı kalkar (sonucu beklenmez).
        agUyarisiniKaldir()
        _state.update { st ->
            st.copy(
                mesajlar = st.mesajlar + yeniMesaj(metin = mesaj, benden = true),
                yaziyor = true,
            )
        }
        viewModelScope.launch {
            try {
                val sonuc = repo.guvenliSohbet(kategori, oge, sessionId, mesaj)
                // Yeni mesajlar, yazma ANINDAKİ listeye eklenir (atomik): istek
                // sürerken eklenen başka bir mesaj kaybolmaz.
                val yeniMesajlar = buildList {
                    if (sonuc.yenilendi) add(yeniMesaj(metin = Metinler.SOHBET_YENILENDI, benden = false, sistemNotu = true))
                    add(yeniMesaj(metin = sonuc.cevap, benden = false))
                }
                val guncel = _state.updateAndGet {
                    it.copy(sessionId = sonuc.sessionId, mesajlar = it.mesajlar + yeniMesajlar, yaziyor = false)
                }
                agUyarisiniKaldir()
                Logger.d { "sohbet yanıtı alındı (yenilendi=${sonuc.yenilendi}, mesaj sayısı=${guncel.mesajlar.size})" }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "sohbet hatası: ${e.logOzeti()}" }
                _state.update { it.copy(yaziyor = false) }
                agUyarisiGoster(Metinler.hataMesaji(e))
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
        agUyarisiniKaldir()
        _state.update { it.copy(yaziyor = true) }
        try {
            val sonuc = repo.guvenliSesliSohbet(kategori, oge, sessionId, ses)
            val yeniMesajlar = buildList {
                add(yeniMesaj(metin = sonuc.kullaniciMetni, benden = true))
                if (sonuc.yenilendi) add(yeniMesaj(metin = Metinler.SOHBET_YENILENDI, benden = false, sistemNotu = true))
                add(yeniMesaj(metin = sonuc.cevap, benden = false))
            }
            val guncel = _state.updateAndGet {
                it.copy(sessionId = sonuc.sessionId, mesajlar = it.mesajlar + yeniMesajlar, yaziyor = false)
            }
            // Bot cevabı listenin son öğesi: otomatik okunacak mesajın kalıcı kimliği.
            val botMesajId = guncel.mesajlar.last().id
            agUyarisiniKaldir()
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
            _state.update { it.copy(yaziyor = false) }
            agUyarisiGoster(hataMetni)
        }
    }

    /**
     * TEK paylaşılan oynatma mekanizması — hem sesli mesajın otomatik
     * okunması (gonderSesliMesaj) hem de manuel "tekrar dinle" butonu
     * (ChatSheet) bunu çağırır.
     */
    fun mesajSesiCal(mesajId: Long) {
        val simdikiId = _oynatilanMesajId.value
        when {
            simdikiId == mesajId -> {
                oynatici?.durdur()
                _oynatilanMesajId.value = null
            }
            // Başka bir mesaj çalıyor — bu bir savunma kontrolüdür, UI zaten
            // bu durumda ilgili butonu devre dışı bırakır.
            simdikiId != null -> Unit
            else -> {
                val metin = _state.value.mesajlar.find { it.id == mesajId }?.metin ?: return
                oynaticiyaEris().oynat(metin)
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
        oynatici?.let {
            it.durdur()
            it.serbestBirak()
        }
        _oynatilanMesajId.value = null
        if (sesKaydedici.durum.value != KayitDurumu.BOSTA) {
            // Devam eden kayıt varsa iptal edilip temizlenir — sonuç GÖNDERİLMEZ.
            GlobalScope.launch { sesKaydedici.kayidiDurdurVeAl() }
        }
        sesKaydedici.serbestBirak()
    }
}
