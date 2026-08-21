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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.GeriButonu
import com.kuzeykapisi.app.ui.vm.RotaYerEkleViewModel

@Composable
fun RotaYerEkleScreen(
    repo: KuzeyRepository,
    token: String,
    onGeri: () -> Unit,
    onYetkisiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(repo) { RotaYerEkleViewModel(repo) }
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
            text = "Rota İçin Yeni Yer Ekle",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )

        OutlinedTextField(
            value = ui.ad,
            onValueChange = { vm.adDegisti(it) },
            label = { Text("Ad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = ui.enlem,
                onValueChange = { vm.enlemDegisti(it) },
                label = { Text("Enlem") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = ui.boylam,
                onValueChange = { vm.boylamDegisti(it) },
                label = { Text("Boylam") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ui.sureDk,
            onValueChange = { vm.sureDkDegisti(it) },
            label = { Text("Ziyaret Süresi (dakika)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ui.aciklama,
            onValueChange = { vm.aciklamaDegisti(it) },
            label = { Text("Açıklama") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
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
