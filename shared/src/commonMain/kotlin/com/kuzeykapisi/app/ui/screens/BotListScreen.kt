package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.kuzeykapisi.app.ui.components.CoverCard
import com.kuzeykapisi.app.ui.components.GeriButonu
import com.kuzeykapisi.app.ui.vm.CatalogViewModel

private val GENIS_EKRAN_ESIGI = 600.dp

@Composable
fun BotListScreen(
    repo: KuzeyRepository,
    kategori: String,
    baslik: String,
    onGeri: () -> Unit,
    onBotTiklandi: (KatalogOge) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(repo) { CatalogViewModel(repo) }
    val ui by vm.state.collectAsState()
    LaunchedEffect(vm) { vm.yukle() }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            GeriButonu(metin = "Geri", onClick = onGeri)
            Text(
                text = baslik,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        when {
            ui.yukleniyor -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.hata != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(ui.hata ?: "", color = MaterialTheme.colorScheme.tertiary)
            }
            else -> {
                val ogeler = ui.katalog[kategori]?.ogeler ?: emptyList()
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
                    val hucreMin = if (genisEkran) 230.dp else 140.dp
                    val oran = when {
                        !genisEkran -> 4f / 3f
                        kategori == "kisiler" -> 3f / 4f
                        else -> 16f / 9f
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(hucreMin),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(ogeler) { oge ->
                            CoverCard(
                                kategori = kategori,
                                kod = oge.kod,
                                baslik = oge.ad,
                                onClick = { onBotTiklandi(oge) },
                                modifier = Modifier.aspectRatio(oran),
                            )
                        }
                    }
                }
            }
        }
    }
}
