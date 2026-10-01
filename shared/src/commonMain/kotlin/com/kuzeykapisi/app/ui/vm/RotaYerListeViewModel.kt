package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RotaYerListeUiState(
    val yukleniyor: Boolean = true,
    val hata: String? = null,
    val mekanlar: List<RotaMekaniAdmin> = emptyList(),
    val silinecekMekan: RotaMekaniAdmin? = null,
    val silmeYukleniyor: Boolean = false,
    val silmeHatasi: String? = null,
    val duzenlemeYukleniyorId: Int? = null,
    val duzenlemeHatasi: String? = null,
    val oturumGecersiz: Boolean = false,
)

/** İlk liste yüklemesi ViewModel oluşurken bir kez yapılır (döndürmede tekrarlanmaz). */
class RotaYerListeViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(RotaYerListeUiState())
    val state: StateFlow<RotaYerListeUiState> = _state.asStateFlow()

    init {
        yukle()
    }

    fun yukle() {
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null) }
            try {
                val mekanlar = repo.rotaYerleriListele()
                _state.update { it.copy(mekanlar = mekanlar, yukleniyor = false) }
            } catch (e: AdminApiHatasi) {
                Logger.d { "rota-yerleri listesi hatası: ${e.logOzeti()}" }
                _state.update { st ->
                    if (e.httpKodu == 401) {
                        st.copy(yukleniyor = false, oturumGecersiz = true)
                    } else {
                        st.copy(yukleniyor = false, hata = Metinler.adminHataMesaji(e))
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota-yerleri listesi hatası: ${e.logOzeti()}" }
                _state.update {
                    it.copy(
                        yukleniyor = false,
                        hata = Metinler.LISTE_YUKLENEMEDI,
                    )
                }
            }
        }
    }

    /**
     * Düzenle ikonuna basılınca ÖNCE anlatim metni çekilir (liste zaten
     * ad/enlem/boylam/sureDk/aciklama'yı biliyor, o alanlar DEĞİŞMEZ; yalnızca
     * anlatim eksikti) — küçük yükleniyor göstergesi bu sırada gösterilir,
     * yalnızca başarılı olursa `onHazir` çağrılır.
     */
    fun duzenlemeyiBaslat(mekan: RotaMekaniAdmin, onHazir: (RotaMekaniAdmin, String?) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(duzenlemeYukleniyorId = mekan.id, duzenlemeHatasi = null) }
            try {
                val detay = repo.rotaYeriGetir(mekanId = mekan.id)
                _state.update { it.copy(duzenlemeYukleniyorId = null) }
                onHazir(mekan, detay.anlatim)
            } catch (e: AdminApiHatasi) {
                Logger.d { "rota-yeri-getir hatası: ${e.logOzeti()}" }
                _state.update { st ->
                    if (e.httpKodu == 401) {
                        st.copy(duzenlemeYukleniyorId = null, oturumGecersiz = true)
                    } else {
                        st.copy(
                            duzenlemeYukleniyorId = null,
                            duzenlemeHatasi = Metinler.adminHataMesaji(e),
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota-yeri-getir hatası: ${e.logOzeti()}" }
                _state.update {
                    it.copy(
                        duzenlemeYukleniyorId = null,
                        duzenlemeHatasi = Metinler.ICERIK_YUKLENEMEDI,
                    )
                }
            }
        }
    }

    fun silmeyiBaslat(mekan: RotaMekaniAdmin) {
        _state.update { it.copy(silinecekMekan = mekan, silmeHatasi = null) }
    }

    fun silmeyiVazgec() {
        _state.update { it.copy(silinecekMekan = null) }
    }

    fun silmeyiOnayla() {
        val mekan = _state.value.silinecekMekan ?: return
        viewModelScope.launch {
            _state.update { it.copy(silmeYukleniyor = true, silmeHatasi = null) }
            try {
                repo.rotaYeriSil(mekanId = mekan.id)
                _state.update { it.copy(silmeYukleniyor = false, silinecekMekan = null) }
                yukle()
            } catch (e: AdminApiHatasi) {
                Logger.d { "rota-yer-sil hatası: ${e.logOzeti()}" }
                _state.update { st ->
                    if (e.httpKodu == 401) {
                        st.copy(silmeYukleniyor = false, oturumGecersiz = true)
                    } else {
                        st.copy(silmeYukleniyor = false, silmeHatasi = Metinler.adminHataMesaji(e))
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota-yer-sil hatası: ${e.logOzeti()}" }
                _state.update {
                    it.copy(
                        silmeYukleniyor = false,
                        silmeHatasi = Metinler.hataMesaji(e),
                    )
                }
            }
        }
    }
}
