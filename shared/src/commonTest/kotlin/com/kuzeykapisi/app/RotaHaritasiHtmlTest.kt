package com.kuzeykapisi.app

import com.kuzeykapisi.app.data.model.RotaDurak
import com.kuzeykapisi.app.ui.components.rotaHaritasiHtmlOlustur
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RotaHaritasiHtmlTest {

    private fun durak(ad: String) = RotaDurak(
        sira = 1, id = 1, ad = ad, tur = "tarih", aciklama = "",
        enlem = 42.0, boylam = 35.1,
        oncekiNoktadanYolDk = 0, ziyaretSuresiDk = 10, varisToplamDk = 10, googleMapsUrl = "",
    )

    private fun gomuluVeri(html: String) = html.substringAfter("var duraklar = ").substringBefore(";")

    @Test
    fun mekanAdi_scriptBlogunuKapatamaz() {
        val html = rotaHaritasiHtmlOlustur(listOf(durak("</script><img src=x onerror=alert(1)>")))
        // Uygulamanın kendi iki </script> etiketi dışında hiçbiri olmamalı.
        assertEquals(2, Regex("</script>").findAll(html).count())
        assertFalse(html.contains("<img src=x"))
        val veri = gomuluVeri(html)
        assertFalse(veri.contains("<"), veri)
        assertTrue(veri.contains("\\u003c/script>"), veri)
    }

    @Test
    fun mekanAdi_htmlOlarakDegilMetinOlarakEklenir() {
        val html = rotaHaritasiHtmlOlustur(listOf(durak("A")))
        assertTrue(html.contains("baslik.textContent = d.sira + '. ' + d.ad;"))
        assertFalse(html.contains("bindPopup('<b>'"))
    }

    @Test
    fun yerTutucuKalmaz_veLeafletSriIleYuklenir() {
        val html = rotaHaritasiHtmlOlustur(listOf(durak("__ZEMIN__")))
        // Veri içindeki yer tutucu benzeri metin değiştirilmez, şablondakilerin hepsi doldurulur.
        assertEquals(1, Regex("__[A-Z_]+__").findAll(html).count())
        assertEquals(2, Regex("integrity=\"sha256-").findAll(html).count())
        // Token renkleri: KaranlikLacivert / FenerAlevi / TasBeyazi.
        assertTrue(html.contains("#0B1E2D") && html.contains("#E8A33D") && html.contains("#F2EFE7"))
        // Eski elle yazılmış, token olmayan renkler kalmadı.
        assertFalse(html.contains("#16455A") || html.contains("#7C8B93"))
        // İmza köşe: 20/20/4/20 (sol-üst, sağ-üst, sağ-alt, sol-alt).
        assertTrue(html.contains("border-radius: 20px 20px 4px 20px"))
    }
}
