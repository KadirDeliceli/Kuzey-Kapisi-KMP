package com.kuzeykapisi.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * TEK kırılım noktası — telefon (dar) ile tablet/masaüstü (geniş) düzen
 * arasındaki sınır. Yedi farklı dosyada (App.kt'deki sohbet paneli, Footer,
 * TopBar, AdminAnaSayfaScreen, BotListScreen, HomeScreen, SubMenuScreen)
 * ayrı ayrı `600.dp` olarak tekrarlanıyordu; artık TEK kaynak burada —
 * değer değişirse tüm ekranlar birlikte güncellenir.
 */
val TELEFON_KIRILIMI = 600.dp
