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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuzeykapisi.app.domain.MAIN_CARDS
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.ui.components.CoverCard
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

private val GENIS_EKRAN_ESIGI = 600.dp

/** Tescilli Ürünler kartı — coğrafi işaret mührünü taşıyan tek ana kart. */
private const val TESCIL_KART_ID = "tescil"

@Composable
fun HomeScreen(onKartTiklandi: (MainCard) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 34.dp)) {
                    // Üst etiket: harf aralığı açılarak "eyebrow" hissi verilir;
                    // sayfadaki tek fener alevi metni.
                    Text(
                        text = "SİNOP · KUZEY KAPISI",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.4.sp),
                        color = FenerAlevi,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Başlık: Fraunces, sıkı satır yüksekliği ve negatif harf
                    // aralığıyla editöryel/dergi başlığı; vurgulu kısım italik ve
                    // taş beyazından biraz daha sıcak.
                    val anaRenk = TasBeyazi
                    val vurguRenk = FenerAlevi
                    val heroBaslik = remember(anaRenk, vurguRenk) {
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = anaRenk)) {
                                append("Karadeniz'in kuzey kapısında, ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = vurguRenk,
                                    fontStyle = FontStyle.Italic,
                                )
                            ) {
                                append("her başlığın bir anlatıcısı var.")
                            }
                        }
                    }
                    Text(
                        text = heroBaslik,
                        style = if (genisEkran) {
                            MaterialTheme.typography.displayMedium
                        } else {
                            MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 32.sp,
                                lineHeight = 38.sp,
                            )
                        },
                        modifier = Modifier.widthIn(max = 780.dp),
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    // Açıklama: satır arası ferahlatılır, satır uzunluğu
                    // okunabilirlik için sınırlanır (ölçü/measure).
                    Text(
                        text = "Bir başlık seçin; tarihî bir şahsiyet, bir usta aşçı ya da bir doğa " +
                            "rehberi Sinop'u size kendi diliyle anlatsın.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SisGrisi,
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
                                    AnaKart(
                                        kart = kart,
                                        onKartTiklandi = onKartTiklandi,
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
                            AnaKart(
                                kart = kart,
                                onKartTiklandi = onKartTiklandi,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

/**
 * Ana sayfa kartı — uygulamadaki fener halesini taşıyan iki yerden biri
 * (diğeri birincil CTA butonları). Tescilli Ürünler kartı ayrıca köşesinde
 * coğrafi işaret mührünü taşır.
 */
@Composable
private fun AnaKart(
    kart: MainCard,
    onKartTiklandi: (MainCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        kategori = "kart",
        kod = kart.kapak,
        baslik = kart.ad,
        etiket = kart.altBaslik,
        onClick = { onKartTiklandi(kart) },
        modifier = modifier,
        hale = true,
        muhur = kart.id == TESCIL_KART_ID,
    )
}
