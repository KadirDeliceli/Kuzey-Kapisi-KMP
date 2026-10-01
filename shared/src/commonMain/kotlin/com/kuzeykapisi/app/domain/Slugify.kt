package com.kuzeykapisi.app.domain

private val TR_HARITA: Map<Char, Char> = mapOf(
    'ç' to 'c', 'Ç' to 'c', 'ğ' to 'g', 'Ğ' to 'g', 'ı' to 'i', 'İ' to 'i',
    'ö' to 'o', 'Ö' to 'o', 'ş' to 's', 'Ş' to 's', 'ü' to 'u', 'Ü' to 'u',
    // Şapkalı harfler (Dükkân, Hâlâ, Kâtip): atılırsa kelime bölünürdü ("dukk_n").
    'â' to 'a', 'Â' to 'a', 'î' to 'i', 'Î' to 'i', 'û' to 'u', 'Û' to 'u',
)

/**
 * Backend'deki admin_motoru._slugify ile aynı mantık: Türkçe karakterleri
 * sadeleştirir, küçük harfe çevirir, harf/rakam dışını (boşluk, "/", "?",
 * "#", kesme işareti...) alt çizgiye çevirir, baş/son alt çizgileri kırpar.
 * Sonuç yalnızca [a-z0-9_] içerir: URL yolunda ve dosya adında güvenlidir.
 * Ayırıcı, mevcut kodlarla (ahmet_muhip_diranas) uyum için alt çizgidir.
 */
fun slugify(metin: String): String {
    val donusturulmus = metin.map { TR_HARITA[it] ?: it }.joinToString("").lowercase()
    return donusturulmus.replace(Regex("[^a-z0-9]+"), "_").trim('_')
}
