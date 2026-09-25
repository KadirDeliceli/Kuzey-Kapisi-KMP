package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

/** [ilkToken]: ilk liste yüklemesi ViewModel oluşurken bir kez yapılır (döndürmede tekrarlanmaz). */
class RotaYerListeViewModel(private val repo: KuzeyRepository, ilkToken: String) : ViewModel() {
    private val _state = MutableStateFlow(RotaYerListeUiState())
    val state: StateFlow<RotaYerListeUiState> = _state.asStateFlow()

    init {
        yukle(ilkToken)
    }

    fun yukle(token: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            try {
                val mekanlar = repo.rotaYerleriListele(token)
                _state.value = _state.value.copy(mekanlar = mekanlar, yukleniyor = false)
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] rota-yerleri listesi hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(yukleniyor = false, oturumGecersiz = true)
                } else {
                    _state.value.copy(yukleniyor = false, hata = e.detay)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                println("[KuzeyKapisi] rota-yerleri listesi ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    yukleniyor = false,
                    hata = "Liste yüklenemedi, lütfen tekrar deneyin.",
                )
            }
        }
    }

    /**
     * Düzenle ikonuna basılınca ÖNCE anlatim metni çekilir (liste zaten
     * ad/enlem/boylam/sureDk/aciklama'yı biliyor, o alanlar DEĞİŞMEZ; yalnızca
     * anlatim eksikti) — küçük yükleniyor göstergesi bu sırada gösterilir,
     * yalnızca başarılı olursa `onHazir` çağrılır.
     */
    fun duzenlemeyiBaslat(mekan: RotaMekaniAdmin, token: String, onHazir: (RotaMekaniAdmin, String?) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(duzenlemeYukleniyorId = mekan.id, duzenlemeHatasi = null)
            try {
                val detay = repo.rotaYeriGetir(mekanId = mekan.id, token = token)
                _state.value = _state.value.copy(duzenlemeYukleniyorId = null)
                onHazir(mekan, detay.anlatim)
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] rota-yeri-getir hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(duzenlemeYukleniyorId = null, oturumGecersiz = true)
                } else {
                    _state.value.copy(
                        duzenlemeYukleniyorId = null,
                        duzenlemeHatasi = "Bu içerik yüklenemedi: ${e.detay}",
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                println("[KuzeyKapisi] rota-yeri-getir ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    duzenlemeYukleniyorId = null,
                    duzenlemeHatasi = "İçerik yüklenemedi, lütfen tekrar deneyin.",
                )
            }
        }
    }

    fun silmeyiBaslat(mekan: RotaMekaniAdmin) {
        _state.value = _state.value.copy(silinecekMekan = mekan, silmeHatasi = null)
    }

    fun silmeyiVazgec() {
        _state.value = _state.value.copy(silinecekMekan = null)
    }

    fun silmeyiOnayla(token: String) {
        val mekan = _state.value.silinecekMekan ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(silmeYukleniyor = true, silmeHatasi = null)
            try {
                repo.rotaYeriSil(token = token, mekanId = mekan.id)
                _state.value = _state.value.copy(silmeYukleniyor = false, silinecekMekan = null)
                yukle(token)
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] rota-yer-sil hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(silmeYukleniyor = false, oturumGecersiz = true)
                } else {
                    _state.value.copy(silmeYukleniyor = false, silmeHatasi = e.detay)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                println("[KuzeyKapisi] rota-yer-sil ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    silmeYukleniyor = false,
                    silmeHatasi = "Ağ hatası: lütfen bağlantınızı kontrol edip tekrar deneyin.",
                )
            }
        }
    }
}
