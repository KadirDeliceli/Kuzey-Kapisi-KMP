package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.kuzeykapisi.app.data.model.ADMIN_PERSONA_KATEGORILERI
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.DuzenleIkonuButonu
import com.kuzeykapisi.app.ui.components.GeriButonu
import com.kuzeykapisi.app.ui.components.OnayDialog
import com.kuzeykapisi.app.ui.components.SilIkonuButonu
import com.kuzeykapisi.app.ui.vm.PersonaListeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonaYonetScreen(
    repo: KuzeyRepository,
    token: String,
    onGeri: () -> Unit,
    onDuzenleTiklandi: (PersonaDetay) -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(repo) { PersonaListeViewModel(repo) }
    val ui by vm.state.collectAsState()

    LaunchedEffect(vm) { vm.yukle() }
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
            text = "Personaları Yönet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )

        Text(
            text = "Kategori",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ADMIN_PERSONA_KATEGORILERI.forEach { (kod, etiket) ->
                YonetimKategoriChip(
                    secili = ui.kategori == kod,
                    etiket = etiket,
                    onClick = { vm.kategoriSec(kod) },
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        when {
            ui.yukleniyor -> Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.hata != null -> Text(
                text = ui.hata ?: "",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
            )
            else -> {
                val ogeler = ui.katalog[ui.kategori]?.ogeler ?: emptyList()
                if (ogeler.isEmpty()) {
                    Text(
                        text = "Bu kategoride henüz içerik yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ogeler.forEach { oge ->
                            PersonaSatiri(
                                oge = oge,
                                duzenlemeYukleniyor = ui.duzenlemeYukleniyorKod == oge.kod,
                                onDuzenle = {
                                    vm.duzenlemeyiBaslat(oge, token) { detay -> onDuzenleTiklandi(detay) }
                                },
                                onSil = { vm.silmeyiBaslat(oge) },
                            )
                        }
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

    val silinecekOge = ui.silinecekOge
    if (silinecekOge != null) {
        OnayDialog(
            baslik = "Silinsin mi?",
            metin = "'${silinecekOge.ad}' silinsin mi? Görsel ve anlatım da silinir, geri alınamaz.",
            onOnay = { vm.silmeyiOnayla(token) },
            onVazgec = { vm.silmeyiVazgec() },
        )
    }
}

@Composable
private fun PersonaSatiri(
    oge: KatalogOge,
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
            Text(
                text = oge.ad,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = oge.kod,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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

@Composable
private fun YonetimKategoriChip(secili: Boolean, etiket: String, onClick: () -> Unit) {
    val zemin = if (secili) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val metinRenk = if (secili) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(zemin)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text = etiket, color = metinRenk, style = MaterialTheme.typography.labelLarge)
    }
}
