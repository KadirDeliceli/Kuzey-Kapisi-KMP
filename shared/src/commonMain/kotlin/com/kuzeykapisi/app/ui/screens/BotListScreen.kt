package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.BosDurumGorunumu
import com.kuzeykapisi.app.ui.components.CoverCard
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataGorunumu
import com.kuzeykapisi.app.ui.components.YukleniyorGorunumu
import com.kuzeykapisi.app.ui.vm.CatalogViewModel

private val GENIS_EKRAN_ESIGI = 600.dp

/** Coğrafi işaret mührünü taşıyan tek katalog kategorisi. */
private const val TESCIL_KATEGORISI = "tescil"

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
    val vm = remember(repo) { CatalogViewModel(repo) }
    val ui by vm.state.collectAsState()
    LaunchedEffect(vm) { vm.yukle() }

    Column(modifier = modifier.fillMaxSize()) {
        EkranBasligi(
            baslik = baslik,
            geriMetni = "Geri",
            onGeri = onGeri,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        when {
            ui.yukleniyor -> YukleniyorGorunumu(modifier = Modifier.fillMaxSize())
            ui.hata != null -> HataGorunumu(
                mesaj = ui.hata ?: "Bağlantı kurulamadı.",
                modifier = Modifier.fillMaxSize(),
                onTekrarDene = { vm.yukle() },
            )
            else -> {
                val ogeler = ui.katalog[kategori]?.ogeler ?: emptyList()
                if (ogeler.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                        BosDurumGorunumu(
                            mesaj = "Bu başlıkta henüz içerik yok.",
                            modifier = Modifier.padding(horizontal = 30.dp),
                        )
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
                        val hucreMin = if (genisEkran) 230.dp else 140.dp
                        val oran = when {
                            kategori == "kisiler" -> 3f / 4f
                            !genisEkran -> 4f / 3f
                            else -> 16f / 9f
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(hucreMin),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
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
