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
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.FormDurumMesajlari
import com.kuzeykapisi.app.ui.components.KaydetButonu
import com.kuzeykapisi.app.ui.components.KuzeyMetinAlani
import com.kuzeykapisi.app.ui.vm.RotaYerEkleViewModel

@Composable
fun RotaYerEkleScreen(
    repo: KuzeyRepository,
    onGeri: () -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = viewModel { RotaYerEkleViewModel(repo) }
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
            baslik = "Rota İçin Yeni Yer Ekle",
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
            yardimMetni = "Doldurursanız sesli dinleme özelliği de eklenir. Boş bırakabilirsiniz.",
            enAzSatir = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))

        FormDurumMesajlari(genelHata = ui.genelHata, basariMesaji = ui.basariMesaji)

        KaydetButonu(
            kaydediliyor = ui.kaydediliyor,
            onClick = { vm.kaydet() },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
