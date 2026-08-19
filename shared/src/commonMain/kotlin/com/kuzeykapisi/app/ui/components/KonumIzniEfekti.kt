package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable

/**
 * Platforma özel konum izni akışı (yalnızca Android'de gerçek bir şey yapar:
 * runtime izin isteği başlatır). Diğer platformlarda no-op'tur — tarayıcı
 * kendi izin diyaloğunu [guncelKonumAl] çağrıldığında otomatik gösterir,
 * iOS ise şimdilik varsayılan konuma düşer.
 */
@Composable
expect fun KonumIzniEfekti()
