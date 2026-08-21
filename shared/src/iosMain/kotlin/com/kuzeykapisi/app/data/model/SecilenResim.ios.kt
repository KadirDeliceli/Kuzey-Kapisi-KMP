package com.kuzeykapisi.app.data.model

// Zaman kısıtlaması nedeniyle PHPickerViewController entegrasyonu şimdilik
// atlandı (bkz. Konum.ios.kt'deki aynı gerekçe) — Android + Web öncelikli,
// iOS güvenli varsayılana (seçim yok) düşer.
actual suspend fun resimSec(): SecilenResim? = null
