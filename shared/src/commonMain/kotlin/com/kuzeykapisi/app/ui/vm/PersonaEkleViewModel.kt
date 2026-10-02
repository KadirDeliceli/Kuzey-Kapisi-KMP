package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.media.SecilenResim
import com.kuzeykapisi.app.data.media.resimSec
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.domain.slugify
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonaEkleUiState(
    val kategori: String = "kisiler",
    val ad: String = "",
    val kod: String = "",
    val kodElleDuzenlendi: Boolean = false,
    val karsilama: String = "",
    val icerik: String = "",
    val anlatim: String = "",
    val gorsel: SecilenResim? = null,
    val kaydediliyor: Boolean = false,
    val genelHata: String? = null,
    val kodHatasi: String? = null,
    val basariMesaji: String? = null,
    val oturumGecersiz: Boolean = false,
)

/**
 * Backend'e gidecek persona kodu: [elleGirilen] boş değilse o, boşsa [ad];
 * her iki durumda da [slugify] ile normalize edilir. Normalize edilmiş
 * sonuç boşsa (ör. ad yalnızca noktalama işaretiyse) null — çağıran kaydı
 * durdurur.
 */
internal fun personaKoduBelirle(elleGirilen: String, ad: String): String? {
    val kaynak = elleGirilen.ifBlank { ad }
    return slugify(kaynak).ifEmpty { null }
}

class PersonaEkleViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(PersonaEkleUiState())
    val state: StateFlow<PersonaEkleUiState> = _state.asStateFlow()

    fun kategoriSec(kategori: String) {
        _state.update { it.copy(kategori = kategori) }
    }

    fun adDegisti(yeniAd: String) {
        _state.update { mevcut ->
            val yeniKod = if (mevcut.kodElleDuzenlendi) mevcut.kod else slugify(yeniAd)
            mevcut.copy(ad = yeniAd, kod = yeniKod)
        }
    }

    /** Alan tamamen silinirse kod yeniden Ad'dan otomatik üretilir. */
    fun kodDegisti(yeniKod: String) {
        _state.update {
            it.copy(
                kod = yeniKod,
                kodElleDuzenlendi = yeniKod.isNotBlank(),
                kodHatasi = null,
            )
        }
    }

    fun karsilamaDegisti(v: String) {
        _state.update { it.copy(karsilama = v) }
    }

    fun icerikDegisti(v: String) {
        _state.update { it.copy(icerik = v) }
    }

    fun anlatimDegisti(v: String) {
        _state.update { it.copy(anlatim = v) }
    }

    fun gorselSec() {
        // Görsel baytları okunurken UI thread'i bloklanmasın diye (önceki davranış gibi) arka planda.
        viewModelScope.launch(Dispatchers.Default) {
            val secilen = runCatching { resimSec() }.getOrNull()
            if (secilen != null) _state.update { it.copy(gorsel = secilen) }
        }
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
        if (s.gorsel == null) {
            _state.update { it.copy(genelHata = Metinler.FORM_GORSEL_GEREKLI) }
            return
        }
        // Gönderilecek kod her zaman burada, kaydet anında belirlenir: alan
        // boşsa (elle silinmiş olsa bile) Ad'dan, doluysa elle girilenden —
        // ikisinde de slugify ile normalize edilerek. Boş kod asla gitmez.
        val kod = personaKoduBelirle(elleGirilen = s.kod, ad = s.ad)
        if (kod == null) {
            _state.update { it.copy(kodHatasi = Metinler.FORM_KOD_URETILEMEDI) }
            return
        }
        viewModelScope.launch {
            // Kullanıcı gönderilen kodu görsün (ör. "zaten var" hatasında neyi değiştireceğini bilsin).
            _state.update { it.copy(kod = kod, kaydediliyor = true, genelHata = null, kodHatasi = null) }
            try {
                val yanit = repo.personaEkle(
                    kategori = s.kategori,
                    ad = s.ad.trim(),
                    kod = kod,
                    karsilama = s.karsilama.trim(),
                    icerik = s.icerik.trim(),
                    anlatim = s.anlatim.trim().ifBlank { null },
                    gorsel = s.gorsel,
                )
                _state.value = PersonaEkleUiState(
                    kategori = s.kategori,
                    basariMesaji = Metinler.eklendi(yanit.ad),
                )
            } catch (e: AdminApiHatasi) {
                Logger.d { "persona-ekle hatası: ${e.logOzeti()}" }
                _state.update { st ->
                    if (e.detay?.contains("zaten var") == true) {
                        st.copy(kaydediliyor = false, kodHatasi = Metinler.kodZatenVar(e.detay.orEmpty()))
                    } else {
                        when (val sonuc = adminHatasiDegerlendir(e)) {
                            AdminHataSonucu.OturumGecersiz -> st.copy(kaydediliyor = false, oturumGecersiz = true)
                            is AdminHataSonucu.Mesaj -> st.copy(kaydediliyor = false, genelHata = sonuc.metin)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "persona-ekle hatası: ${e.logOzeti()}" }
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
