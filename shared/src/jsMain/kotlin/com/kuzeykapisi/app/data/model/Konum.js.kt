package com.kuzeykapisi.app.data.model

// Eski (legacy) Kotlin/JS hedefi — asıl "web" hedefi wasmJs'tir (bkz. CLAUDE.md §0).
// Burada gerçek geolocation entegrasyonu yerine güvenli varsayılana düşülür.
actual suspend fun guncelKonumAl(): Konum = VARSAYILAN_KONUM
