package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.model.PersonaDetay
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

class PersonaListeViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(PersonaListeUiState())
    val state: StateFlow<PersonaListeUiState> = _state.asStateFlow()

    init {
        yukle()
    }

    fun yukle() {
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null) }
            try {
                val katalog = repo.katalog()
                _state.update { it.copy(katalog = katalog, yukleniyor = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "persona listesi yüklenemedi: ${e.logOzeti()}" }
                _state.update {
                    it.copy(
                        yukleniyor = false,
                        hata = Metinler.LISTE_YUKLENEMEDI,
                    )
                }
            }
        }
    }

    fun kategoriSec(kategori: String) {
        _state.update { it.copy(kategori = kategori) }
    }

    /**
     * Düzenle ikonuna basılınca ÖNCE mevcut içerik çekilir (küçük yükleniyor
     * göstergesi bu sırada gösterilir), yalnızca başarılı olursa `onHazir`
     * çağrılır ve çağıran taraf düzenleme ekranını açar. Başarısız olursa
     * form hiç açılmaz, hata bu ekranda gösterilir.
     */
    fun duzenlemeyiBaslat(oge: KatalogOge, onHazir: (PersonaDetay) -> Unit) {
        val kategori = _state.value.kategori
        viewModelScope.launch {
            _state.update { it.copy(duzenlemeYukleniyorKod = oge.kod, duzenlemeHatasi = null) }
            try {
                val detay = repo.personaGetir(kategori = kategori, kod = oge.kod)
                _state.update { it.copy(duzenlemeYukleniyorKod = null) }
                onHazir(detay)
            } catch (e: AdminApiHatasi) {
                Logger.d { "persona-getir hatası: ${e.logOzeti()}" }
                _state.update { st ->
                    when (val sonuc = adminHatasiDegerlendir(e)) {
                        AdminHataSonucu.OturumGecersiz -> st.copy(duzenlemeYukleniyorKod = null, oturumGecersiz = true)
                        is AdminHataSonucu.Mesaj -> st.copy(
                            duzenlemeYukleniyorKod = null,
                            duzenlemeHatasi = sonuc.metin,
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "persona-getir hatası: ${e.logOzeti()}" }
                _state.update {
                    it.copy(
                        duzenlemeYukleniyorKod = null,
                        duzenlemeHatasi = Metinler.ICERIK_YUKLENEMEDI,
                    )
                }
            }
        }
    }

    fun silmeyiBaslat(oge: KatalogOge) {
        _state.update { it.copy(silinecekOge = oge, silmeHatasi = null) }
    }

    fun silmeyiVazgec() {
        _state.update { it.copy(silinecekOge = null) }
    }

    fun silmeyiOnayla() {
        val oge = _state.value.silinecekOge ?: return
        val kategori = _state.value.kategori
        viewModelScope.launch {
            _state.update { it.copy(silmeYukleniyor = true, silmeHatasi = null) }
            try {
                repo.personaSil(kategori = kategori, kod = oge.kod)
                _state.update { it.copy(silmeYukleniyor = false, silinecekOge = null) }
                yukle()
            } catch (e: AdminApiHatasi) {
                Logger.d { "persona-sil hatası: ${e.logOzeti()}" }
                _state.update { st ->
                    when (val sonuc = adminHatasiDegerlendir(e)) {
                        AdminHataSonucu.OturumGecersiz -> st.copy(silmeYukleniyor = false, oturumGecersiz = true)
                        is AdminHataSonucu.Mesaj -> st.copy(silmeYukleniyor = false, silmeHatasi = sonuc.metin)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "persona-sil hatası: ${e.logOzeti()}" }
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
