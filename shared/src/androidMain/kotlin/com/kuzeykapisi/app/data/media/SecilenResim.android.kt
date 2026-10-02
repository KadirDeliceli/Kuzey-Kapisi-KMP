package com.kuzeykapisi.app.data.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.kuzeykapisi.app.platform.AndroidContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Seçilen görsel için üst bayt sınırı — aşan bir fotoğraf belleğe yük bindirmesin diye otomatik küçültülür. */
private const val GORSEL_AZAMI_BAYT = 5 * 1024 * 1024

actual suspend fun resimSec(): SecilenResim? {
    val uri = ImagePickerHolder.sec() ?: return null
    val resolver = AndroidContextHolder.appContext.contentResolver
    // Dosya I/O bloklayan bir çağrıdır — Dispatchers.Default (CPU-yoğun iş
    // için sınırlı thread havuzu) değil, Dispatchers.IO üzerinde çalışmalı.
    return withContext(Dispatchers.IO) {
        val hamBaytlar = runCatching {
            resolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull() ?: return@withContext null

        val bytes = if (hamBaytlar.size > GORSEL_AZAMI_BAYT) {
            kucultVeSikistir(hamBaytlar) ?: hamBaytlar
        } else {
            hamBaytlar
        }

        val mimeTipi = resolver.getType(uri)
        val uzanti = when {
            // Yeniden sıkıştırılan görsel her zaman JPEG'e kodlanır.
            bytes !== hamBaytlar -> ".jpg"
            mimeTipi?.contains("png") == true -> ".png"
            mimeTipi?.contains("webp") == true -> ".webp"
            else -> ".jpg"
        }
        SecilenResim(bytes = bytes, dosyaAdi = "secilen_gorsel$uzanti", uzanti = uzanti)
    }
}

/**
 * [GORSEL_AZAMI_BAYT]'ı aşan görseli yarı çözünürlükte decode edip JPEG
 * kalitesini kademeli düşürerek sınırın altına indirmeye çalışır. Decode
 * edilemezse (bozuk/desteklenmeyen format) null döner — çağıran taraf
 * orijinal baytlarla devam eder.
 */
private fun kucultVeSikistir(hamBaytlar: ByteArray): ByteArray? = runCatching {
    val secenekler = BitmapFactory.Options().apply { inSampleSize = 2 }
    val bitmap = BitmapFactory.decodeByteArray(hamBaytlar, 0, hamBaytlar.size, secenekler)
        ?: return null
    try {
        var kalite = 90
        var sonuc: ByteArray
        do {
            val cikis = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, kalite, cikis)
            sonuc = cikis.toByteArray()
            kalite -= 15
        } while (sonuc.size > GORSEL_AZAMI_BAYT && kalite > 30)
        sonuc
    } finally {
        bitmap.recycle()
    }
}.getOrNull()
