package com.kuzeykapisi.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.domain.SubCard
import com.kuzeykapisi.app.ui.components.AnaKapakKarti
import com.kuzeykapisi.app.ui.theme.ButonSekli
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

/** Ana sayfadaki telefon eşiğiyle aynı: altında kartlar alt alta dizilir. */
private val GENIS_EKRAN_ESIGI = 600.dp

/** İki alt kart arasındaki boşluk. */
private val ALT_KART_ARASI_BOSLUK = 32.dp

/**
 * Kart grubunun, ortalandığı alanın üstünde ve altında bıraktığı asgari pay —
 * kısa ekranda kartlar başlığa ya da Footer'a yapışmasın.
 */
private val DIKEY_NEFES = 16.dp

/** Alt kart oranı (genişlik / yükseklik): önceki kart boyutunu (~506x355dp) koruyan yatay kapak. */
private const val KART_ORANI = 4f / 3f

@Composable
fun SubMenuScreen(
    mainCard: MainCard,
    onGeri: () -> Unit,
    onSubTiklandi: (SubCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth >= GENIS_EKRAN_ESIGI) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)) {
                UstBlok(mainCard = mainCard, onGeri = onGeri, modifier = Modifier.padding(bottom = 16.dp))

                // Breadcrumb/başlık sabit üstte kalır; kart grubu, kalan tüm
                // alanı dolduran bu weight'li kapsayıcı içinde hem yatayda hem
                // dikeyde ortalanır (TopBar altındaki tüm viewport'a göre).
                // Kart genişliği hem yatay alana hem de kalan YÜKSEKLİĞE
                // sığacak şekilde seçilir: geniş ekranda kartlar tam boy
                // (~510x382dp), kısa ekranda başlığa binmeden küçülür.
                BoxWithConstraints(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val adet = mainCard.subs.size.coerceAtLeast(1)
                    val yatayPay = (maxWidth - ALT_KART_ARASI_BOSLUK * (adet - 1)) / adet
                    val dikeyPay = (maxHeight - DIKEY_NEFES * 2) * KART_ORANI
                    val kartGenisligi = minOf(yatayPay, dikeyPay).coerceAtLeast(0.dp)

                    Row(horizontalArrangement = Arrangement.spacedBy(ALT_KART_ARASI_BOSLUK)) {
                        for (sub in mainCard.subs) {
                            AnaKapakKarti(
                                kategori = "kart",
                                kod = sub.kapak,
                                baslik = sub.ad,
                                onClick = { onSubTiklandi(sub) },
                                modifier = Modifier.width(kartGenisligi),
                                gorselOran = KART_ORANI,
                            )
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    UstBlok(mainCard = mainCard, onGeri = onGeri, modifier = Modifier.padding(bottom = 12.dp))
                }
                items(mainCard.subs) { sub ->
                    AnaKapakKarti(
                        kategori = "kart",
                        kod = sub.kapak,
                        baslik = sub.ad,
                        onClick = { onSubTiklandi(sub) },
                        modifier = Modifier.fillMaxWidth(),
                        gorselOran = KART_ORANI,
                    )
                }
            }
        }
    }
}

/**
 * Üst blok: breadcrumb + ekran başlığı. Breadcrumb'daki "Ana Sayfa" eski
 * "Başlıklara dön" bağlantısının yerini alır (aynı eylem: bir seviye geri).
 * Başlık, diğer alt ekranlardaki [com.kuzeykapisi.app.ui.components.EkranBasligi]
 * ile aynı stil ve hizadadır.
 */
@Composable
private fun UstBlok(mainCard: MainCard, onGeri: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Breadcrumb(aktif = mainCard.ad, onAnaSayfa = onGeri)
        Text(
            text = mainCard.ad,
            style = MaterialTheme.typography.headlineMedium,
            color = TasBeyazi,
            modifier = Modifier
                .padding(top = 8.dp, start = 6.dp)
                .semantics { heading() },
        )
    }
}

/**
 * "Ana Sayfa › {Kategori}". Bağlantı SisGrisi, üzerine gelince/basılınca
 * TasBeyazi + alt çizgi; aktif (son) öğe FenerAlevi ve tıklanamaz. Ayraç
 * ince bir chevron çizgisidir, ekran okuyucuya okunmaz.
 */
@Composable
private fun Breadcrumb(aktif: String, onAnaSayfa: () -> Unit, modifier: Modifier = Modifier) {
    val kaynak = remember { MutableInteractionSource() }
    val hoverlu by kaynak.collectIsHoveredAsState()
    val basili by kaynak.collectIsPressedAsState()
    val odakli by kaynak.collectIsFocusedAsState()
    val vurgulu = hoverlu || basili
    val baglantiRengi by animateColorAsState(
        targetValue = if (vurgulu) TasBeyazi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "breadcrumbBaglantisi",
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .odakHalkasi(odakli, ButonSekli)
                .clip(ButonSekli)
                .hoverable(interactionSource = kaynak)
                .clickable(
                    interactionSource = kaynak,
                    indication = null,
                    role = Role.Button,
                    onClick = onAnaSayfa,
                )
                .heightIn(min = 40.dp)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Ana Sayfa",
                style = MaterialTheme.typography.labelLarge.copy(
                    textDecoration = if (vurgulu) TextDecoration.Underline else TextDecoration.None,
                ),
                color = baglantiRengi,
            )
        }

        Canvas(modifier = Modifier.padding(horizontal = 2.dp).size(14.dp)) {
            val b = size.width / 24f
            val yol = Path().apply {
                moveTo(9f * b, 6f * b)
                lineTo(15f * b, 12f * b)
                lineTo(9f * b, 18f * b)
            }
            drawPath(
                path = yol,
                color = SisGrisi,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }

        Text(
            text = aktif,
            style = MaterialTheme.typography.labelLarge,
            color = FenerAlevi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 6.dp)
                .semantics { stateDescription = "geçerli sayfa" },
        )
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
