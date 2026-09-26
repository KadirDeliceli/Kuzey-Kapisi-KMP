package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.model.KategoriBilgi
import com.kuzeykapisi.app.data.model.Konum
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.model.VARSAYILAN_KONUM
import com.kuzeykapisi.app.data.model.guncelKonumAl
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MAKS_SECILI_TUR = 4

data class RotaUiState(
    val konum: Konum? = null,
    val kategoriler: Map<String, KategoriBilgi>? = null,
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

    init {
        basla()
    }

    private fun basla() {
        viewModelScope.launch {
            try {
                val kategoriler = repo.rotaKategorileriGetir()
                _state.value = _state.value.copy(kategoriler = kategoriler)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota kategorileri yüklenemedi: ${e.logOzeti()}" }
                _state.value = _state.value.copy(hata = Metinler.ROTA_KATEGORILER_YUKLENEMEDI)
            }
        }
        ilkYuklemeBaslat()
    }

    /** Konum alma + varsayılan rotaları çekme: tek bir tam ekran yükleniyor/hata katmanının kaynağı. */
    private fun ilkYuklemeBaslat() {
        viewModelScope.launch {
            _state.value = _state.value.copy(ilkYuklemeTamamlandi = false, ilkYuklemeHatasi = null)
            val konum = runCatching { guncelKonumAl() }.getOrDefault(VARSAYILAN_KONUM)
            try {
                val rotalar = repo.varsayilanRotalariGetir(konum.enlem, konum.boylam)
                _state.value = _state.value.copy(
                    konum = konum,
                    varsayilanlar = rotalar,
                    ilkYuklemeTamamlandi = true,
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "varsayılan rotalar yüklenemedi: ${e.logOzeti()}" }
                _state.value = _state.value.copy(
                    konum = konum,
                    ilkYuklemeTamamlandi = true,
                    ilkYuklemeHatasi = Metinler.ROTALAR_YUKLENEMEDI,
                )
            }
        }
    }

    fun tekrarDene() {
        ilkYuklemeBaslat()
    }

    fun sureSec(saat: Int) {
        val mevcut = _state.value.secilenSureSaat
        _state.value = _state.value.copy(secilenSureSaat = if (mevcut == saat) null else saat)
    }

    fun turSec(kod: String) {
        val mevcut = _state.value.seciliTurler
        _state.value = when {
            kod in mevcut -> _state.value.copy(seciliTurler = mevcut - kod)
            mevcut.size >= MAKS_SECILI_TUR -> _state.value.copy(uyari = Metinler.rotaEnFazlaTur(MAKS_SECILI_TUR))
            else -> _state.value.copy(seciliTurler = mevcut + kod)
        }
    }

    fun uyariTemizle() {
        _state.value = _state.value.copy(uyari = null)
    }

    fun ara() {
        val sureSaat = _state.value.secilenSureSaat ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(yukleniyorOzel = true, hata = null)
            val konum = _state.value.konum ?: runCatching { guncelKonumAl() }.getOrDefault(VARSAYILAN_KONUM)
            Logger.d { "rota oluşturuluyor (sureSaat=$sureSaat, turler=${_state.value.seciliTurler})" }
            try {
                val yanit = repo.rotaOlustur(konum.enlem, konum.boylam, sureSaat, _state.value.seciliTurler.toList())
                Logger.d { "rota oluşturuldu: ${yanit.rota.size} durak" }
                _state.value = _state.value.copy(konum = konum, ozelSonuc = yanit, yukleniyorOzel = false)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota oluşturulamadı: ${e.logOzeti()}" }
                _state.value = _state.value.copy(
                    konum = konum,
                    yukleniyorOzel = false,
                    hata = Metinler.hataMesaji(e),
                )
            }
        }
    }

    fun rotaGoster(rota: RotaYaniti) {
        _state.value = _state.value.copy(gosterilenRota = rota)
    }

    fun detaydanCik() {
        _state.value = _state.value.copy(gosterilenRota = null)
    }
}
