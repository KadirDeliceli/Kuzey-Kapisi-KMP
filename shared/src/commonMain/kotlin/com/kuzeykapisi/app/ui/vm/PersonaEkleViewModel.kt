package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.model.SecilenResim
import com.kuzeykapisi.app.data.model.resimSec
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.domain.slugify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PersonaEkleUiState(
    val kategori: String = "kisiler",
    val ad: String = "",
    val kod: String = "",
    val kodElleDuzenlendi: Boolean = false,
    val karsilama: String = "",
    val icerik: String = "",
    val gorsel: SecilenResim? = null,
    val kaydediliyor: Boolean = false,
    val genelHata: String? = null,
    val kodHatasi: String? = null,
    val basariMesaji: String? = null,
    val oturumGecersiz: Boolean = false,
)

class PersonaEkleViewModel(private val repo: KuzeyRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(PersonaEkleUiState())
    val state: StateFlow<PersonaEkleUiState> = _state.asStateFlow()

    fun kategoriSec(kategori: String) {
        _state.value = _state.value.copy(kategori = kategori)
    }

    fun adDegisti(yeniAd: String) {
        val mevcut = _state.value
        val yeniKod = if (mevcut.kodElleDuzenlendi) mevcut.kod else slugify(yeniAd)
        _state.value = mevcut.copy(ad = yeniAd, kod = yeniKod)
    }

    fun kodDegisti(yeniKod: String) {
        _state.value = _state.value.copy(kod = yeniKod, kodElleDuzenlendi = true, kodHatasi = null)
    }

    fun karsilamaDegisti(v: String) {
        _state.value = _state.value.copy(karsilama = v)
    }

    fun icerikDegisti(v: String) {
        _state.value = _state.value.copy(icerik = v)
    }

    fun gorselSec() {
        scope.launch {
            val secilen = runCatching { resimSec() }.getOrNull()
            if (secilen != null) _state.value = _state.value.copy(gorsel = secilen)
        }
    }

    fun kaydet(token: String) {
        val s = _state.value
        if (s.ad.isBlank() || s.karsilama.isBlank() || s.icerik.isBlank()) {
            _state.value = s.copy(
                genelHata = "'Ad', 'Açılış Mesajı' ve 'Detaylı İçerik' alanları boş olamaz.",
            )
            return
        }
        if (s.gorsel == null) {
            _state.value = s.copy(genelHata = "Lütfen bir görsel seçin.")
            return
        }
        scope.launch {
            _state.value = _state.value.copy(kaydediliyor = true, genelHata = null, kodHatasi = null)
            try {
                val yanit = repo.personaEkle(
                    token = token,
                    kategori = s.kategori,
                    ad = s.ad.trim(),
                    kod = s.kod.trim(),
                    karsilama = s.karsilama.trim(),
                    icerik = s.icerik.trim(),
                    gorsel = s.gorsel,
                )
                _state.value = PersonaEkleUiState(
                    kategori = s.kategori,
                    basariMesaji = "Eklendi: ${yanit.ad}",
                )
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] persona-ekle hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = when {
                    e.httpKodu == 401 -> _state.value.copy(kaydediliyor = false, oturumGecersiz = true)
                    e.detay.contains("zaten var") -> _state.value.copy(
                        kaydediliyor = false,
                        kodHatasi = "${e.detay} Lütfen yukarıdaki 'Kod' alanını değiştirip tekrar deneyin.",
                    )
                    else -> _state.value.copy(kaydediliyor = false, genelHata = e.detay)
                }
            } catch (e: Exception) {
                println("[KuzeyKapisi] persona-ekle ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    kaydediliyor = false,
                    genelHata = "Ağ hatası: lütfen bağlantınızı kontrol edip tekrar deneyin.",
                )
            }
        }
    }
}
