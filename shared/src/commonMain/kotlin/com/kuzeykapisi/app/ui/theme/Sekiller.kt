package com.kuzeykapisi.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * ŞEKİL ÖLÇEĞİ — tam simetrik, yumuşak köşeler.
 *
 * Tek bir kaynaktan üretilir ve TÜM kart/panel/buton tiplerinde (ana kart,
 * alt kart, bot kartı, rota durak kartı, admin liste satırı, form alanı,
 * dialog, mesaj balonu) aynı şekilde uygulanır — böylece uygulamanın her
 * yerinde iki tutarlı köşe yarıçapı görünür: küçük/orta bileşenlerde 16dp,
 * büyük yüzeylerde 20dp.
 */

/** Buton, chip, form alanı, liste satırı, mesaj balonu, harita çerçevesi. */
val ButonSekli = RoundedCornerShape(16.dp)

/** Metin alanı (OutlinedTextField). */
val AlanSekli = RoundedCornerShape(16.dp)

/** Liste satırı, küçük kart, harita çerçevesi, mesaj balonu. */
val SatirSekli = RoundedCornerShape(16.dp)

/** Ana/alt/bot kartları, rota tur kartları. */
val KartSekli = RoundedCornerShape(20.dp)

/** Dialog kabuğu. */
val DialogSekli = RoundedCornerShape(20.dp)
