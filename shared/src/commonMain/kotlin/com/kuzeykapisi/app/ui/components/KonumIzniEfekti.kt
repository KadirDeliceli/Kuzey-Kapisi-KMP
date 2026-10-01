package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable

/**
 * Platforma özel konum izni akışı. Android'de runtime izin isteği başlatır;
 * diğer platformlarda no-op'tur — tarayıcı kendi izin diyaloğunu
 * [guncelKonumAl] çağrıldığında gösterir, iOS ise şimdilik varsayılan
 * konuma düşer.
 *
 * [onSonuc], izin durumu KESİNLEŞTİĞİNDE (zaten verilmiş, kullanıcı verdi ya
 * da reddetti) bir kez çağrılır; konum bundan ÖNCE istenmemelidir, yoksa
 * izin diyaloğu açıkken varsayılan konuma düşülür. Değer: izin verildi mi
 * (izin sistemi olmayan platformlarda true — karar tarayıcıya kalır).
 */
@Composable
expect fun KonumIzniEfekti(onSonuc: (verildi: Boolean) -> Unit)
