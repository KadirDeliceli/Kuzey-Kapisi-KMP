package com.kuzeykapisi.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.kuzeykapisi.app.data.remote.ApiService
import com.kuzeykapisi.app.data.repo.KuzeyRepository

/**
 * Uygulama düzeyinde tek örnek: Activity'nin (web/iOS'ta pencerenin)
 * ViewModelStore'unda yaşar, yani yapılandırma değişikliğinden (döndürme,
 * karanlık mod) sağ çıkar. Paylaşılan repository'yi ve her ekran kapsamının
 * kendi ViewModelStore'unu tutar.
 *
 * Neden ekran başına ayrı store: `viewModel {}` varsayılan olarak Activity'nin
 * store'unu kullanır ve o store yalnızca Activity tamamen bitince temizlenir.
 * Bu durumda bir ekrandan geri çıkıldığında o ekranın ViewModel'i (ve
 * viewModelScope'u) hiç iptal edilmezdi. Burada her kapsam kendi store'unu
 * alır; kapsam artık gerekmediğinde store temizlenir, ViewModel'lerin
 * onCleared()'ı çalışır ve viewModelScope iptal olur.
 */
class VmDeposu : ViewModel() {
    val repo = KuzeyRepository(ApiService())

    private val depolar = mutableMapOf<String, ViewModelStore>()

    fun depo(anahtar: String): ViewModelStore = depolar.getOrPut(anahtar) { ViewModelStore() }

    /** [anahtar]'ı ve altındaki tüm iç kapsamları ("$anahtar/...") temizler. */
    fun temizle(anahtar: String) {
        val silinecekler = depolar.keys.filter { it == anahtar || it.startsWith("$anahtar/") }
        silinecekler.forEach { depolar.remove(it)?.clear() }
    }

    override fun onCleared() {
        depolar.values.forEach { it.clear() }
        depolar.clear()
    }
}

val LocalVmDeposu = staticCompositionLocalOf<VmDeposu> { error("VmDeposu sağlanmadı (App içinde değil).") }

private val LocalKapsamAnahtari = staticCompositionLocalOf<String?> { null }

/**
 * İçindeki `viewModel {}` çağrılarına [anahtar]'a ait ayrı bir
 * ViewModelStoreOwner verir. İç içe kullanılırsa anahtar üst kapsamın altına
 * eklenir ("ekran-3/rota-anlatim-12"); üst kapsam temizlenince alttakiler de
 * temizlenir.
 *
 * Temizleme, içerik kompozisyondan AYRILDIĞINDA ve [halaGerekli] false
 * döndüğünde yapılır:
 *  - Ekran yığından çıkarıldı -> çıkış animasyonu bitince içerik ayrılır,
 *    halaGerekli false -> store temizlenir, istekler iptal olur.
 *  - Yapılandırma değişikliği -> içerik yine ayrılır ama girdi hâlâ
 *    (kaydedilmiş) yığındadır, halaGerekli true -> store korunur ve yeniden
 *    oluşturulan ekran aynı ViewModel'e bağlanır.
 * Temizlik bilerek çıkış animasyonundan SONRA yapılır: animasyon sırasında
 * hâlâ çizilen ekran viewModel {} çağırırsa boşaltılmış store'da yeni bir
 * ViewModel yaratılırdı.
 */
@Composable
fun VmKapsami(
    anahtar: String,
    halaGerekli: () -> Boolean,
    icerik: @Composable () -> Unit,
) {
    val depo = LocalVmDeposu.current
    val ust = LocalKapsamAnahtari.current
    val tamAnahtar = if (ust == null) anahtar else "$ust/$anahtar"
    val sahip = remember(depo, tamAnahtar) {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = depo.depo(tamAnahtar)
        }
    }
    val guncelHalaGerekli by rememberUpdatedState(halaGerekli)
    DisposableEffect(depo, tamAnahtar) {
        onDispose {
            if (!guncelHalaGerekli()) depo.temizle(tamAnahtar)
        }
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides sahip,
        LocalKapsamAnahtari provides tamAnahtar,
    ) {
        icerik()
    }
}
