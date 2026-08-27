package com.kuzeykapisi.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * ŞEKİL ÖLÇEĞİ.
 *
 * Küçük/orta bileşenler (buton, alan, satır): tam simetrik 16dp — kararlı,
 * nötr bir dil.
 *
 * BÜYÜK YÜZEYLER (kart, dialog): imza geometri — "elle kesilmiş taş".
 * Üç köşe normal (20dp), sağ-alt köşe belirgin küçük (4dp). Kuzey Kapısı'nın
 * taş kale surlarına bir gönderme; tek bir kaynaktan üretilir ve TÜM kart
 * tiplerinde (ana/alt kart, bot kartı, rota durak kartı, tur kartı, dialog
 * kabuğu) birebir aynı şekilde uygulanır — bkz. tokens/borders.json →
 * radius-asymmetric.card.
 */

/** Buton, chip, form alanı, liste satırı, mesaj balonu, harita çerçevesi. */
val ButonSekli = RoundedCornerShape(16.dp)

/** Metin alanı (OutlinedTextField). */
val AlanSekli = RoundedCornerShape(16.dp)

/** Liste satırı, küçük kart, harita çerçevesi, mesaj balonu. */
val SatirSekli = RoundedCornerShape(16.dp)

/**
 * İmza kart geometrisi — ana/alt/bot kartları, rota tur kartları. Üç köşe
 * 20dp, sağ-alt köşe 4dp.
 */
val KartSekli = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 20.dp,
    bottomEnd = 4.dp,
)

/** Dialog kabuğu — kart geometrisiyle aynı imza, aynı sağ-alt kesim. */
val DialogSekli = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 20.dp,
    bottomEnd = 4.dp,
)
