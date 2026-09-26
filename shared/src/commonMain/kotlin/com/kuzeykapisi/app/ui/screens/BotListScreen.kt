package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.domain.MAIN_CARDS
import com.kuzeykapisi.app.ui.components.Breadcrumb
import com.kuzeykapisi.app.ui.components.BreadcrumbBasamagi
import com.kuzeykapisi.app.ui.components.CoverCard
import com.kuzeykapisi.app.ui.components.IkincilButon
import com.kuzeykapisi.app.ui.components.YukleniyorGorunumu
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.NotrGeceCizgi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.vm.CatalogViewModel

/** Ana sayfadaki telefon eşiğiyle aynı. */
private val TELEFON_ESIGI = 600.dp

/** Ana sayfada kenar boşluğunun 48dp'ye çıktığı eşik. */
private val GENIS_ESIK = 1200.dp

/**
 * Izgara hücresinin asgari genişliği. Telefonda 140dp: 360-499dp arası 2,
 * 500-599dp arası 3 sütun sığar (320dp'lik çok dar ekranda 1 sütun).
 */
private val DAR_HUCRE_MIN = 140.dp
private val GENIS_HUCRE_MIN = 240.dp

/** Kartlar arası yatay ve dikey boşluk. */
private val KART_ARALIGI = 16.dp

/** Coğrafi işaret mührünü taşıyan tek katalog kategorisi. */
private const val TESCIL_KATEGORISI = "tescil"

/** Portre kartla gösterilen tek kategori; diğerleri yatay. */
private const val KISILER_KATEGORISI = "kisiler"

/** Ana sayfa ile aynı kenar boşluğu standardı. */
private fun kenarBoslugu(genislik: Dp): Dp = when {
    genislik < TELEFON_ESIGI -> 24.dp
    genislik < GENIS_ESIK -> 32.dp
    else -> 48.dp
}

@Composable
fun BotListScreen(
    repo: KuzeyRepository,
    kategori: String,
    baslik: String,
    onGeri: () -> Unit,
    onBotTiklandi: (KatalogOge) -> Unit,
    onSesTiklandi: (KatalogOge) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = viewModel { CatalogViewModel(repo) }
    val ui by vm.state.collectAsState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val darEkran = maxWidth < TELEFON_ESIGI
        val gutter = kenarBoslugu(maxWidth)

        Column(modifier = Modifier.fillMaxSize()) {
            UstBlok(
                kategori = kategori,
                baslik = baslik,
                onGeri = onGeri,
                modifier = Modifier.padding(start = gutter, end = gutter, top = 8.dp, bottom = 16.dp),
            )
            when {
                ui.yukleniyor -> YukleniyorGorunumu(modifier = Modifier.fillMaxSize())
                ui.hata != null -> DurumKutusu(
                    baslik = Metinler.ICERIK_YUKLENEMEDI_BASLIK,
                    // Türüne göre: bağlantı yok / sunucu sorunu / içerik yok...
                    aciklama = ui.hata ?: Metinler.HATA_BILINMEYEN,
                    butonMetni = "Tekrar dene",
                    onButon = { vm.yukle() },
                )
                else -> {
                    val ogeler = ui.katalog[kategori]?.ogeler ?: emptyList()
                    if (ogeler.isEmpty()) {
                        DurumKutusu(
                            baslik = Metinler.BASLIKTA_ICERIK_YOK,
                            aciklama = Metinler.BASLIKTA_ICERIK_YOK_ACIKLAMA,
                            butonMetni = "Başlıklara dön",
                            onButon = onGeri,
                        )
                    } else {
                        // Kişiler portre, diğer tüm kategoriler yatay.
                        val oran = when {
                            kategori == KISILER_KATEGORISI -> 3f / 4f
                            darEkran -> 3f / 2f
                            else -> 16f / 9f
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(if (darEkran) DAR_HUCRE_MIN else GENIS_HUCRE_MIN),
                            contentPadding = PaddingValues(start = gutter, end = gutter, top = 8.dp, bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(KART_ARALIGI),
                            verticalArrangement = Arrangement.spacedBy(KART_ARALIGI),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(ogeler) { oge ->
                                CoverCard(
                                    kategori = kategori,
                                    kod = oge.kod,
                                    baslik = oge.ad,
                                    onClick = { onBotTiklandi(oge) },
                                    modifier = Modifier.fillMaxWidth(),
                                    gorselOran = oran,
                                    anlatimVar = oge.anlatimVar,
                                    onSesTiklandi = { onSesTiklandi(oge) },
                                    hale = true,
                                    muhur = kategori == TESCIL_KATEGORISI,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Breadcrumb + başlık, alt menüdekiyle aynı stil. Üst ana kart (varsa)
 * MAIN_CARDS'tan bulunur: alt menüden gelindiyse yol üç basamaklıdır ve
 * "Ana Sayfa" iki geri adımıdır; doğrudan ana sayfadan gelinen kategoride
 * (tescil) yol iki basamaklıdır.
 */
@Composable
private fun UstBlok(
    kategori: String,
    baslik: String,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ustKart = remember(kategori) {
        MAIN_CARDS.firstOrNull { kart -> kart.subs.any { it.kategori == kategori } }
    }
    val ustBasamaklar = if (ustKart != null) {
        listOf(
            BreadcrumbBasamagi("Ana Sayfa") {
                onGeri()
                onGeri()
            },
            BreadcrumbBasamagi(ustKart.ad, onGeri),
        )
    } else {
        listOf(BreadcrumbBasamagi("Ana Sayfa", onGeri))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Breadcrumb(ustBasamaklar = ustBasamaklar, aktif = baslik)
        Text(
            text = baslik,
            style = MaterialTheme.typography.headlineMedium,
            color = TasBeyazi,
            modifier = Modifier
                .padding(top = 8.dp, start = 6.dp)
                .semantics { heading() },
        )
    }
}

/**
 * Hata ve boş liste durumu: kalan alanın ortasında, ince kenarlıklı sakin bir
 * kutu. Ne olduğunu söyler, ne yapılacağını söyler; özür dilemez.
 */
@Composable
private fun DurumKutusu(
    baslik: String,
    aciklama: String,
    butonMetni: String,
    onButon: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(KartSekli)
                .background(DerinDeniz)
                .border(1.dp, NotrGeceCizgi, KartSekli)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = baslik,
                style = MaterialTheme.typography.titleMedium,
                color = TasBeyazi,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = aciklama,
                style = MaterialTheme.typography.bodyMedium,
                color = SisGrisi,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            IkincilButon(metin = butonMetni, onClick = onButon)
        }
    }
}
