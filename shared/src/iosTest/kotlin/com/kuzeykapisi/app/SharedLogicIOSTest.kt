package com.kuzeykapisi.app

import com.kuzeykapisi.app.domain.slugify
import com.kuzeykapisi.app.ui.theme.turkceBuyukHarf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * commonTest'teki aynı mantığın (bkz. SharedCommonTest) Kotlin/Native (iOS)
 * hedefinde de aynı sonucu ürettiğini doğrular — Türkçe karakter dönüşümü
 * platformdan platforma (JVM/Native/JS) farklı davranabilen bir alandır.
 */
class SharedLogicIOSTest {

    @Test
    fun slugify_turkceKarakterleriSadelestirirVeAltCizgiyeCevirir() {
        assertEquals("sinop_kalesi", slugify("Sinop Kalesi"))
        assertEquals("comlekci_umit", slugify("Çömlekçi Ümit"))
    }

    @Test
    fun turkceBuyukHarf_i_ve_iNoktasizDogruBuyur() {
        assertEquals("YÖNETİM", "yönetim".turkceBuyukHarf())
        assertEquals("ISPARTA", "ısparta".turkceBuyukHarf())
    }
}
