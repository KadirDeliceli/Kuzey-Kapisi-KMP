package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.model.ChatUiState
import com.kuzeykapisi.app.data.model.Mesaj
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repo: KuzeyRepository,
    private val kategori: String,
    private val oge: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    fun basla() {
        scope.launch {
            _state.value = _state.value.copy(yukleniyor = true, hata = null)
            println("[KuzeyKapisi] POST /oturum/baslat isteği başlatılıyor (kategori=$kategori, oge=$oge)...")
            try {
                val yanit = repo.oturumBaslat(kategori, oge)
                println("[KuzeyKapisi] /oturum/baslat başarılı, sessionId=${yanit.sessionId}")
                _state.value = _state.value.copy(
                    baslik = yanit.baslik,
                    sessionId = yanit.sessionId,
                    mesajlar = listOf(Mesaj(metin = yanit.karsilama, benden = false)),
                    yukleniyor = false,
                )
            } catch (e: Exception) {
                println("[KuzeyKapisi] /oturum/baslat hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(yukleniyor = false, hata = e.message ?: "Oturum başlatılamadı")
            }
        }
    }

    fun gonder(mesaj: String) {
        val sessionId = _state.value.sessionId ?: return
        if (mesaj.isBlank()) return
        _state.value = _state.value.copy(
            mesajlar = _state.value.mesajlar + Mesaj(metin = mesaj, benden = true),
            yaziyor = true,
        )
        scope.launch {
            try {
                val sonuc = repo.guvenliSohbet(kategori, oge, sessionId, mesaj)
                var mesajlar = _state.value.mesajlar
                if (sonuc.yenilendi) {
                    mesajlar = mesajlar + Mesaj(
                        metin = "Bağlantı yenilendi — sohbet geçmişi sıfırlandı.",
                        benden = false,
                        sistemNotu = true,
                    )
                }
                mesajlar = mesajlar + Mesaj(metin = sonuc.cevap, benden = false)
                _state.value = _state.value.copy(
                    sessionId = sonuc.sessionId,
                    mesajlar = mesajlar,
                    yaziyor = false,
                )
            } catch (e: Exception) {
                println("[KuzeyKapisi] /sohbet hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    yaziyor = false,
                    hata = e.message ?: "Mesaj gönderilemedi",
                )
            }
        }
    }

    fun temizle() {
        scope.launch { repo.oturumKapat(_state.value.sessionId ?: return@launch) }
    }
}
