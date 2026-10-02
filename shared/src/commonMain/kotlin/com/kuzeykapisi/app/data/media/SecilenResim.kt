package com.kuzeykapisi.app.data.media

/** Admin panelinde kullanıcının seçtiği görsel: ham bayt + dosya adı/uzantısı. */
data class SecilenResim(val bytes: ByteArray, val dosyaAdi: String, val uzanti: String)

/**
 * Platforma özel görsel seçici. Kullanıcı seçimi iptal ederse ya da bir hata
 * oluşursa null döner — çağıran taraf bunu sessizce ele alır, uygulama
 * çökmez. androidMain: ActivityResultContracts (GetContent). wasmJsMain:
 * gizli HTML <input type=file> + FileReader. iOS ve eski (legacy) js
 * hedeflerinde şimdilik güvenli varsayılana (null) düşülür.
 */
expect suspend fun resimSec(): SecilenResim?
