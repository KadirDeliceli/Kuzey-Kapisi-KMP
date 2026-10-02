package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.media.SecilenResim
import com.kuzeykapisi.app.data.media.resimSec
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonaDuzenleUiState(
    val ad: String,
    val karsilama: String,
    val icerik: String,
    val anlatim: String,
    val anlatimiKaldir: Boolean = false,
    val gorselVar: Boolean,
    val gorsel: SecilenResim? = null,
    val kaydediliyor: Boolean = false,
    val genelHata: String? = null,
    val basariMesaji: String? = null,
    val oturumGecersiz: Boolean = false,
)

/**
 * kategori/kod SABİTTİR. Form artık `initial` (repo.personaGetir ile önceden
 * çekilmiş içerik) ile ÖN-DOLU açılır. anlatim alanı ayrıca `anlatimiKaldir`
 * checkbox'ı ile birlikte, metin ve kaldırma niyeti ayrı form alanları olarak
 * HER İSTEKTE gönderilecek şekilde çalışır — bkz. [anlatimGonderilecek].
 */
class PersonaDuzenleViewModel(
    private val repo: KuzeyRepository,
    val kategori: String,
    val kod: String,
    private val initial: PersonaDetay,
) : ViewModel() {
    private val anlatimOrijinal = initial.anlatim ?: ""
    private val _state = MutableStateFlow(
        PersonaDuzenleUiState(
            ad = initial.ad,
            karsilama = initial.karsilama,
            icerik = initial.icerik,
            anlatim = anlatimOrijinal,
            gorselVar = initial.gorselVar,
        ),
    )
    val state: StateFlow<PersonaDuzenleUiState> = _state.asStateFlow()

    fun adDegisti(v: String) { _state.update { it.copy(ad = v) } }
    fun karsilamaDegisti(v: String) { _state.update { it.copy(karsilama = v) } }
    fun icerikDegisti(v: String) { _state.update { it.copy(icerik = v) } }
    fun anlatimDegisti(v: String) { _state.update { it.copy(anlatim = v) } }
    fun anlatimiKaldirDegisti(v: Boolean) { _state.update { it.copy(anlatimiKaldir = v) } }

    fun gorselSec() {
        // Görsel baytları okunurken UI thread'i bloklanmasın diye (önceki davranış gibi) arka planda.
        viewModelScope.launch(Dispatchers.Default) {
            val secilen = runCatching { resimSec() }.getOrNull()
            if (secilen != null) _state.update { it.copy(gorsel = secilen) }
        }
    }

    private data class AnlatimGonderim(val metin: String, val kaldir: Boolean)

    /**
     * Backend artık "anlatim" ve "anlatim_kaldir" alanlarını HER İSTEKTE
     * ayrı ayrı bekliyor. metin: kullanıcı orijinal değeri değiştirdiyse
     * güncel metin, değiştirmediyse "" (dokunmama niyeti anlatim_kaldir=false
     * ile taşınır). kaldir: checkbox'ın durumu.
     */
    private fun anlatimGonderilecek(s: PersonaDuzenleUiState): AnlatimGonderim {
        val metin = if (s.anlatim.trim() != anlatimOrijinal.trim()) s.anlatim.trim() else ""
        return AnlatimGonderim(metin = metin, kaldir = s.anlatimiKaldir)
    }

    fun kaydet() {
        val s = _state.value
        if (s.ad.isBlank() || s.karsilama.isBlank() || s.icerik.isBlank()) {
            _state.update {
                it.copy(
                    genelHata = Metinler.FORM_PERSONA_ZORUNLU_ALANLAR,
                )
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(kaydediliyor = true, genelHata = null, basariMesaji = null) }
            try {
                val anlatimGonderim = anlatimGonderilecek(s)
                repo.personaGuncelle(
                    kategori = kategori,
                    kod = kod,
                    ad = s.ad.trim(),
                    karsilama = s.karsilama.trim(),
                    icerik = s.icerik.trim(),
                    anlatim = anlatimGonderim.metin,
                    anlatimKaldir = anlatimGonderim.kaldir,
                    gorsel = s.gorsel,
                )
                _state.update { it.copy(kaydediliyor = false, basariMesaji = Metinler.GUNCELLENDI) }
            } catch (e: AdminApiHatasi) {
                Logger.d { "persona-guncelle hatası: ${e.logOzeti()}" }
                _state.update { st ->
                    when (val sonuc = adminHatasiDegerlendir(e)) {
                        AdminHataSonucu.OturumGecersiz -> st.copy(kaydediliyor = false, oturumGecersiz = true)
                        is AdminHataSonucu.Mesaj -> st.copy(kaydediliyor = false, genelHata = sonuc.metin)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "persona-guncelle hatası: ${e.logOzeti()}" }
                _state.update {
                    it.copy(
                        kaydediliyor = false,
                        genelHata = Metinler.hataMesaji(e),
                    )
                }
            }
        }
    }
}
