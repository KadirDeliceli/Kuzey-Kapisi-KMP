package com.kuzeykapisi.app.ui.nav

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.kuzeykapisi.app.data.remote.AdminOturumu
import com.kuzeykapisi.app.domain.Screen
import com.kuzeykapisi.app.domain.adminEkrani
import com.kuzeykapisi.app.ui.vm.AdminViewModel

/**
 * Uygulamanın TEK navigasyon kaynağı — App.kt'ye dağılmış git/geri/admin-geçit
 * mantığını tek yerde toplar. Composable DEĞİLDİR: App.kt'de `remember` ile bir
 * kez oluşturulup ekranlara STABİL bir referans olarak geçilir (P3 turundaki
 * lambda sabitlemesiyle aynı gerekçe — alt ekranlar "skip" optimizasyonundan
 * yararlanabilsin).
 */
class Navigator(
    val ekranYigini: EkranYigini,
    private val adminOturumu: AdminOturumu,
    private val adminVm: AdminViewModel,
) {
    private val _gecisIleri = mutableStateOf(true)

    /**
     * Yalnızca GÖRSEL geçişin yönü: "kapı açılma" animasyonunun hangi tarafa
     * işleyeceğini söyler. Navigasyon kararlarına HİÇBİR etkisi yoktur.
     */
    val gecisIleri: State<Boolean> get() = _gecisIleri

    private val _adminGirisDialoguAcik = mutableStateOf(false)
    val adminGirisDialoguAcik: State<Boolean> get() = _adminGirisDialoguAcik

    fun adminGirisDialoguKapat() {
        _adminGirisDialoguAcik.value = false
    }

    /** Oturum yokken hiçbir admin ekranı yığına girmez; yerine giriş istenir. */
    fun git(hedef: Screen) {
        if (hedef.adminEkrani && !adminOturumu.acik.value) {
            adminVm.hataTemizle()
            _adminGirisDialoguAcik.value = true
        } else {
            _gecisIleri.value = true
            ekranYigini.ekle(hedef)
        }
    }

    /** Yığında SADECE bir üst seviyeye çıkar (kök ekranda no-op). */
    fun geri() {
        if (ekranYigini.boyut > 1) {
            _gecisIleri.value = false
            ekranYigini.cikar()
        }
    }

    /**
     * Yığındaki TÜM ekranları atıp kök (Ana Sayfa) ekrana TEK ADIMDA döner —
     * yığın derinliğinden bağımsız. Breadcrumb'ın "Ana Sayfa" basamağı gibi,
     * kaç seviye derinde olunduğunu bilmeden doğrudan köke dönmek gereken
     * yerlerde kullanılır (önceden [geri]'yi İKİ KEZ çağırıp yığın derinliğine
     * bağımlı, kırılgan bir varsayımla aynı sonuca ulaşılmaya çalışılıyordu).
     */
    fun anaSayfayaDon() {
        if (ekranYigini.boyut > 1) _gecisIleri.value = false
        ekranYigini.kaldir { true }
    }

    /**
     * Admin oturumu NASIL biterse bitsin (401, "Çıkış yap", hareketsizlik) tek
     * yol: yığındaki TÜM admin ekranları atılır ve Ana Sayfa'ya dönülür.
     * [girisIste] true ise (çıkış DIŞINDAKİ nedenler) giriş dialogu açılır.
     */
    fun adminOturumuSonlandi(girisIste: Boolean) {
        anaSayfayaDon()
        if (girisIste) _adminGirisDialoguAcik.value = true
    }
}
