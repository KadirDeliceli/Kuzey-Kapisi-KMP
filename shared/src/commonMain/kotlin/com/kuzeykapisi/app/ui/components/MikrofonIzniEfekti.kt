package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable

/**
 * Platforma özel mikrofon izni akışı — [KonumIzniEfekti] ile aynı desen.
 * [istekNo] her değiştiğinde (0 dışında) izin kontrol edilir/istenir ve
 * sonuç [onSonuc]'a bildirilir. Yalnızca Android'de gerçek bir şey yapar
 * (rememberLauncherForActivityResult ile runtime izin diyaloğu); diğer
 * platformlarda no-op'tur ve doğrudan true bildirilir — asıl izin akışı o
 * platformlarda SesKaydedici'nin kendi kayıt girişiminde (AVAudioRecorder /
 * getUserMedia) native olarak yürütülür.
 */
@Composable
expect fun MikrofonIzniEfekti(istekNo: Int, onSonuc: (Boolean) -> Unit)
