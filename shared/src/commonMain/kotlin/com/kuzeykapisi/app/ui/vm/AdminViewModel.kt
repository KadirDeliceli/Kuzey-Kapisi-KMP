package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val token: String? = null,
    val yukleniyor: Boolean = false,
    val hata: String? = null,
)

/**
 * Token yalnızca bellekte tutulur (kalıcı depolama yok) — uygulama yeniden
 * açılınca tekrar giriş istenmesi kasıtlıdır.
 */
class AdminViewModel(private val repo: KuzeyRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    fun girisYap(kullaniciAdi: String, sifre: String) {
        if (kullaniciAdi.isBlank() || sifre.isBlank()) {
            _state.value = _state.value.copy(hata = "Kullanıcı adı ve şifre gerekli.")
            return
        }
        scope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            try {
                val token = repo.adminGiris(kullaniciAdi.trim(), sifre)
                _state.value = _state.value.copy(token = token, yukleniyor = false, hata = null)
            } catch (e: Exception) {
                println("[KuzeyKapisi] admin girişi başarısız: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    yukleniyor = false,
                    hata = "Kullanıcı adı veya şifre hatalı.",
                )
            }
        }
    }

    fun hataTemizle() {
        _state.value = _state.value.copy(hata = null)
    }

    fun oturumuSifirla() {
        _state.value = _state.value.copy(token = null)
    }
}
