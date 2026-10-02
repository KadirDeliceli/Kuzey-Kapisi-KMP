package com.kuzeykapisi.app.ui.theme

import androidx.compose.ui.unit.sp

/**
 * "Etiket" (all-caps eyebrow) metinlerinde ekran ekran elle yazılan
 * `labelSmall.copy(letterSpacing = X.sp)` değerlerinin tek kaynağı. Type.kt'nin
 * kendi ölçeğindeki (gövde/başlık) letterSpacing değerleri bunun dışındadır —
 * onlar zaten o dosyada tek tanımlı.
 */
object HarfAraligi {
    val SP12 = 1.2.sp
    val SP14 = 1.4.sp
    val SP16 = 1.6.sp
    val SP18 = 1.8.sp
    val SP22 = 2.2.sp
    val SP24 = 2.4.sp
}
