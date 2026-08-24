package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.AnlatimRozeti
import com.kuzeykapisi.app.ui.components.DuzenleIkonuButonu
import com.kuzeykapisi.app.ui.components.GeriButonu
import com.kuzeykapisi.app.ui.components.OnayDialog
import com.kuzeykapisi.app.ui.components.SilIkonuButonu
import com.kuzeykapisi.app.ui.vm.RotaYerListeViewModel

@Composable
fun RotaYerYonetScreen(
    repo: KuzeyRepository,
    token: String,
    onGeri: () -> Unit,
    onDuzenleTiklandi: (mekan: RotaMekaniAdmin, mevcutAnlatim: String?) -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(repo) { RotaYerListeViewModel(repo) }
    val ui by vm.state.collectAsState()

    LaunchedEffect(vm) { vm.yukle(token) }
    LaunchedEffect(ui.oturumGecersiz) {
        if (ui.oturumGecersiz) onYetkisiz()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        GeriButonu(metin = "Geri", onClick = onGeri)
        Text(
            text = "Rota Yerlerini Yönet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )

        when {
            ui.yukleniyor -> Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.hata != null -> Text(
                text = ui.hata ?: "",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
            )
            ui.mekanlar.isEmpty() -> Text(
                text = "Henüz mekan eklenmedi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ui.mekanlar.forEach { mekan ->
                        RotaYerSatiri(
                            mekan = mekan,
                            duzenlemeYukleniyor = ui.duzenlemeYukleniyorId == mekan.id,
                            onDuzenle = {
                                vm.duzenlemeyiBaslat(mekan, token) { m, anlatim -> onDuzenleTiklandi(m, anlatim) }
                            },
                            onSil = { vm.silmeyiBaslat(mekan) },
                        )
                    }
                }
            }
        }

        val silmeHatasi = ui.silmeHatasi
        if (silmeHatasi != null) {
            Text(
                text = silmeHatasi,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        val duzenlemeHatasi = ui.duzenlemeHatasi
        if (duzenlemeHatasi != null) {
            Text(
                text = duzenlemeHatasi,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    val silinecekMekan = ui.silinecekMekan
    if (silinecekMekan != null) {
        OnayDialog(
            baslik = "Silinsin mi?",
            metin = "'${silinecekMekan.ad}' silinsin mi? Anlatımı da silinir, geri alınamaz.",
            onOnay = { vm.silmeyiOnayla(token) },
            onVazgec = { vm.silmeyiVazgec() },
        )
    }
}

@Composable
private fun RotaYerSatiri(
    mekan: RotaMekaniAdmin,
    duzenlemeYukleniyor: Boolean,
    onDuzenle: () -> Unit,
    onSil: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mekan.ad,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (mekan.anlatimVar) {
                    AnlatimRozeti(modifier = Modifier.padding(start = 6.dp))
                }
            }
            Text(
                text = mekan.aciklama,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (duzenlemeYukleniyor) {
            Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
            }
        } else {
            DuzenleIkonuButonu(onClick = onDuzenle)
        }
        SilIkonuButonu(onClick = onSil)
    }
}
