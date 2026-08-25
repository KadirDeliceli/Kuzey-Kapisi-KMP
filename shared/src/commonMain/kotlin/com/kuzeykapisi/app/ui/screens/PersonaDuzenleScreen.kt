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
import com.kuzeykapisi.app.data.model.ADMIN_PERSONA_KATEGORILERI
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.AlanBasligi
import com.kuzeykapisi.app.ui.components.AnlatimiKaldirSecimi
import com.kuzeykapisi.app.ui.components.BasariMetni
import com.kuzeykapisi.app.ui.components.BirincilButon
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataMetni
import com.kuzeykapisi.app.ui.components.IkincilButon
import com.kuzeykapisi.app.ui.components.KuzeyMetinAlani
import com.kuzeykapisi.app.ui.components.SaltOkunurAlan
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.vm.PersonaDuzenleViewModel

@Composable
fun PersonaDuzenleScreen(
    repo: KuzeyRepository,
    detay: PersonaDetay,
    token: String,
    onGeri: () -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(repo, detay) { PersonaDuzenleViewModel(repo, detay.kategori, detay.kod, detay) }
    val ui by vm.state.collectAsState()

    LaunchedEffect(ui.oturumGecersiz) {
        if (ui.oturumGecersiz) onYetkisiz()
    }

    val kategoriEtiketi = ADMIN_PERSONA_KATEGORILERI.firstOrNull { it.first == detay.kategori }?.second
        ?: detay.kategori

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        EkranBasligi(
            baslik = "Düzenle: ${detay.ad}",
            etiket = "Yönetim",
            geriMetni = "Geri",
            onGeri = onGeri,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        SaltOkunurAlan(etiket = "Kategori", deger = kategoriEtiketi)
        Spacer(modifier = Modifier.height(14.dp))
        SaltOkunurAlan(etiket = "Kod (dosya adı)", deger = detay.kod)
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
            etkin = !ui.anlatimiKaldir,
            yardimMetni = "Dokunmadan bırakırsan mevcut anlatım (varsa) korunur.",
            enAzSatir = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        AnlatimiKaldirSecimi(
            isaretli = ui.anlatimiKaldir,
            onDegisti = { vm.anlatimiKaldirDegisti(it) },
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
        if (ui.gorselVar) {
            Text(
                text = "Mevcut bir görsel var. Değiştirmek için yeni bir dosya seçin, " +
                    "dokunmak istemiyorsanız boş bırakın.",
                style = MaterialTheme.typography.bodySmall,
                color = SisGrisi,
                modifier = Modifier.padding(top = 8.dp),
            )
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

