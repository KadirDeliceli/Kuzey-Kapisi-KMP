package com.kuzeykapisi.app.ui.theme

/**
 * Türkçe büyük harf. Kotlin'in `uppercase()`'i yerel ayardan bağımsızdır ve
 * "i"yi "I"ya çevirir ("Yönetim" → "YÖNETIM"); burada i/ı elle eşlenir.
 */
fun String.turkceBuyukHarf(): String = buildString(length) {
    for (harf in this@turkceBuyukHarf) {
        append(
            when (harf) {
                'i' -> 'İ'
                'ı' -> 'I'
                else -> harf.uppercaseChar()
            },
        )
    }
}
