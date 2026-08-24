package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.model.SecilenResim
import com.kuzeykapisi.app.data.model.resimSec
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PersonaDuzenleUiState(
    val ad: String,
    val karsilama: String,
    val icerik: String,
    val anlatim: String,
    val anlatimiKaldir: Boolean = false,
    val gorselVar: Boolean,
    val gorsel: SecilenResim? = null,
    val kaydediliyor: Boolean = false,
    val genelHata: String? = null,
    val basariMesaji: String? = null,
    val oturumGecersiz: Boolean = false,
)

/**
 * kategori/kod SABİTTİR. Form artık `initial` (repo.personaGetir ile önceden
 * çekilmiş içerik) ile ÖN-DOLU açılır. anlatim alanı ayrıca `anlatimiKaldir`
 * checkbox'ı ile birlikte "dokunulmadıysa gönderme" mantığıyla çalışır —
 * bkz. [anlatimGonderilecek].
 */
class PersonaDuzenleViewModel(
    private val repo: KuzeyRepository,
    val kategori: String,
    val kod: String,
    private val initial: PersonaDetay,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val anlatimOrijinal = initial.anlatim ?: ""
    private val _state = MutableStateFlow(
        PersonaDuzenleUiState(
            ad = initial.ad,
            karsilama = initial.karsilama,
            icerik = initial.icerik,
            anlatim = anlatimOrijinal,
            gorselVar = initial.gorselVar,
        ),
    )
    val state: StateFlow<PersonaDuzenleUiState> = _state.asStateFlow()

    fun adDegisti(v: String) { _state.value = _state.value.copy(ad = v) }
    fun karsilamaDegisti(v: String) { _state.value = _state.value.copy(karsilama = v) }
    fun icerikDegisti(v: String) { _state.value = _state.value.copy(icerik = v) }
    fun anlatimDegisti(v: String) { _state.value = _state.value.copy(anlatim = v) }
    fun anlatimiKaldirDegisti(v: Boolean) { _state.value = _state.value.copy(anlatimiKaldir = v) }

    fun gorselSec() {
        scope.launch {
            val secilen = runCatching { resimSec() }.getOrNull()
            if (secilen != null) _state.value = _state.value.copy(gorsel = secilen)
        }
    }

    /**
     * Kullanıcı anlatım alanına hiç dokunmadıysa (metin çekilen orijinalle
     * aynıysa) null döner — backend bunu "mevcut anlatıma dokunma" olarak
     * yorumluyor. Checkbox işaretliyse her durumda "" (kaldır) döner.
     * Kullanıcı metni gerçekten değiştirdiyse yeni metni döner.
     */
    private fun anlatimGonderilecek(s: PersonaDuzenleUiState): String? = when {
        s.anlatimiKaldir -> ""
        s.anlatim.trim() == anlatimOrijinal.trim() -> null
        else -> s.anlatim.trim()
    }

    fun kaydet(token: String) {
        val s = _state.value
        if (s.ad.isBlank() || s.karsilama.isBlank() || s.icerik.isBlank()) {
            _state.value = s.copy(
                genelHata = "'Ad', 'Açılış Mesajı' ve 'Detaylı İçerik' alanları boş olamaz.",
            )
            return
        }
        scope.launch {
            _state.value = _state.value.copy(kaydediliyor = true, genelHata = null, basariMesaji = null)
            try {
                val gonderilecekAnlatim = anlatimGonderilecek(s)
                println(
                    "[KuzeyKapisi][DEBUG] persona-guncelle anlatim kararı: " +
                        when {
                            gonderilecekAnlatim == null -> "null (dokunma)"
                            gonderilecekAnlatim.isEmpty() -> "\"\" (kaldır)"
                            else -> "gerçek metin (${gonderilecekAnlatim.length} karakter)"
                        },
                )
                repo.personaGuncelle(
                    token = token,
                    kategori = kategori,
                    kod = kod,
                    ad = s.ad.trim(),
                    karsilama = s.karsilama.trim(),
                    icerik = s.icerik.trim(),
                    anlatim = gonderilecekAnlatim,
                    gorsel = s.gorsel,
                )
                _state.value = _state.value.copy(kaydediliyor = false, basariMesaji = "Güncellendi.")
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] persona-guncelle hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(kaydediliyor = false, oturumGecersiz = true)
                } else {
                    _state.value.copy(kaydediliyor = false, genelHata = e.detay)
                }
            } catch (e: Exception) {
                println("[KuzeyKapisi] persona-guncelle ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    kaydediliyor = false,
                    genelHata = "Ağ hatası: lütfen bağlantınızı kontrol edip tekrar deneyin.",
                )
            }
        }
    }
}
