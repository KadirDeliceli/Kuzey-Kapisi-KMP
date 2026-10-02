package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.data.tts.AnlatimDurumu
import com.kuzeykapisi.app.data.tts.AnlatimOynatici
import com.kuzeykapisi.app.domain.AnlatimKaynagi
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnlatimUiState(
    val metin: String? = null,
    val yukleniyor: Boolean = true,
    val hata: String? = null,
    // true: backend bu kaynak için anlatım olmadığını söyledi (404). Tekrar
    // denemek sonucu değiştirmez; ağ/sunucu hatasından (hata) ayrı tutulur.
    val anlatimYok: Boolean = false,
)

class AnlatimViewModel(
    private val repo: KuzeyRepository,
    private val kaynak: AnlatimKaynagi,
) : ViewModel() {
    private val _state = MutableStateFlow(AnlatimUiState())
    val state: StateFlow<AnlatimUiState> = _state.asStateFlow()

    private val oynatici = AnlatimOynatici()

    /** Dinleme motorunun anlık durumu — ekran yalnızca bunu izler, motora kendisi erişmez. */
    val oynatimDurumu: StateFlow<AnlatimDurumu> = oynatici.durum

    /** TR ses desteklenmiyorsa ya da oynatma başarısız olursa kısa, kullanıcıya gösterilebilir mesaj. */
    val sesHatasi: StateFlow<String?> = oynatici.hata

    init {
        yukle()
    }

    /** BAŞTAN oynatır — "Dinle" butonu. */
    fun dinle() {
        _state.value.metin?.let { oynatici.oynat(it) }
    }

    /** Duraklatır, konumu korur (devamEt() ile sürdürülebilir). */
    fun duraklat() {
        oynatici.duraklat()
    }

    /** Duraklatılan yerden devam eder. */
    fun devamEt() {
        oynatici.devamEt()
    }

    /** Metni BAŞTAN tekrar oynatır — "Baştan başla" butonu. */
    fun bastanBasla() {
        _state.value.metin?.let { oynatici.oynat(it) }
    }

    fun yukle() {
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null, anlatimYok = false) }
            try {
                val metin = when (kaynak) {
                    is AnlatimKaynagi.Persona -> repo.anlatimGetir(kaynak.kategori, kaynak.kod)
                    is AnlatimKaynagi.RotaDuragi -> repo.rotaAnlatimGetir(kaynak.mekanId)
                }
                _state.update { st ->
                    if (metin.isNullOrBlank()) {
                        st.copy(yukleniyor = false, anlatimYok = true)
                    } else {
                        st.copy(metin = metin, yukleniyor = false)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "anlatım yüklenemedi: ${e.logOzeti()}" }
                _state.update { it.copy(yukleniyor = false, hata = Metinler.hataMesaji(e)) }
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
