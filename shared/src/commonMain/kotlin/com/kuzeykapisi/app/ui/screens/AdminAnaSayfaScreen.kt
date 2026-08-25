package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

private val GENIS_EKRAN_ESIGI = 600.dp

/** Web/masaüstünde yönetim panelinin aşırı yayılmasını önleyen üst sınır. */
private val ICERIK_MAX_GENISLIK = 900.dp

@Composable
fun AdminAnaSayfaScreen(
    onGeri: () -> Unit,
    onPersonaEkleTiklandi: () -> Unit,
    onRotaYeriEkleTiklandi: () -> Unit,
    onPersonalariYonetTiklandi: () -> Unit,
    onRotaYerleriniYonetTiklandi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = ICERIK_MAX_GENISLIK)
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            EkranBasligi(
                baslik = "Yönetim Paneli",
                etiket = "Yönetim",
                geriMetni = "Geri",
                onGeri = onGeri,
                modifier = Modifier.padding(bottom = 28.dp),
            )

            if (genisEkran) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        AdminKart(
                            baslik = "Persona Ekle",
                            aciklama = "Yeni bir tarihi kişilik, mekan, lezzet, doğa ya da tescilli ürün botu ekle",
                            onClick = onPersonaEkleTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                        AdminKart(
                            baslik = "Personaları Yönet",
                            aciklama = "Mevcut personaları listele, düzenle ya da sil",
                            onClick = onPersonalariYonetTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        AdminKart(
                            baslik = "Rota İçin Yeni Yer Ekle",
                            aciklama = "Akıllı Rota Planlayıcı'nın önerebileceği yeni bir mekan ekle",
                            onClick = onRotaYeriEkleTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                        AdminKart(
                            baslik = "Rota Yerlerini Yönet",
                            aciklama = "Mevcut rota mekanlarını listele, düzenle ya da sil",
                            onClick = onRotaYerleriniYonetTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminKart(
                        baslik = "Persona Ekle",
                        aciklama = "Yeni bir tarihi kişilik, mekan, lezzet, doğa ya da tescilli ürün botu ekle",
                        onClick = onPersonaEkleTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AdminKart(
                        baslik = "Personaları Yönet",
                        aciklama = "Mevcut personaları listele, düzenle ya da sil",
                        onClick = onPersonalariYonetTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AdminKart(
                        baslik = "Rota İçin Yeni Yer Ekle",
                        aciklama = "Akıllı Rota Planlayıcı'nın önerebileceği yeni bir mekan ekle",
                        onClick = onRotaYeriEkleTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AdminKart(
                        baslik = "Rota Yerlerini Yönet",
                        aciklama = "Mevcut rota mekanlarını listele, düzenle ya da sil",
                        onClick = onRotaYerleriniYonetTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}

/**
 * Admin ana ekranı kartı — ana sayfa kartlarıyla aynı yumuşak [KartSekli]
 * formu ve hover kenarlığı, artık zeminden hafifçe ayrışan bir tonal gölgeyle;
 * görselsiz ve halesiz kalır: burası bir iş ekranı, vitrin değil. Sol üstteki
 * yönlü ok rozeti, bir SaaS panosundaki gezinme kutucuğu hissini verir —
 * bu kartın bir yere GÖTÜRDÜĞÜNÜ, salt bilgi vermediğini imler.
 */
@Composable
private fun AdminKart(baslik: String, aciklama: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val vurgulu = etkilesim.hoverlu || etkilesim.basili

    Column(
        modifier = modifier
            .scale(etkilesim.olcek)
            .shadow(
                elevation = if (vurgulu) 12.dp else 6.dp,
                shape = KartSekli,
                ambientColor = KaranlikLacivert,
                spotColor = KaranlikLacivert,
            )
            .clip(KartSekli)
            .background(DerinDeniz)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = baslik,
                style = MaterialTheme.typography.titleLarge,
                color = TasBeyazi,
                modifier = Modifier.weight(1f, fill = false).padding(end = 12.dp),
            )
            YonRozeti(vurgulu = vurgulu)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = aciklama,
            style = MaterialTheme.typography.bodyMedium,
            color = SisGrisi,
        )
    }
}

/** [AdminKart]'ın sağ üstündeki, hover'da fener alevine dönen yönlü ok rozeti. */
@Composable
private fun YonRozeti(vurgulu: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background((if (vurgulu) FenerAlevi else SisGrisi).copy(alpha = if (vurgulu) 0.16f else 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val w = size.width
            val h = size.height
            val renk = if (vurgulu) FenerAlevi else SisGrisi
            val kalinlik = w * 0.14f
            val yol = Path().apply {
                moveTo(w * 0.22f, h * 0.22f)
                lineTo(w * 0.82f, h * 0.5f)
                lineTo(w * 0.22f, h * 0.78f)
            }
            drawPath(
                path = yol,
                color = renk,
                style = Stroke(width = kalinlik, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
