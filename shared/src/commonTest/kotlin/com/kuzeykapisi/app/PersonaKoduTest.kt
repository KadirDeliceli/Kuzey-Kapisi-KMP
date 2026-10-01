package com.kuzeykapisi.app

import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.data.remote.yol
import com.kuzeykapisi.app.domain.slugify
import com.kuzeykapisi.app.ui.vm.personaKoduBelirle
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PersonaKoduTest {

    @Test
    fun bosKod_addanUretilir() {
        assertEquals("comlekci_umit_in_dukkani", personaKoduBelirle(elleGirilen = "", ad = "Çömlekçi Ümit'in Dükkânı"))
        // Elle yazılıp silinmiş (yalnızca boşluk kalmış) alan da boş sayılır.
        assertEquals("comlekci_umit_in_dukkani", personaKoduBelirle(elleGirilen = "   ", ad = "Çömlekçi Ümit'in Dükkânı"))
    }

    @Test
    fun elleGirilenKod_normalizeEdilir() {
        assertEquals("test_kod_ozel", personaKoduBelirle(elleGirilen = "test kod/özel", ad = "Başka Ad"))
        assertEquals("istanbul_lezzet", personaKoduBelirle(elleGirilen = "İstanbul/Lezzet", ad = "x"))
        assertEquals("a_b_c", personaKoduBelirle(elleGirilen = "../a?b#c", ad = "x"))
    }

    @Test
    fun uretilemeyenKod_null() {
        assertNull(personaKoduBelirle(elleGirilen = "", ad = "!!! ///"))
        assertEquals("", slugify("  /  "))
    }

    @Test
    fun yolParcalari_kodlanir() {
        assertEquals("kisiler/test%20kod%2F%C3%B6zel%3F%23", yol("kisiler", "test kod/özel?#"))
        // Güvenli kodlar olduğu gibi kalır.
        assertEquals("kisiler/ahmet_muhip_diranas", yol("kisiler", "ahmet_muhip_diranas"))
    }

    @Test
    fun kodlanmisYol_ktordaCiftKodlanmaz_veSlashYolBolmez() {
        // Mevcut tescil kodlarından biri: Türkçe harf + büyük harf.
        val url = URLBuilder().takeFrom("https://ornek.test/anlatim/${yol("tescil", "Ayancık_Göynek_Yakası")}").build()
        assertEquals("/anlatim/tescil/Ayanc%C4%B1k_G%C3%B6ynek_Yakas%C4%B1", url.encodedPath)
        assertEquals(listOf("anlatim", "tescil", "Ayancık_Göynek_Yakası"), url.segments)
        // Kod içindeki "/" yeni bir yol parçası açamaz.
        val kotu = URLBuilder().takeFrom("https://ornek.test/admin/persona-sil/${yol("kisiler", "../katalog")}").build()
        assertEquals(listOf("admin", "persona-sil", "kisiler", "../katalog"), kotu.segments)
    }

    @Test
    fun gorselUrl_kodlanir() {
        assertEquals("${Config.BASE_URL}gorseller/tescil/Sinop_Mant%C4%B1s%C4%B1", Config.gorselUrl("tescil", "Sinop_Mantısı"))
    }
}
