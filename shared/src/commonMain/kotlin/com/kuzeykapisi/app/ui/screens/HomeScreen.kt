package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuzeykapisi.app.domain.MAIN_CARDS
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.ui.components.CoverCard
import com.kuzeykapisi.app.ui.components.Footer

private val GENIS_EKRAN_ESIGI = 600.dp

@Composable
fun HomeScreen(onKartTiklandi: (MainCard) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
        val viewportYuksekligi = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Sticky footer: iç kolon en az viewport kadar yüksek olur ve
            // SpaceBetween ile artan boşluğu içerik ile footer arasına dağıtır.
            // İçerik kısaysa footer en alta itilir, uzunsa normal akışta kalır.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = viewportYuksekligi),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
                // Üst etiket: harf aralığı açılarak "eyebrow" hissi verilir.
                Text(
                    text = "SİNOP · KUZEY KAPISI",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.8.sp),
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Başlık: sıkı satır yüksekliği + hafif negatif harf aralığı ile
                // editöryel/dergi başlığı görünümü; vurgulu kısım petrol + italik.
                val anaRenk = MaterialTheme.colorScheme.primary
                val vurguRenk = MaterialTheme.colorScheme.secondary
                val heroBaslik = remember(anaRenk, vurguRenk) {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = anaRenk)) {
                            append("Karadeniz'in kuzey kapısında, ")
                        }
                        withStyle(
                            SpanStyle(
                                color = vurguRenk,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                            )
                        ) {
                            append("her başlığın bir anlatıcısı var.")
                        }
                    }
                }
                Text(
                    text = heroBaslik,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        lineHeight = 38.sp,
                        letterSpacing = (-0.6).sp,
                    ),
                    modifier = Modifier.widthIn(max = 720.dp),
                )
                Spacer(modifier = Modifier.height(14.dp))
                // Açıklama: satır arası ferahlatılır, satır uzunluğu okunabilirlik
                // için sınırlanır (ölçü/measure).
                Text(
                    text = "Bir başlık seçin; tarihî bir şahsiyet, bir usta aşçı ya da bir doğa " +
                        "rehberi Sinop'u size kendi diliyle anlatsın.",
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = 620.dp),
                )
            }
            if (genisEkran) {
                // 4 ana kart 2x2 ızgara olarak gösterilir; kart oranı/boyutu
                // önceki 3'lü tek satırdaki ile aynı kalır (16:9, eşit genişlik).
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    for (satir in MAIN_CARDS.chunked(2)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            for (kart in satir) {
                                CoverCard(
                                    kategori = "kart",
                                    kod = kart.kapak,
                                    baslik = kart.ad,
                                    etiket = kart.altBaslik,
                                    onClick = { onKartTiklandi(kart) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(16f / 9f),
                                )
                            }
                            if (satir.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    for (kart in MAIN_CARDS) {
                        CoverCard(
                            kategori = "kart",
                            kod = kart.kapak,
                            baslik = kart.ad,
                            etiket = kart.altBaslik,
                            onClick = { onKartTiklandi(kart) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                        )
                    }
                }
            }
            }
                Footer(modifier = Modifier.fillMaxWidth().padding(top = 24.dp))
            }
        }
    }
}
