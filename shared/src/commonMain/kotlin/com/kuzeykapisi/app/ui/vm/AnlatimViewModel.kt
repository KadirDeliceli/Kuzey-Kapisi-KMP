package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.data.tts.AnlatimOynatici
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnlatimUiState(
    val metin: String? = null,
    val yukleniyor: Boolean = true,
    val hata: String? = null,
)

class AnlatimViewModel(
    private val repo: KuzeyRepository,
    private val kategori: String,
    private val kod: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(AnlatimUiState())
    val state: StateFlow<AnlatimUiState> = _state.asStateFlow()

    val oynatici = AnlatimOynatici()

    fun yukle() {
        scope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            try {
                val metin = repo.anlatimGetir(kategori, kod)
                _state.value = _state.value.copy(metin = metin, yukleniyor = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(yukleniyor = false, hata = "Anlatım yüklenemedi.")
            }
        }
    }

    fun temizle() {
        oynatici.durdur()
        oynatici.serbestBirak()
    }
}
