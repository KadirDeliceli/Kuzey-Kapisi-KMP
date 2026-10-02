package com.kuzeykapisi.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Bölüm başlığındaki kayıt sayısı: yükleniyor, geldi ya da (hata/yetki yok) gizli. */
sealed interface KayitSayisi {
    data object Yukleniyor : KayitSayisi
    data class Hazir(val adet: Int) : KayitSayisi
    data object Gizli : KayitSayisi
}

data class AdminOzetUiState(
    val personaSayisi: KayitSayisi = KayitSayisi.Yukleniyor,
    val rotaSayisi: KayitSayisi = KayitSayisi.Yukleniyor,
)

/**
 * AdminAnaSayfaScreen'in iki özet sayısını (persona/rota yeri adedi) çeker.
 * İkisi de ikincil bilgidir: birbirinden BAĞIMSIZ çekilir ve hata (401 dahil)
 * olursa rozeti sessizce gizler, panelin kendisini asla engellemez.
 */
class AdminOzetViewModel(private val repo: KuzeyRepository) : ViewModel() {
    private val _state = MutableStateFlow(AdminOzetUiState())
    val state: StateFlow<AdminOzetUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val sayi = sayiGetir { repo.katalog().values.sumOf { it.ogeler.size } }
            _state.update { it.copy(personaSayisi = sayi) }
        }
        viewModelScope.launch {
            val sayi = sayiGetir { repo.rotaYerleriListele().size }
            _state.update { it.copy(rotaSayisi = sayi) }
        }
    }

    private suspend fun sayiGetir(islem: suspend () -> Int): KayitSayisi = try {
        KayitSayisi.Hazir(islem())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        KayitSayisi.Gizli
    }
}
