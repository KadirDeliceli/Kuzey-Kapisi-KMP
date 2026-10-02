package com.kuzeykapisi.app

import com.kuzeykapisi.app.domain.slugify
import com.kuzeykapisi.app.ui.theme.turkceBuyukHarf
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun slugify_turkceKarakterleriSadelestirirVeAltCizgiyeCevirir() {
        assertEquals("sinop_kalesi", slugify("Sinop Kalesi"))
        assertEquals("comlekci_umit", slugify("Çömlekçi Ümit"))
    }

    @Test
    fun turkceBuyukHarf_i_ve_iNoktasizDogruBuyur() {
        // Kotlin'in yerel ayardan bağımsız uppercase()'i "i"yi "I"ya çevirir;
        // Türkçede doğrusu "İ"dir (bkz. ui/theme/Metin.kt).
        assertEquals("YÖNETİM", "yönetim".turkceBuyukHarf())
        assertEquals("ISPARTA", "ısparta".turkceBuyukHarf())
    }
}
