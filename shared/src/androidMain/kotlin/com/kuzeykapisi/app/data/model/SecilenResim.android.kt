package com.kuzeykapisi.app.data.model

import com.kuzeykapisi.app.data.location.AndroidContextHolder
import com.kuzeykapisi.app.data.media.ImagePickerHolder

actual suspend fun resimSec(): SecilenResim? {
    val uri = ImagePickerHolder.sec() ?: return null
    val resolver = AndroidContextHolder.appContext.contentResolver
    val bytes = runCatching {
        resolver.openInputStream(uri)?.use { it.readBytes() }
    }.getOrNull() ?: return null

    val mimeTipi = resolver.getType(uri)
    val uzanti = when {
        mimeTipi?.contains("png") == true -> ".png"
        mimeTipi?.contains("webp") == true -> ".webp"
        else -> ".jpg"
    }
    return SecilenResim(bytes = bytes, dosyaAdi = "secilen_gorsel$uzanti", uzanti = uzanti)
}
