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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.kuzeykapisi.app.ui.components.GeriButonu
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
        GeriButonu(metin = "Geri", onClick = onGeri)
        Text(
            text = "Düzenle: ${detay.ad}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )

        SaltOkunurAlan(etiket = "Kategori", deger = kategoriEtiketi)
        Spacer(modifier = Modifier.height(12.dp))
        SaltOkunurAlan(etiket = "Kod (dosya adı)", deger = detay.kod)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = ui.ad,
            onValueChange = { vm.adDegisti(it) },
            label = { Text("Ad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ui.karsilama,
            onValueChange = { vm.karsilamaDegisti(it) },
            label = { Text("Açılış Mesajı (Karşılama)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ui.icerik,
            onValueChange = { vm.icerikDegisti(it) },
            label = { Text("Detaylı İçerik") },
            supportingText = {
                Text(
                    "Bu botun bilgi kaynağı olacak. Kimlik/tarihçe, karakter/üslup, Sinop'a " +
                        "katkısı gibi bölümler halinde mümkün olduğunca detaylı yazın — bot " +
                        "yalnızca burada yazdıklarınızı bilecek.",
                )
            },
            minLines = 8,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ui.anlatim,
            onValueChange = { vm.anlatimDegisti(it) },
            label = { Text("Anlatım Metni (opsiyonel)") },
            enabled = !ui.anlatimiKaldir,
            supportingText = {
                Text("Dokunmadan bırakırsan mevcut anlatım (varsa) korunur.")
            },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Checkbox(checked = ui.anlatimiKaldir, onCheckedChange = { vm.anlatimiKaldirDegisti(it) })
            Text(
                text = "Anlatımı kaldır",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Görsel",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = { vm.gorselSec() }) {
                Text(if (ui.gorsel == null) "Görsel Seç" else "Görseli Değiştir")
            }
            val secilenGorsel = ui.gorsel
            if (secilenGorsel != null) {
                Text(
                    text = "Seçildi: ${secilenGorsel.dosyaAdi}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (ui.gorselVar) {
            Text(
                text = "Mevcut bir görsel var. Değiştirmek için yeni bir dosya seçin, " +
                    "dokunmak istemiyorsanız boş bırakın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        val genelHata = ui.genelHata
        if (genelHata != null) {
            Text(
                text = genelHata,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        val basariMesaji = ui.basariMesaji
        if (basariMesaji != null) {
            Text(
                text = basariMesaji,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Button(
            onClick = { vm.kaydet(token) },
            enabled = !ui.kaydediliyor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (ui.kaydediliyor) "Kaydediliyor…" else "Kaydet")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SaltOkunurAlan(etiket: String, deger: String) {
    Column {
        Text(
            text = etiket,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = deger,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
