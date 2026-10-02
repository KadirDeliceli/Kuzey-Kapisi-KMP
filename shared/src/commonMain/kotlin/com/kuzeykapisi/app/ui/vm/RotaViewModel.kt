package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.location.Konum
import com.kuzeykapisi.app.data.location.VARSAYILAN_KONUM
import com.kuzeykapisi.app.data.location.guncelKonumAl
import com.kuzeykapisi.app.data.model.KategoriBilgi
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAKS_SECILI_TUR = 4

data class RotaUiState(
    val konum: Konum? = null,
    val kategoriler: Map<String, KategoriBilgi>? = null,
    // Kategoriler ilk yüklemeden (konum + varsayılan rotalar) BAĞIMSIZ yüklenir;
    // kendi yükleniyor/hata durumu vardır ki başarısız olunca "yükleniyor"da
    // takılı kalmasın ve tekrar denenebilsin.
    val kategorilerYukleniyor: Boolean = true,
    val kategoriHatasi: String? = null,
    val varsayilanlar: List<RotaYaniti>? = null,
    val secilenSureSaat: Int? = null,
    val seciliTurler: Set<String> = emptySet(),
    val ozelSonuc: RotaYaniti? = null,
    // Konum + varsayılan rotalar İKİSİ birden tamamlanınca (ya da hata olursa)
    // true olur — galeri, bu true olana kadar HİÇ çizilmez (bkz. RotaScreen).
    val ilkYuklemeTamamlandi: Boolean = false,
    val ilkYuklemeHatasi: String? = null,
    val yukleniyorOzel: Boolean = false,
    val hata: String? = null,
    val uyari: String? = null,
    val gosterilenRota: RotaYaniti? = null,
)

class RotaViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(RotaUiState())
    val state: StateFlow<RotaUiState> = _state.asStateFlow()

    /** Son bildirilen konum izni sonucu; null = henüz sonuçlanmadı (konum İSTENMEZ). */
    private var sonIzinSonucu: Boolean? = null
    private var ilkYuklemeIsi: Job? = null
    private var kategoriIsi: Job? = null

    init {
        // Kategoriler konuma bağlı değil: hemen yüklenir. Konum + varsayılan
        // rotalar ise konum izni SONUÇLANANA kadar bekler (bkz. konumIzniSonuclandi).
        kategorileriYukle()
    }

    /**
     * RotaScreen'deki KonumIzniEfekti, izin istemi sonuçlandığında (verildi ya
     * da reddedildi) bunu çağırır. Böylece izin diyaloğu açıkken konum
     * istenip varsayılan konuma düşülmez.
     *
     * İlk sonuçta ilk yükleme başlar. Sonraki çağrılarda (ör. döndürmede
     * efekt yeniden çalışır) sonuç AYNIYSA hiçbir şey yapılmaz; DEĞİŞTİYSE
     * (önce reddedilip sonra verildiyse) konum ve rotalar yenilenir.
     */
    fun konumIzniSonuclandi(verildi: Boolean) {
        val onceki = sonIzinSonucu
        sonIzinSonucu = verildi
        when {
            onceki == null -> ilkYuklemeBaslat()
            onceki != verildi -> tekrarDene()
        }
    }

    private fun kategorileriYukle() {
        if (kategoriIsi?.isActive == true) return
        kategoriIsi = viewModelScope.launch {
            _state.update { it.copy(kategorilerYukleniyor = true, kategoriHatasi = null) }
            try {
                val kategoriler = repo.rotaKategorileriGetir()
                _state.update { it.copy(kategoriler = kategoriler, kategorilerYukleniyor = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota kategorileri yüklenemedi: ${e.logOzeti()}" }
                _state.update {
                    it.copy(kategorilerYukleniyor = false, kategoriHatasi = Metinler.ROTA_KATEGORILER_YUKLENEMEDI)
                }
            }
        }
    }

    /** Konum alma + varsayılan rotaları çekme: tek bir tam ekran yükleniyor/hata katmanının kaynağı. */
    private fun ilkYuklemeBaslat() {
        // Önceki deneme hâlâ sürüyorsa iptal: eski (ör. varsayılan) konumla
        // gelen geç bir cevap yenisinin üstüne yazmasın.
        ilkYuklemeIsi?.cancel()
        ilkYuklemeIsi = viewModelScope.launch {
            _state.update { it.copy(ilkYuklemeTamamlandi = false, ilkYuklemeHatasi = null) }
            val konum = runCatching { guncelKonumAl() }.getOrDefault(VARSAYILAN_KONUM)
            Logger.d { "rota ilk yükleme: ${if (konum == VARSAYILAN_KONUM) "varsayılan konum" else "cihaz konumu"}" }
            try {
                val rotalar = repo.varsayilanRotalariGetir(konum.enlem, konum.boylam)
                _state.update { it.copy(konum = konum, varsayilanlar = rotalar, ilkYuklemeTamamlandi = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "varsayılan rotalar yüklenemedi: ${e.logOzeti()}" }
                _state.update {
                    it.copy(
                        konum = konum,
                        ilkYuklemeTamamlandi = true,
                        ilkYuklemeHatasi = Metinler.ROTALAR_YUKLENEMEDI,
                    )
                }
            }
        }
    }

    /** Konum + rotaları yeniden çeker; kategoriler gelmemişse onları da. */
    fun tekrarDene() {
        if (_state.value.kategoriler == null) kategorileriYukle()
        ilkYuklemeBaslat()
    }

    /** Yalnızca kategorileri yeniden çeker (galerideki satır içi "Tekrar dene"). */
    fun kategorileriTekrarDene() {
        kategorileriYukle()
    }

    fun sureSec(saat: Int) {
        _state.update { it.copy(secilenSureSaat = if (it.secilenSureSaat == saat) null else saat) }
    }

    fun turSec(kod: String) {
        _state.update { st ->
            val mevcut = st.seciliTurler
            when {
                kod in mevcut -> st.copy(seciliTurler = mevcut - kod)
                mevcut.size >= MAKS_SECILI_TUR -> st.copy(uyari = Metinler.rotaEnFazlaTur(MAKS_SECILI_TUR))
                else -> st.copy(seciliTurler = mevcut + kod)
            }
        }
    }

    fun uyariTemizle() {
        _state.update { it.copy(uyari = null) }
    }

    fun ara() {
        val sureSaat = _state.value.secilenSureSaat ?: return
        val turler = _state.value.seciliTurler.toList()
        viewModelScope.launch {
            _state.update { it.copy(yukleniyorOzel = true, hata = null) }
            val konum = _state.value.konum ?: runCatching { guncelKonumAl() }.getOrDefault(VARSAYILAN_KONUM)
            Logger.d { "rota oluşturuluyor (sureSaat=$sureSaat, turler=$turler)" }
            try {
                val yanit = repo.rotaOlustur(konum.enlem, konum.boylam, sureSaat, turler)
                Logger.d { "rota oluşturuldu: ${yanit.rota.size} durak" }
                _state.update { it.copy(konum = konum, ozelSonuc = yanit, yukleniyorOzel = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota oluşturulamadı: ${e.logOzeti()}" }
                _state.update { it.copy(konum = konum, yukleniyorOzel = false, hata = Metinler.hataMesaji(e)) }
            }
        }
    }

    fun rotaGoster(rota: RotaYaniti) {
        _state.update { it.copy(gosterilenRota = rota) }
    }

    fun detaydanCik() {
        _state.update { it.copy(gosterilenRota = null) }
    }
}
