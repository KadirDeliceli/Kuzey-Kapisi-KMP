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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.ADMIN_PERSONA_KATEGORILERI
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.GeriButonu
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
    val vm = remember(repo) { PersonaEkleViewModel(repo) }
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
        GeriButonu(metin = "Geri", onClick = onGeri)
        Text(
            text = "Persona Ekle",
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
                KategoriChip(
                    secili = ui.kategori == kod,
                    etiket = etiket,
                    onClick = { vm.kategoriSec(kod) },
                )
            }
        }
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
            value = ui.kod,
            onValueChange = { vm.kodDegisti(it) },
            label = { Text("Kod (dosya adı)") },
            singleLine = true,
            isError = ui.kodHatasi != null,
            supportingText = {
                val kodHatasi = ui.kodHatasi
                if (kodHatasi != null) {
                    Text(kodHatasi, color = MaterialTheme.colorScheme.tertiary)
                } else {
                    Text("Boş bırakırsan Ad'dan otomatik üretilir. Aynı isimde içerik varsa burayı değiştir.")
                }
            },
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
private fun KategoriChip(secili: Boolean, etiket: String, onClick: () -> Unit) {
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
