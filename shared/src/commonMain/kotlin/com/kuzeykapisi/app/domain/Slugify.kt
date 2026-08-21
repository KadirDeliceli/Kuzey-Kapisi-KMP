package com.kuzeykapisi.app.domain

private val TR_HARITA: Map<Char, Char> = mapOf(
    'ç' to 'c', 'Ç' to 'c', 'ğ' to 'g', 'Ğ' to 'g', 'ı' to 'i', 'İ' to 'i',
    'ö' to 'o', 'Ö' to 'o', 'ş' to 's', 'Ş' to 's', 'ü' to 'u', 'Ü' to 'u',
)

/**
 * Backend'deki admin_motoru._slugify ile birebir aynı mantık: Türkçe
 * karakterleri sadeleştirir, küçük harfe çevirir, harf/rakam dışını alt
 * çizgiye çevirir, baş/son alt çizgileri kırpar.
 */
fun slugify(metin: String): String {
    val donusturulmus = metin.map { TR_HARITA[it] ?: it }.joinToString("").lowercase()
    return donusturulmus.replace(Regex("[^a-z0-9]+"), "_").trim('_')
}
