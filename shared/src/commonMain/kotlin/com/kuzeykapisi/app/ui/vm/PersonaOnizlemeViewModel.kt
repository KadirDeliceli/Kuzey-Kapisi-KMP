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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonaOnizlemeUiState(
    val yukleniyor: Boolean = true,
    /** boş/null: backend bu öge için anlatım olmadığını söyledi (404) — hata değil. */
    val metin: String? = null,
    val hata: String? = null,
)

/**
 * PersonaOnizlemeEkrani'nın anlatım ÖNİZLEME metnini çeker. [AnlatimViewModel]'den
 * BİLEREK AYRI tutulur: o bir TTS motoru ([com.kuzeykapisi.app.data.tts.AnlatimOynatici])
 * kurar, bu ekranda ise ses hiç çalınmaz — yalnızca metin gösterilir (bkz.
 * PersonaOnizlemeEkrani'nin üstteki doc yorumu).
 */
class PersonaOnizlemeViewModel(
    private val repo: KuzeyRepository,
    private val kategori: String,
    private val kod: String,
) : ViewModel() {
    private val _state = MutableStateFlow(PersonaOnizlemeUiState())
    val state: StateFlow<PersonaOnizlemeUiState> = _state.asStateFlow()

    init {
        yukle()
    }

    fun yukle() {
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null) }
            try {
                val metin = repo.anlatimGetir(kategori, kod).orEmpty()
                _state.update { it.copy(yukleniyor = false, metin = metin) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "anlatım önizlemesi yüklenemedi: ${e.logOzeti()}" }
                _state.update { it.copy(yukleniyor = false, hata = Metinler.hataMesaji(e)) }
            }
        }
    }
}
