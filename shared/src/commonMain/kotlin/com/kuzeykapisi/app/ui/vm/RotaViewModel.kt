package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.model.Konum
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.model.VARSAYILAN_KONUM
import com.kuzeykapisi.app.data.model.guncelKonumAl
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RotaUiState(
    val konum: Konum? = null,
    val mesaj: String = "",
    val yukleniyor: Boolean = false,
    val sonuc: RotaYaniti? = null,
    val hata: String? = null,
)

class RotaViewModel(private val repo: KuzeyRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(RotaUiState())
    val state: StateFlow<RotaUiState> = _state.asStateFlow()

    fun basla() {
        scope.launch {
            val konum = runCatching { guncelKonumAl() }.getOrDefault(VARSAYILAN_KONUM)
            _state.value = _state.value.copy(konum = konum)
        }
    }

    fun gonder(mesaj: String) {
        if (mesaj.isBlank()) return
        scope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            val konum = _state.value.konum
                ?: runCatching { guncelKonumAl() }.getOrDefault(VARSAYILAN_KONUM)
            println("[KuzeyKapisi] POST /rota/olustur isteği başlatılıyor (enlem=${konum.enlem}, boylam=${konum.boylam})...")
            try {
                val yanit = repo.rotaOlustur(konum.enlem, konum.boylam, mesaj)
                println("[KuzeyKapisi] /rota/olustur başarılı, durak sayısı=${yanit.rota.size}")
                _state.value = _state.value.copy(konum = konum, sonuc = yanit, yukleniyor = false)
            } catch (e: Exception) {
                println("[KuzeyKapisi] /rota/olustur hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    konum = konum,
                    yukleniyor = false,
                    hata = e.message ?: "Rota oluşturulamadı, lütfen tekrar deneyin.",
                )
            }
        }
    }
}
