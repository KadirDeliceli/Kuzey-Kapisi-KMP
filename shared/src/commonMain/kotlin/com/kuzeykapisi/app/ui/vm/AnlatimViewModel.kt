package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.data.tts.AnlatimOynatici
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/** Anlatım ekranının metni nereden çekeceğini belirten kaynak — persona (kategori+kod) ya da rota durağı (mekan id). */
@Serializable
sealed interface AnlatimKaynagi {
    @Serializable
    data class Persona(val kategori: String, val kod: String) : AnlatimKaynagi

    @Serializable
    data class RotaDuragi(val mekanId: Int) : AnlatimKaynagi
}

data class AnlatimUiState(
    val metin: String? = null,
    val yukleniyor: Boolean = true,
    val hata: String? = null,
)

class AnlatimViewModel(
    private val repo: KuzeyRepository,
    private val kaynak: AnlatimKaynagi,
) : ViewModel() {
    private val _state = MutableStateFlow(AnlatimUiState())
    val state: StateFlow<AnlatimUiState> = _state.asStateFlow()

    val oynatici = AnlatimOynatici()

    init {
        yukle()
    }

    fun yukle() {
        viewModelScope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            try {
                val metin = when (kaynak) {
                    is AnlatimKaynagi.Persona -> repo.anlatimGetir(kaynak.kategori, kaynak.kod)
                    is AnlatimKaynagi.RotaDuragi -> repo.rotaAnlatimGetir(kaynak.mekanId)
                }
                _state.value = _state.value.copy(metin = metin, yukleniyor = false)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.value = _state.value.copy(yukleniyor = false, hata = "Anlatım yüklenemedi.")
            }
        }
    }

    // Ekrandan çıkılınca (kapsam temizlenince) ses MUTLAKA durdurulur ve motor
    // kaynakları serbest bırakılır. Yapılandırma değişikliğinde çağrılmaz:
    // anlatım döndürmeden sonra kaldığı yerden sürer.
    override fun onCleared() {
        oynatici.durdur()
        oynatici.serbestBirak()
    }
}
