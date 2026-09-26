package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.model.RotaYerEkleIstek
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RotaYerEkleUiState(
    val ad: String = "",
    val enlem: String = "",
    val boylam: String = "",
    val sureDk: String = "",
    val aciklama: String = "",
    val anlatim: String = "",
    val kaydediliyor: Boolean = false,
    val genelHata: String? = null,
    val basariMesaji: String? = null,
    val oturumGecersiz: Boolean = false,
)

class RotaYerEkleViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(RotaYerEkleUiState())
    val state: StateFlow<RotaYerEkleUiState> = _state.asStateFlow()

    fun adDegisti(v: String) { _state.value = _state.value.copy(ad = v) }
    fun enlemDegisti(v: String) { _state.value = _state.value.copy(enlem = v) }
    fun boylamDegisti(v: String) { _state.value = _state.value.copy(boylam = v) }
    fun sureDkDegisti(v: String) { _state.value = _state.value.copy(sureDk = v) }
    fun aciklamaDegisti(v: String) { _state.value = _state.value.copy(aciklama = v) }
    fun anlatimDegisti(v: String) { _state.value = _state.value.copy(anlatim = v) }

    fun kaydet(token: String) {
        val s = _state.value
        val enlem = s.enlem.trim().replace(',', '.').toDoubleOrNull()
        val boylam = s.boylam.trim().replace(',', '.').toDoubleOrNull()
        val sureDk = s.sureDk.trim().toIntOrNull()
        if (s.ad.isBlank() || s.aciklama.isBlank()) {
            _state.value = s.copy(genelHata = Metinler.FORM_MEKAN_ZORUNLU_ALANLAR)
            return
        }
        if (enlem == null || boylam == null) {
            _state.value = s.copy(genelHata = Metinler.FORM_KOORDINAT_GECERSIZ)
            return
        }
        if (sureDk == null || sureDk <= 0) {
            _state.value = s.copy(genelHata = Metinler.FORM_SURE_GECERSIZ)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(kaydediliyor = true, genelHata = null)
            try {
                repo.rotaYerEkle(
                    token = token,
                    istek = RotaYerEkleIstek(
                        ad = s.ad.trim(),
                        enlem = enlem,
                        boylam = boylam,
                        sureDk = sureDk,
                        aciklama = s.aciklama.trim(),
                        anlatim = s.anlatim.trim().ifBlank { null },
                    ),
                )
                _state.value = RotaYerEkleUiState(basariMesaji = Metinler.eklendi(s.ad.trim()))
            } catch (e: AdminApiHatasi) {
                Logger.d { "rota-yer-ekle hatası: ${e.logOzeti()}" }
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(kaydediliyor = false, oturumGecersiz = true)
                } else {
                    _state.value.copy(kaydediliyor = false, genelHata = Metinler.adminHataMesaji(e))
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "rota-yer-ekle hatası: ${e.logOzeti()}" }
                _state.value = _state.value.copy(
                    kaydediliyor = false,
                    genelHata = Metinler.hataMesaji(e),
                )
            }
        }
    }
}
