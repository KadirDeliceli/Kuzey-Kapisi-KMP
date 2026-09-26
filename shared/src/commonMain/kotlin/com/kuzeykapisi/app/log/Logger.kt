package com.kuzeykapisi.app.log

/**
 * Uygulamanın TEK günlük (log) kapısı. Yalnızca hata ayıklama (debug)
 * derlemesinde yazar; release derlemede hiçbir şey yazdırmaz ve mesaj
 * lambdası hiç çalıştırılmaz (string bile üretilmez).
 *
 * Buraya ASLA kişisel veri verilmez: kullanıcının yazdığı/söylediği mesaj,
 * botun cevabı, API istek/yanıt gövdeleri, konum, oturum kimliği.
 */
object Logger {
    private const val ETIKET = "KuzeyKapisi"

    fun d(mesaj: () -> String) {
        if (hataAyiklamaModu) platformaYaz(ETIKET, mesaj())
    }
}

/** Debug derlemesi mi? Her platform kendi güvenilir sinyalinden okur. */
internal expect val hataAyiklamaModu: Boolean

internal expect fun platformaYaz(etiket: String, mesaj: String)
