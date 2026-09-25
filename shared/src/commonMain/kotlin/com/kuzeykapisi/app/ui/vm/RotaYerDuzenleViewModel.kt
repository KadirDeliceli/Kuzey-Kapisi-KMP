package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.model.RotaYerEkleIstek
import com.kuzeykapisi.app.data.remote.AdminApiHatasi
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RotaYerDuzenleUiState(
    val ad: String,
    val enlem: String,
    val boylam: String,
    val sureDk: String,
    val aciklama: String,
    val anlatim: String = "",
    val anlatimiKaldir: Boolean = false,
    val kaydediliyor: Boolean = false,
    val genelHata: String? = null,
    val basariMesaji: String? = null,
    val oturumGecersiz: Boolean = false,
)

/**
 * ad/enlem/boylam/sureDk/aciklama, API listede zaten döndüğü için MEVCUT
 * değerlerle ön-dolu başlar. anlatim ise ayrıca repo.rotaYeriGetir ile
 * önceden çekilmiş `mevcutAnlatim` ile ön-dolu başlar (bkz. [anlatimGonderilecek]
 * — dokunulmadıysa gönderilmez, checkbox ile kaldırılabilir).
 */
class RotaYerDuzenleViewModel(
    private val repo: KuzeyRepository,
    val mekanId: Int,
    baslangic: RotaMekaniAdmin,
    mevcutAnlatim: String?,
) : ViewModel() {
    private val anlatimOrijinal = mevcutAnlatim ?: ""
    private val _state = MutableStateFlow(
        RotaYerDuzenleUiState(
            ad = baslangic.ad,
            enlem = baslangic.enlem.toString(),
            boylam = baslangic.boylam.toString(),
            sureDk = baslangic.sureDk.toString(),
            aciklama = baslangic.aciklama,
            anlatim = anlatimOrijinal,
        ),
    )
    val state: StateFlow<RotaYerDuzenleUiState> = _state.asStateFlow()

    fun adDegisti(v: String) { _state.value = _state.value.copy(ad = v) }
    fun enlemDegisti(v: String) { _state.value = _state.value.copy(enlem = v) }
    fun boylamDegisti(v: String) { _state.value = _state.value.copy(boylam = v) }
    fun sureDkDegisti(v: String) { _state.value = _state.value.copy(sureDk = v) }
    fun aciklamaDegisti(v: String) { _state.value = _state.value.copy(aciklama = v) }
    fun anlatimDegisti(v: String) { _state.value = _state.value.copy(anlatim = v) }
    fun anlatimiKaldirDegisti(v: Boolean) { _state.value = _state.value.copy(anlatimiKaldir = v) }

    /**
     * Kullanıcı anlatım alanına hiç dokunmadıysa (metin çekilen orijinalle
     * aynıysa) null döner — backend bunu "mevcut anlatıma dokunma" olarak
     * yorumluyor. Checkbox işaretliyse her durumda "" (kaldır) döner.
     */
    private fun anlatimGonderilecek(s: RotaYerDuzenleUiState): String? = when {
        s.anlatimiKaldir -> ""
        s.anlatim.trim() == anlatimOrijinal.trim() -> null
        else -> s.anlatim.trim()
    }

    fun kaydet(token: String) {
        val s = _state.value
        val enlem = s.enlem.trim().replace(',', '.').toDoubleOrNull()
        val boylam = s.boylam.trim().replace(',', '.').toDoubleOrNull()
        val sureDk = s.sureDk.trim().toIntOrNull()
        if (s.ad.isBlank() || s.aciklama.isBlank()) {
            _state.value = s.copy(genelHata = "'Ad' ve 'Açıklama' alanları boş olamaz.")
            return
        }
        if (enlem == null || boylam == null) {
            _state.value = s.copy(genelHata = "'Enlem' ve 'Boylam' geçerli birer sayı olmalı.")
            return
        }
        if (sureDk == null || sureDk <= 0) {
            _state.value = s.copy(genelHata = "'Ziyaret Süresi' sıfırdan büyük bir tam sayı olmalı.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(kaydediliyor = true, genelHata = null, basariMesaji = null)
            try {
                repo.rotaYeriGuncelle(
                    token = token,
                    mekanId = mekanId,
                    istek = RotaYerEkleIstek(
                        ad = s.ad.trim(),
                        enlem = enlem,
                        boylam = boylam,
                        sureDk = sureDk,
                        aciklama = s.aciklama.trim(),
                        anlatim = anlatimGonderilecek(s),
                    ),
                )
                _state.value = _state.value.copy(kaydediliyor = false, basariMesaji = "Güncellendi.")
            } catch (e: AdminApiHatasi) {
                println("[KuzeyKapisi] rota-yer-guncelle hatası: HTTP ${e.httpKodu} — ${e.detay}")
                _state.value = if (e.httpKodu == 401) {
                    _state.value.copy(kaydediliyor = false, oturumGecersiz = true)
                } else {
                    _state.value.copy(kaydediliyor = false, genelHata = e.detay)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                println("[KuzeyKapisi] rota-yer-guncelle ağ hatası: ${e::class.simpleName}: ${e.message}")
                _state.value = _state.value.copy(
                    kaydediliyor = false,
                    genelHata = "Ağ hatası: lütfen bağlantınızı kontrol edip tekrar deneyin.",
                )
            }
        }
    }
}
