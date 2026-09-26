package com.kuzeykapisi.app.data.remote

import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException

/**
 * Kullanıcıya gösterilecek mesajı seçmek için hatanın TÜRÜ. Ham istisna
 * metni (e.message) URL, HTTP gövdesi ve İngilizce teknik ayrıntı içerir;
 * arayüze asla o gitmez, yalnızca bu türe karşılık gelen sabit metin gider
 * (bkz. Metinler.hataMesaji).
 */
enum class HataTuru { AG, YETKISIZ, BULUNAMADI, DOGRULAMA, SUNUCU, BILINMEYEN }

/** HTTP yanıtı hiç alınamadı (bağlantı yok, sunucu kapalı, DNS...). Bkz. createHttpClient. */
class BaglantiHatasi(cause: Throwable) : Exception("Bağlantı kurulamadı", cause)

fun Throwable.hataTuru(): HataTuru = when (this) {
    is BaglantiHatasi -> HataTuru.AG
    is AdminApiHatasi -> httpKodunaGore(httpKodu)
    is ResponseException -> httpKodunaGore(response.status.value)
    // Sunucu beklenmeyen biçimde yanıt verdi: kullanıcı açısından sunucu sorunu.
    is SerializationException -> HataTuru.SUNUCU
    // HTTP yanıtı hiç gelmedi: bağlantı yok, zaman aşımı, sunucu kapalı, DNS...
    // Platformların ağ istisnaları ortak bir tipte birleşmediği için geri kalan
    // her şey ağ hatası sayılır.
    else -> HataTuru.AG
}

private fun httpKodunaGore(kod: Int): HataTuru = when (kod) {
    401, 403 -> HataTuru.YETKISIZ
    404 -> HataTuru.BULUNAMADI
    400, 409, 413, 415, 422 -> HataTuru.DOGRULAMA
    in 500..599 -> HataTuru.SUNUCU
    else -> HataTuru.BILINMEYEN
}

/** Günlük (log) için güvenli özet: yalnızca tip ve HTTP kodu — mesaj/gövde/URL yok. */
fun Throwable.logOzeti(): String = when (this) {
    is AdminApiHatasi -> "AdminApiHatasi (HTTP $httpKodu)"
    is ResponseException -> "${this::class.simpleName} (HTTP ${response.status.value})"
    else -> this::class.simpleName ?: "Bilinmeyen hata"
}
