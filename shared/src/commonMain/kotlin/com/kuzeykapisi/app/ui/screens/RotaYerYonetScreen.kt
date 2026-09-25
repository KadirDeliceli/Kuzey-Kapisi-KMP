package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.AnlatimRozeti
import com.kuzeykapisi.app.ui.components.BosDurumGorunumu
import com.kuzeykapisi.app.ui.components.DuzenleIkonuButonu
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataMetni
import com.kuzeykapisi.app.ui.components.NabizGostergesi
import com.kuzeykapisi.app.ui.components.OnayDialog
import com.kuzeykapisi.app.ui.components.SilIkonuButonu
import com.kuzeykapisi.app.ui.components.YukleniyorGorunumu
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.SatirSekli
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.vm.RotaYerListeViewModel

/** Web/masaüstünde yönetim listesinin aşırı yayılmasını önleyen üst sınır. */
private val ICERIK_MAX_GENISLIK = 900.dp

@Composable
fun RotaYerYonetScreen(
    repo: KuzeyRepository,
    token: String,
    onGeri: () -> Unit,
    onDuzenleTiklandi: (mekan: RotaMekaniAdmin, mevcutAnlatim: String?) -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = viewModel { RotaYerListeViewModel(repo, token) }
    val ui by vm.state.collectAsState()

    LaunchedEffect(ui.oturumGecersiz) {
        if (ui.oturumGecersiz) onYetkisiz()
    }

    Column(
        modifier = modifier
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
            baslik = "Rota Yerlerini Yönet",
            etiket = "Yönetim",
            geriMetni = "Geri",
            onGeri = onGeri,
            modifier = Modifier.padding(bottom = 28.dp),
        )

        when {
            ui.yukleniyor -> YukleniyorGorunumu(
                modifier = Modifier.fillMaxWidth().height(140.dp),
            )
            ui.hata != null -> HataMetni(ui.hata ?: "")
            ui.mekanlar.isEmpty() -> BosDurumGorunumu("Henüz mekan eklenmedi.")
            else -> {
                Text(
                    text = "${ui.mekanlar.size} mekan",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = SisGrisi,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            HataMetni(silmeHatasi, modifier = Modifier.padding(top = 14.dp))
        }
        val duzenlemeHatasi = ui.duzenlemeHatasi
        if (duzenlemeHatasi != null) {
            HataMetni(duzenlemeHatasi, modifier = Modifier.padding(top = 14.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
    }

    val silinecekMekan = ui.silinecekMekan
    if (silinecekMekan != null) {
        OnayDialog(
            baslik = "Silinsin mi?",
            metin = "'${silinecekMekan.ad}' silinecek. Anlatımı da gider, bu işlem geri alınamaz.",
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
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (etkilesim.hoverlu) 6.dp else 3.dp,
                shape = SatirSekli,
                ambientColor = KaranlikLacivert,
                spotColor = KaranlikLacivert,
            )
            .clip(SatirSekli)
            .background(DerinDeniz)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, SatirSekli)
            .hoverable(interactionSource = interactionSource)
            .padding(start = 18.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mekan.ad,
                    style = MaterialTheme.typography.titleMedium,
                    color = TasBeyazi,
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
                color = SisGrisi,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        if (duzenlemeYukleniyor) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                NabizGostergesi(boyut = 22.dp)
            }
        } else {
            DuzenleIkonuButonu(onClick = onDuzenle)
        }
        SilIkonuButonu(onClick = onSil)
    }
}
