package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.AnlatimiKaldirSecimi
import com.kuzeykapisi.app.ui.components.BasariMetni
import com.kuzeykapisi.app.ui.components.BirincilButon
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataMetni
import com.kuzeykapisi.app.ui.components.KuzeyMetinAlani
import com.kuzeykapisi.app.ui.vm.RotaYerDuzenleViewModel

@Composable
fun RotaYerDuzenleScreen(
    repo: KuzeyRepository,
    mekan: RotaMekaniAdmin,
    mevcutAnlatim: String?,
    onGeri: () -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = viewModel { RotaYerDuzenleViewModel(repo, mekan.id, mekan, mevcutAnlatim) }
    val ui by vm.state.collectAsState()

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
            baslik = "Düzenle: ${mekan.ad}",
            etiket = "Yönetim",
            geriMetni = "Geri",
            onGeri = onGeri,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        KuzeyMetinAlani(
            deger = ui.ad,
            onDegisti = { vm.adDegisti(it) },
            etiket = "Ad",
            tekSatir = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KuzeyMetinAlani(
                deger = ui.enlem,
                onDegisti = { vm.enlemDegisti(it) },
                etiket = "Enlem",
                tekSatir = true,
                modifier = Modifier.weight(1f),
            )
            KuzeyMetinAlani(
                deger = ui.boylam,
                onDegisti = { vm.boylamDegisti(it) },
                etiket = "Boylam",
                tekSatir = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        KuzeyMetinAlani(
            deger = ui.sureDk,
            onDegisti = { vm.sureDkDegisti(it) },
            etiket = "Ziyaret Süresi (dakika)",
            tekSatir = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        KuzeyMetinAlani(
            deger = ui.aciklama,
            onDegisti = { vm.aciklamaDegisti(it) },
            etiket = "Açıklama",
            enAzSatir = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        KuzeyMetinAlani(
            deger = ui.anlatim,
            onDegisti = { vm.anlatimDegisti(it) },
            etiket = "Anlatım Metni (opsiyonel)",
            etkin = !ui.anlatimiKaldir,
            yardimMetni = "Mevcut anlatımın üzerine yazmak için doldurun, dokunmadan bırakırsanız " +
                "mevcut anlatım (varsa) korunur.",
            enAzSatir = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        AnlatimiKaldirSecimi(
            isaretli = ui.anlatimiKaldir,
            onDegisti = { vm.anlatimiKaldirDegisti(it) },
        )
        Spacer(modifier = Modifier.height(24.dp))

        val genelHata = ui.genelHata
        if (genelHata != null) {
            HataMetni(genelHata, modifier = Modifier.padding(bottom = 12.dp))
        }
        val basariMesaji = ui.basariMesaji
        if (basariMesaji != null) {
            BasariMetni(basariMesaji, modifier = Modifier.padding(bottom = 12.dp))
        }

        BirincilButon(
            metin = if (ui.kaydediliyor) "Kaydediliyor…" else "Kaydet",
            onClick = { vm.kaydet() },
            etkin = !ui.kaydediliyor,
            hale = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
