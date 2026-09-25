package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogUiState(
    val katalog: Katalog = emptyMap(),
    val yukleniyor: Boolean = true,
    val hata: String? = null,
)

class CatalogViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init {
        yukle()
    }

    fun yukle() {
        viewModelScope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            println("[KuzeyKapisi] GET /katalog isteği başlatılıyor...")
            try {
                val katalog = repo.katalog()
                println("[KuzeyKapisi] /katalog başarılı, ${katalog.size} kategori geldi")
                _state.value = _state.value.copy(katalog = katalog, yukleniyor = false)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                println("[KuzeyKapisi] katalog() hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(yukleniyor = false, hata = e.message ?: "Katalog yüklenemedi")
            }
        }
    }
}
