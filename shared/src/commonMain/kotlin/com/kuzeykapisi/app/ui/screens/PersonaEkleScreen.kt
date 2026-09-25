package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuzeykapisi.app.data.model.ADMIN_PERSONA_KATEGORILERI
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.AlanBasligi
import com.kuzeykapisi.app.ui.components.BasariMetni
import com.kuzeykapisi.app.ui.components.BirincilButon
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataMetni
import com.kuzeykapisi.app.ui.components.IkincilButon
import com.kuzeykapisi.app.ui.components.KuzeyChip
import com.kuzeykapisi.app.ui.components.KuzeyMetinAlani
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.vm.PersonaEkleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonaEkleScreen(
    repo: KuzeyRepository,
    token: String,
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
            yardimMetni = "Boş bırakırsan Ad'dan otomatik üretilir. Aynı isimde içerik varsa burayı değiştir.",
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
            yardimMetni = "Doldurursan sesli dinleme özelliği de eklenir. Boş bırakabilirsin.",
            enAzSatir = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))

        AlanBasligi("Görsel")
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IkincilButon(
                metin = if (ui.gorsel == null) "Görsel Seç" else "Görseli Değiştir",
                onClick = { vm.gorselSec() },
            )
            val secilenGorsel = ui.gorsel
            if (secilenGorsel != null) {
                Text(
                    text = "Seçildi: ${secilenGorsel.dosyaAdi}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SisGrisi,
                )
            }
        }
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
            onClick = { vm.kaydet(token) },
            etkin = !ui.kaydediliyor,
            hale = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
