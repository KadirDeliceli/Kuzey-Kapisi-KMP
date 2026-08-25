package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.ADMIN_PERSONA_KATEGORILERI
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.AlanBasligi
import com.kuzeykapisi.app.ui.components.BosDurumGorunumu
import com.kuzeykapisi.app.ui.components.DuzenleIkonuButonu
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataMetni
import com.kuzeykapisi.app.ui.components.KuzeyChip
import com.kuzeykapisi.app.ui.components.NabizGostergesi
import com.kuzeykapisi.app.ui.components.OnayDialog
import com.kuzeykapisi.app.ui.components.SilIkonuButonu
import com.kuzeykapisi.app.ui.components.YukleniyorGorunumu
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.LocalVeriStili
import com.kuzeykapisi.app.ui.theme.SatirSekli
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
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
        EkranBasligi(
            baslik = "Personaları Yönet",
            etiket = "Yönetim",
            geriMetni = "Geri",
            onGeri = onGeri,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        AlanBasligi("Kategori")
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ADMIN_PERSONA_KATEGORILERI.forEach { (kod, etiket) ->
                KuzeyChip(
                    etiket = etiket,
                    secili = ui.kategori == kod,
                    onClick = { vm.kategoriSec(kod) },
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))

        when {
            ui.yukleniyor -> YukleniyorGorunumu(
                modifier = Modifier.fillMaxWidth().height(140.dp),
            )
            ui.hata != null -> HataMetni(ui.hata ?: "")
            else -> {
                val ogeler = ui.katalog[ui.kategori]?.ogeler ?: emptyList()
                if (ogeler.isEmpty()) {
                    BosDurumGorunumu("Bu kategoride henüz içerik yok.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            HataMetni(silmeHatasi, modifier = Modifier.padding(top = 14.dp))
        }
        val duzenlemeHatasi = ui.duzenlemeHatasi
        if (duzenlemeHatasi != null) {
            HataMetni(duzenlemeHatasi, modifier = Modifier.padding(top = 14.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    val silinecekOge = ui.silinecekOge
    if (silinecekOge != null) {
        OnayDialog(
            baslik = "Silinsin mi?",
            metin = "'${silinecekOge.ad}' silinecek. Görseli ve anlatımı da gider, bu işlem geri alınamaz.",
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
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SatirSekli)
            .background(DerinDeniz)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, SatirSekli)
            .hoverable(interactionSource = interactionSource)
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
            Text(
                text = oge.ad,
                style = MaterialTheme.typography.titleMedium,
                color = TasBeyazi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Kod bir tanımlayıcıdır — veri yazı tipiyle yazılır.
            Text(
                text = oge.kod,
                style = LocalVeriStili.current,
                color = SisGrisi,
                maxLines = 1,
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
