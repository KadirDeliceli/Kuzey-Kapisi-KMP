package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.kuzeykapisi.app.data.model.ADMIN_PERSONA_KATEGORILERI
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.AlanBasligi
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.FormDurumMesajlari
import com.kuzeykapisi.app.ui.components.GorselSeciciAlani
import com.kuzeykapisi.app.ui.components.KaydetButonu
import com.kuzeykapisi.app.ui.components.KuzeyChip
import com.kuzeykapisi.app.ui.components.KuzeyMetinAlani
import com.kuzeykapisi.app.ui.vm.PersonaEkleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonaEkleScreen(
    repo: KuzeyRepository,
    onGeri: () -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = viewModel { PersonaEkleViewModel(repo) }
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
            baslik = "Persona Ekle",
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

        KuzeyMetinAlani(
            deger = ui.ad,
            onDegisti = { vm.adDegisti(it) },
            etiket = "Ad",
            tekSatir = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        KuzeyMetinAlani(
            deger = ui.kod,
            onDegisti = { vm.kodDegisti(it) },
            etiket = "Kod (dosya adı)",
            tekSatir = true,
            hataMetni = ui.kodHatasi,
            yardimMetni = "Boş bırakırsanız Ad'dan otomatik üretilir. Aynı isimde içerik varsa burayı değiştirin.",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        KuzeyMetinAlani(
            deger = ui.karsilama,
            onDegisti = { vm.karsilamaDegisti(it) },
            etiket = "Açılış Mesajı (Karşılama)",
            enAzSatir = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        KuzeyMetinAlani(
            deger = ui.icerik,
            onDegisti = { vm.icerikDegisti(it) },
            etiket = "Detaylı İçerik",
            yardimMetni = "Bu botun bilgi kaynağı olacak. Kimlik/tarihçe, karakter/üslup, Sinop'a " +
                "katkısı gibi bölümler halinde mümkün olduğunca detaylı yazın — bot " +
                "yalnızca burada yazdıklarınızı bilecek.",
            enAzSatir = 8,
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
        Spacer(modifier = Modifier.height(20.dp))

        GorselSeciciAlani(gorsel = ui.gorsel, onGorselSec = { vm.gorselSec() })
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
