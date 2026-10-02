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

/** Metin alanı (OutlinedTextField) — ButonSekli ile BİREBİR aynı, tek kaynaktan. */
val AlanSekli = ButonSekli

/** Liste satırı, küçük kart, harita çerçevesi, mesaj balonu — ButonSekli ile BİREBİR aynı, tek kaynaktan. */
val SatirSekli = ButonSekli

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

/** Dialog kabuğu — kart geometrisiyle BİREBİR aynı imza, tek kaynaktan. */
val DialogSekli = KartSekli
