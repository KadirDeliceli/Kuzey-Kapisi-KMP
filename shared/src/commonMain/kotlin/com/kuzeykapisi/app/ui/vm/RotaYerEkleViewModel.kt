package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.model.RotaYerEkleIstek
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RotaYerEkleUiState(
    val ad: String = "",
    val enlem: String = "",
    val boylam: String = "",
    val sureDk: String = "",
    val tur: String = "",
    val aciklama: String = "",
    val kaydediliyor: Boolean = false,
    val genelHata: String? = null,
    val basariMesaji: String? = null,
    val oturumGecersiz: Boolean = false,
)

class RotaYerEkleViewModel(private val repo: KuzeyRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(RotaYerEkleUiState())
    val state: StateFlow<RotaYerEkleUiState> = _state.asStateFlow()

    fun adDegisti(v: String) { _state.value = _state.value.copy(ad = v) }
    fun enlemDegisti(v: String) { _state.value = _state.value.copy(enlem = v) }
    fun boylamDegisti(v: String) { _state.value = _state.value.copy(boylam = v) }
    fun sureDkDegisti(v: String) { _state.value = _state.value.copy(sureDk = v) }
    fun turDegisti(v: String) { _state.value = _state.value.copy(tur = v) }
    fun aciklamaDegisti(v: String) { _state.value = _state.value.copy(aciklama = v) }

    fun kaydet(token: String) {
        val s = _state.value
        val enlem = s.enlem.trim().replace(',', '.').toDoubleOrNull()
        val boylam = s.boylam.trim().replace(',', '.').toDoubleOrNull()
        val sureDk = s.sureDk.trim().toIntOrNull()
        if (s.ad.isBlank() || s.tur.isBlank() || s.aciklama.isBlank()) {
            _state.value = s.copy(genelHata = "'Ad', 'Tür' ve 'Açıklama' alanları boş olamaz.")
            return
        }
        if (enlem == null || boylam == null) {
            _state.value = s.copy(genelHata = "'Enlem' ve 'Boylam' geçerli birer sayı olmalı.")
            return
        }
        if (sureDk == null || sureDk <= 0) {
            _state.value = s.copy(genelHata = "'Ziyaret Süresi' sıfırdan büyük bir tam sayı olmalı.")
            return
        }
        scope.launch {
            _state.value = _state.value.copy(kaydediliyor = true, genelHata = null)
            try {
                repo.rotaYerEkle(
                    token = token,
                    istek = RotaYerEkleIstek(
                        ad = s.ad.trim(),
                        enlem = enlem,
                        boylam = boylam,
                        sureDk = sureDk,
                        tur = s.tur.trim(),
                        aciklama = s.aciklama.trim(),
                    ),
                )
                _state.value = RotaYerEkleUiState(basariMesaji = "Eklendi: ${s.ad.trim()}")
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] rota-yer-ekle hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(kaydediliyor = false, oturumGecersiz = true)
                } else {
                    _state.value.copy(kaydediliyor = false, genelHata = e.detay)
                }
            } catch (e: Exception) {
                println("[KuzeyKapisi] rota-yer-ekle ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    kaydediliyor = false,
                    genelHata = "Ağ hatası: lütfen bağlantınızı kontrol edip tekrar deneyin.",
                )
            }
        }
    }
}
