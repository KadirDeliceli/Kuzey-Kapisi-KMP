package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.domain.SubCard
import com.kuzeykapisi.app.ui.components.CoverCard
import com.kuzeykapisi.app.ui.components.EkranBasligi

/** Ana sayfadaki (HomeScreen) geniş ekran eşiğiyle aynı. */
private val GENIS_EKRAN_ESIGI = 600.dp

/** Kartlar büyüdüğü için aralarındaki boşluk da orantılı artırıldı (önceki: 24dp). */
private val ALT_KART_ARASI_BOSLUK = 40.dp

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
                EkranBasligi(
                    baslik = mainCard.ad,
                    etiket = mainCard.altBaslik,
                    geriMetni = "Başlıklara dön",
                    onGeri = onGeri,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                // Breadcrumb/başlık sabit üstte kalır; kart grubu, kalan tüm
                // alanı dolduran bu weight'li kapsayıcı içinde hem yatayda
                // hem dikeyde ortalanır (yalnızca kendi satırında değil,
                // TopBar altındaki tüm viewport'a göre). Satır, sayfanın
                // (Column'un 24dp yatay padding'i içindeki) tüm genişliğini
                // kullanır — kartları önceki sabit 720dp üst sınırdan çok
                // daha büyük gösterir; sayfa kenarlarındaki boşluk zaten bu
                // padding'den ve App.kt'deki 1100dp içerik üst sınırından gelir.
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ALT_KART_ARASI_BOSLUK),
                    ) {
                        for (sub in mainCard.subs) {
                            CoverCard(
                                kategori = "kart",
                                kod = sub.kapak,
                                baslik = sub.ad,
                                onClick = { onSubTiklandi(sub) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(16f / 9f),
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EkranBasligi(
                        baslik = mainCard.ad,
                        etiket = mainCard.altBaslik,
                        geriMetni = "Başlıklara dön",
                        onGeri = onGeri,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                items(mainCard.subs) { sub ->
                    CoverCard(
                        kategori = "kart",
                        kod = sub.kapak,
                        baslik = sub.ad,
                        onClick = { onSubTiklandi(sub) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                }
            }
        }
    }
}
