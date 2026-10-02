package com.kuzeykapisi.app.data.location

// Zaman kısıtlaması nedeniyle CoreLocation entegrasyonu şimdilik atlandı
// (bkz. görev notu) — Android + Web öncelikli, iOS güvenli varsayılana düşer.
actual suspend fun guncelKonumAl(): Konum = VARSAYILAN_KONUM
