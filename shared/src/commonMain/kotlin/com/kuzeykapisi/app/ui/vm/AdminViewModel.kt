package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.remote.AdminOturumu
import com.kuzeykapisi.app.data.remote.OturumSonlanmaNedeni
import com.kuzeykapisi.app.data.remote.logOzeti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration

data class AdminUiState(
    /** Oturum açık mı. Token'ın kendisi arayüze hiç verilmez (bkz. AdminOturumu). */
    val oturumAcik: Boolean = false,
    val yukleniyor: Boolean = false,
    val hata: String? = null,
)

/**
 * Giriş dialogu ve oturum yaşam döngüsü. Token [AdminOturumu]'nda, yalnızca
 * bellekte tutulur — uygulama yeniden açılınca tekrar giriş istenmesi
 * kasıtlıdır. Uygulama düzeyinde viewModel {} ile alındığı için yapılandırma
 * değişikliğinden (döndürme) sağ çıkar; hareketsizlik zamanlayıcısı da burada,
 * uygulama ömrü boyunca çalışır.
 */
class AdminViewModel(
    private val repo: KuzeyRepository,
    private val oturum: AdminOturumu,
) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState(oturumAcik = oturum.acik.value))
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            oturum.acik.collect { acik -> _state.update { it.copy(oturumAcik = acik) } }
        }
        // Oturum başka bir nedenle kapandıysa giriş dialogu açıldığında
        // kullanıcıya NEDEN yeniden giriş istendiği söylenir.
        viewModelScope.launch {
            oturum.sonlanma.collect { neden ->
                val mesaj = when (neden) {
                    OturumSonlanmaNedeni.CIKIS -> null
                    OturumSonlanmaNedeni.YETKISIZ -> Metinler.ADMIN_OTURUM_GECERSIZ
                    OturumSonlanmaNedeni.ZAMAN_ASIMI -> Metinler.ADMIN_OTURUM_SURESI_DOLDU
                }
                _state.update { it.copy(hata = mesaj, yukleniyor = false) }
            }
        }
        // Hareketsizlik zamanlayıcısı: her admin isteği süreyi yeniler, bu
        // döngü kalan süreyi her uyanışta yeniden hesaplar. Süre dolunca
        // oturum, kullanıcı hiçbir şeye dokunmasa da kapanır.
        viewModelScope.launch {
            oturum.acik.collectLatest { acik ->
                while (acik) {
                    val kalan = oturum.kalanSure() ?: break
                    if (kalan <= Duration.ZERO) {
                        oturum.sonlandir(OturumSonlanmaNedeni.ZAMAN_ASIMI)
                        break
                    }
                    delay(kalan)
                }
            }
        }
    }

    fun girisYap(kullaniciAdi: String, sifre: String) {
        if (kullaniciAdi.isBlank() || sifre.isBlank()) {
            _state.update { it.copy(hata = Metinler.GIRIS_BILGI_EKSIK) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(yukleniyor = true, hata = null) }
            try {
                val token = repo.adminGiris(kullaniciAdi.trim(), sifre)
                oturum.baslat(token)
                _state.update { it.copy(yukleniyor = false, hata = null) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.d { "admin girişi başarısız: ${e.logOzeti()}" }
                // Yalnızca gerçek 401 "şifre hatalı" demektir; sunucu kapalıyken
                // kullanıcıya şifresinin yanlış olduğu söylenmez.
                _state.update {
                    it.copy(
                        yukleniyor = false,
                        hata = Metinler.girisHataMesaji(e),
                    )
                }
            }
        }
    }

    fun hataTemizle() {
        _state.update { it.copy(hata = null) }
    }

    fun cikisYap() {
        oturum.sonlandir(OturumSonlanmaNedeni.CIKIS)
    }

    /** Bir admin ekranı 401 gördüğünde çağırır (HttpClient zaten kapatmış olabilir; tekrar çağrı zararsızdır). */
    fun yetkisizBildir() {
        oturum.sonlandir(OturumSonlanmaNedeni.YETKISIZ)
    }
}
