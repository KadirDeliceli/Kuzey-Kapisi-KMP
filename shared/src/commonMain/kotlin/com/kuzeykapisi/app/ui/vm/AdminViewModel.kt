package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
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
 * açılınca tekrar giriş istenmesi kasıtlıdır. Uygulama düzeyinde viewModel {}
 * ile alındığı için yapılandırma değişikliğinden (döndürme) sağ çıkar.
 */
class AdminViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    fun girisYap(kullaniciAdi: String, sifre: String) {
        if (kullaniciAdi.isBlank() || sifre.isBlank()) {
            _state.value = _state.value.copy(hata = Metinler.GIRIS_BILGI_EKSIK)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            try {
                val token = repo.adminGiris(kullaniciAdi.trim(), sifre)
                _state.value = _state.value.copy(token = token, yukleniyor = false, hata = null)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "admin girişi başarısız: ${e.logOzeti()}" }
                // Yalnızca gerçek 401 "şifre hatalı" demektir; sunucu kapalıyken
                // kullanıcıya şifresinin yanlış olduğu söylenmez.
                _state.value = _state.value.copy(
                    yukleniyor = false,
                    hata = Metinler.girisHataMesaji(e),
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
