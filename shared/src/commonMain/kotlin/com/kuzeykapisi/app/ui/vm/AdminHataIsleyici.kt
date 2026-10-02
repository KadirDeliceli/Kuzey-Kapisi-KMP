package com.kuzeykapisi.app.ui.vm

import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.remote.AdminApiHatasi

/**
 * TÜM admin ViewModel'lerinin paylaştığı [AdminApiHatasi] -> sonuç kararı:
 * 401 oturumun geçersiz olduğunu, diğer her şey kullanıcıya gösterilecek
 * mesajı ifade eder. Önceden 8 ayrı catch bloğunda tekrarlanan
 * `if (e.httpKodu == 401) ... else Metinler.adminHataMesaji(e)` kalıbının
 * yerine — hangi state alanının güncelleneceğine her ViewModel kendi karar
 * verir, bu yalnızca KARARI merkezileştirir.
 */
sealed interface AdminHataSonucu {
    data object OturumGecersiz : AdminHataSonucu
    data class Mesaj(val metin: String) : AdminHataSonucu
}

fun adminHatasiDegerlendir(e: AdminApiHatasi): AdminHataSonucu =
    if (e.httpKodu == 401) {
        AdminHataSonucu.OturumGecersiz
    } else {
        AdminHataSonucu.Mesaj(Metinler.adminHataMesaji(e))
    }
