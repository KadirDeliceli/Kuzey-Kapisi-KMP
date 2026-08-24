package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PersonaListeUiState(
    val kategori: String = "kisiler",
    val yukleniyor: Boolean = true,
    val hata: String? = null,
    val katalog: Katalog = emptyMap(),
    val silinecekOge: KatalogOge? = null,
    val silmeYukleniyor: Boolean = false,
    val silmeHatasi: String? = null,
    val duzenlemeYukleniyorKod: String? = null,
    val duzenlemeHatasi: String? = null,
    val oturumGecersiz: Boolean = false,
)

class PersonaListeViewModel(private val repo: KuzeyRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(PersonaListeUiState())
    val state: StateFlow<PersonaListeUiState> = _state.asStateFlow()

    fun yukle() {
        scope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            try {
                val katalog = repo.katalog()
                _state.value = _state.value.copy(katalog = katalog, yukleniyor = false)
            } catch (e: Exception) {
                println("[KuzeyKapisi] persona listesi yüklenemedi: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    yukleniyor = false,
                    hata = "Liste yüklenemedi, lütfen tekrar deneyin.",
                )
            }
        }
    }

    fun kategoriSec(kategori: String) {
        _state.value = _state.value.copy(kategori = kategori)
    }

    /**
     * Düzenle ikonuna basılınca ÖNCE mevcut içerik çekilir (küçük yükleniyor
     * göstergesi bu sırada gösterilir), yalnızca başarılı olursa `onHazir`
     * çağrılır ve çağıran taraf düzenleme ekranını açar. Başarısız olursa
     * form hiç açılmaz, hata bu ekranda gösterilir.
     */
    fun duzenlemeyiBaslat(oge: KatalogOge, token: String, onHazir: (PersonaDetay) -> Unit) {
        val kategori = _state.value.kategori
        scope.launch {
            _state.value = _state.value.copy(duzenlemeYukleniyorKod = oge.kod, duzenlemeHatasi = null)
            try {
                val detay = repo.personaGetir(kategori = kategori, kod = oge.kod, token = token)
                _state.value = _state.value.copy(duzenlemeYukleniyorKod = null)
                onHazir(detay)
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] persona-getir hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(duzenlemeYukleniyorKod = null, oturumGecersiz = true)
                } else {
                    _state.value.copy(
                        duzenlemeYukleniyorKod = null,
                        duzenlemeHatasi = "Bu içerik yüklenemedi: ${e.detay}",
                    )
                }
            } catch (e: Exception) {
                println("[KuzeyKapisi] persona-getir ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    duzenlemeYukleniyorKod = null,
                    duzenlemeHatasi = "İçerik yüklenemedi, lütfen tekrar deneyin.",
                )
            }
        }
    }

    fun silmeyiBaslat(oge: KatalogOge) {
        _state.value = _state.value.copy(silinecekOge = oge, silmeHatasi = null)
    }

    fun silmeyiVazgec() {
        _state.value = _state.value.copy(silinecekOge = null)
    }

    fun silmeyiOnayla(token: String) {
        val oge = _state.value.silinecekOge ?: return
        val kategori = _state.value.kategori
        scope.launch {
            _state.value = _state.value.copy(silmeYukleniyor = true, silmeHatasi = null)
            try {
                repo.personaSil(token = token, kategori = kategori, kod = oge.kod)
                _state.value = _state.value.copy(silmeYukleniyor = false, silinecekOge = null)
                yukle()
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] persona-sil hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(silmeYukleniyor = false, oturumGecersiz = true)
                } else {
                    _state.value.copy(silmeYukleniyor = false, silmeHatasi = e.detay)
                }
            } catch (e: Exception) {
                println("[KuzeyKapisi] persona-sil ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    silmeYukleniyor = false,
                    silmeHatasi = "Ağ hatası: lütfen bağlantınızı kontrol edip tekrar deneyin.",
                )
            }
        }
    }
}
