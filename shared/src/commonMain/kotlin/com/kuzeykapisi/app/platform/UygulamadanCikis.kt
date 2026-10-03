package com.kuzeykapisi.app.platform

import androidx.compose.runtime.Composable

/**
 * Uygulamayı kapatan eylem — Android'de Activity'yi bitirir (geri tuşunun
 * varsayılan davranışı), web ve iOS'ta hiçbir şey yapmaz (orada "uygulamadan
 * çık" diye bir kavram yok). Kapatılamaz dialoglarda geri tuşunun dialogu
 * kapatmak yerine uygulamadan çıkması için kullanılır.
 */
@Composable
expect fun rememberUygulamadanCikis(): () -> Unit
