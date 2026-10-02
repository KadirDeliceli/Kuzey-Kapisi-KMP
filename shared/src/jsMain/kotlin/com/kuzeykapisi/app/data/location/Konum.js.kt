package com.kuzeykapisi.app.data.location

// Eski (legacy) Kotlin/JS hedefi — asıl "web" hedefi wasmJs'tir.
// Burada gerçek geolocation entegrasyonu yerine güvenli varsayılana düşülür.
actual suspend fun guncelKonumAl(): Konum = VARSAYILAN_KONUM
