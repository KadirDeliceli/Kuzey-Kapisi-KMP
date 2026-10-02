package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.domain.SubCard
import com.kuzeykapisi.app.ui.components.AnaKapakKarti
import com.kuzeykapisi.app.ui.components.Breadcrumb
import com.kuzeykapisi.app.ui.components.BreadcrumbBasamagi
import com.kuzeykapisi.app.ui.theme.TELEFON_KIRILIMI
import com.kuzeykapisi.app.ui.theme.TasBeyazi

/** Ana sayfadaki telefon eşiğiyle aynı: altında kartlar alt alta dizilir. */
private val GENIS_EKRAN_ESIGI = TELEFON_KIRILIMI

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
        // Tek üst basamaklı breadcrumb: "Ana Sayfa › {Kategori}" — paylaşılan
        // bileşen (bkz. components/Breadcrumb.kt), ekrana özel kopya değil.
        Breadcrumb(
            ustBasamaklar = listOf(BreadcrumbBasamagi(ad = "Ana Sayfa", onClick = onGeri)),
            aktif = mainCard.ad,
        )
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
