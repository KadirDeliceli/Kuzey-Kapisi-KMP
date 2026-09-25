package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.fenerHalesiDestekli
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.default_kapak
import org.jetbrains.compose.resources.painterResource

/**
 * ANA GEZİNME KARTI — alt menüdeki kategori kartları bu bileşenden gelir;
 * ana sayfa kartlarıyla aynı geometriyi ve etkileşim dilini konuşur.
 *
 * [CoverCard]'dan farkı bilinçlidir: burada metin görselin ALTINDA ayrı bir
 * yüzeyde değil, görselin ÜSTÜNDE, alttan yukarı koyulaşan bir degrade
 * perdenin içinde durur. Kart bir "kapak" gibi okunur. [CoverCard] ise liste
 * ekranlarının (bot/persona listesi) kartı olarak DEĞİŞMEDEN kalır.
 *
 * Geometri: imza "elle kesilmiş taş" [KartSekli] — üç köşe 20dp, sağ-alt 4dp.
 * Görsel kartın tamamını doldurur (matchParentSize + Crop, [KartSekli] ile kırpılır).
 *
 * Durumlar:
 *  - durağan  → neredeyse görünmez kenarlık (SisGrisi %10), ölçek 1.0
 *  - hover    → kenarlık [FenerAlevi] 1.5dp; yalnız fareli web'de
 *               ([fenerHalesiDestekli]) ölçek 1.02 ve arkasında fener halesi
 *  - basılı   → ölçek 0.97 (mobilde tek geri bildirim budur)
 *  - odak     → kartın 3dp dışında 2dp TasBeyazi halka (klavye)
 * Geçişlerin tamamı [MIKRO_SURE] (200ms).
 */
@Composable
fun AnaKapakKarti(
    kategori: String,
    kod: String,
    baslik: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etiket: String? = null,
    gorselOran: Float = 4f / 5f,
    muhur: Boolean = false,
    hale: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()

    // Ölçek burada yerel olarak hesaplanır: paylaşılan [kartEtkilesimi]
    // yalnızca basma küçülmesini bilir. Hover büyümesi ana sayfa kartlarıyla
    // aynı kurala bağlıdır: yalnız fareli web'de.
    val olcek by animateFloatAsState(
        targetValue = when {
            etkilesim.basili -> 0.97f
            etkilesim.hoverlu && fenerHalesiDestekli -> 1.02f
            else -> 1f
        },
        animationSpec = tween(MIKRO_SURE),
        label = "kapakKartiOlcegi",
    )

    Box(modifier = modifier.scale(olcek)) {
        if (hale) FenerHalesi(gorunur = etkilesim.hoverlu, sekil = KartSekli)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(gorselOran)
                .odakHalkasi(odakli, KartSekli)
                .shadow(
                    elevation = if (etkilesim.hoverlu || etkilesim.basili) 14.dp else 6.dp,
                    shape = KartSekli,
                    ambientColor = KaranlikLacivert,
                    spotColor = KaranlikLacivert,
                )
                .clip(KartSekli)
                .background(DerinDeniz)
                .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli)
                .hoverable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
        ) {
            AsyncImage(
                model = Config.gorselUrl(kategori, kod),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = painterResource(Res.drawable.default_kapak),
                placeholder = painterResource(Res.drawable.default_kapak),
                modifier = Modifier.matchParentSize(),
            )

            // Alttan yukarı koyulaşan perde: başlığın fotoğraf ne olursa olsun
            // (en kötü durum: bembeyaz görsel) okunmasını sağlar.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.42f to KaranlikLacivert.copy(alpha = 0.30f),
                                0.72f to KaranlikLacivert.copy(alpha = 0.80f),
                                1f to KaranlikLacivert.copy(alpha = 0.96f),
                            ),
                        ),
                    ),
            )

            if (muhur) {
                CografiIsaretMuhru(
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                if (etiket != null) {
                    Text(
                        text = etiket.turkceBuyukHarf(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                        color = FenerAlevi,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                Text(
                    text = baslik,
                    style = MaterialTheme.typography.titleLarge,
                    color = TasBeyazi,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Klavye odağı göstergesi: şeklin 3dp dışında 2dp TasBeyazi halka. Yalnızca
 * çizimdir, yerleşimi değiştirmez; `clip`'ten ÖNCE uygulanmalıdır.
 */
private fun Modifier.odakHalkasi(odakli: Boolean, sekil: Shape): Modifier = drawWithContent {
    drawContent()
    if (odakli) {
        val kalinlik = 2.dp.toPx()
        val pay = 3.dp.toPx() + kalinlik / 2f
        val halka = sekil.createOutline(
            Size(size.width + pay * 2f, size.height + pay * 2f),
            layoutDirection,
            this,
        )
        translate(left = -pay, top = -pay) {
            drawOutline(outline = halka, color = TasBeyazi, style = Stroke(width = kalinlik))
        }
    }
}

/**
 * Türkçe büyük harf: `uppercase()` yerel ayardan bağımsızdır ve "i"yi "I"ya
 * çevirir. i/ı burada elle eşlenir.
 */
private fun String.turkceBuyukHarf(): String = buildString(length) {
    for (harf in this@turkceBuyukHarf) {
        append(
            when (harf) {
                'i' -> 'İ'
                'ı' -> 'I'
                else -> harf.uppercaseChar()
            },
        )
    }
}
