package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.model.Katalog
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogUiState(
    val katalog: Katalog = emptyMap(),
    val yukleniyor: Boolean = true,
    val hata: String? = null,
)

class CatalogViewModel(private val repo: KuzeyRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    fun yukle() {
        scope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            println("[KuzeyKapisi] GET /katalog isteği başlatılıyor...")
            try {
                val katalog = repo.katalog()
                println("[KuzeyKapisi] /katalog başarılı, ${katalog.size} kategori geldi")
                _state.value = _state.value.copy(katalog = katalog, yukleniyor = false)
            } catch (e: Exception) {
                println("[KuzeyKapisi] katalog() hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(yukleniyor = false, hata = e.message ?: "Katalog yüklenemedi")
            }
        }
    }
}
