package com.kuzeykapisi.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * İMZA DETAY — "elle kesilmiş taş" köşesi.
 *
 * Üç köşe normal yarıçapta, SAĞ-ALT köşe belirgin şekilde daha küçük. Tek bir
 * yardımcı üzerinden üretilir ve TÜM kart/panel/buton tiplerinde (ana kart,
 * alt kart, bot kartı, rota durak kartı, admin liste satırı, form alanı,
 * dialog) aynı şekilde uygulanır.
 */
fun kesikTasSekli(yaricap: Dp, kesik: Dp = KESIK_KOSE_YARICAPI) = RoundedCornerShape(
    topStart = yaricap,
    topEnd = yaricap,
    bottomEnd = kesik,
    bottomStart = yaricap,
)

/** Kesilmiş (sağ-alt) köşenin yarıçapı — tüm ölçeklerde sabit kalır. */
val KESIK_KOSE_YARICAPI = 4.dp

/** Ana/alt/bot kartları, rota tur kartları. */
val KartSekli = kesikTasSekli(20.dp)

/** Liste satırı, küçük kart, harita çerçevesi, mesaj balonu. */
val SatirSekli = kesikTasSekli(14.dp)

/** Buton ve chip. */
val ButonSekli = kesikTasSekli(12.dp)

/** Metin alanı (OutlinedTextField). */
val AlanSekli = kesikTasSekli(10.dp)

/** Dialog kabuğu. */
val DialogSekli = kesikTasSekli(24.dp, 6.dp)
