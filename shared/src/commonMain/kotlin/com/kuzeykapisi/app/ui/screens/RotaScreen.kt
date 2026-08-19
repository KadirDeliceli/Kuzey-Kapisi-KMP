package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.RotaDurak
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.GeriButonu
import com.kuzeykapisi.app.ui.components.KonumIzniEfekti
import com.kuzeykapisi.app.ui.components.TypingIndicator
import com.kuzeykapisi.app.ui.vm.RotaViewModel

@Composable
fun RotaScreen(
    repo: KuzeyRepository,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KonumIzniEfekti()

    val vm = remember(repo) { RotaViewModel(repo) }
    val ui by vm.state.collectAsState()
    DisposableEffect(vm) {
        vm.basla()
        onDispose { }
    }

    var girdi by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                GeriButonu(metin = "Başlıklara dön", onClick = onGeri)
                Text(
                    text = "Ne kadar vaktin var, ne görmek istiyorsun?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "Vaktini ve ilgi alanını tek cümlede anlat, senin için bir rota hazırlayalım.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = girdi,
                    onValueChange = { girdi = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Örn: 6 saatim var, müze gezmek istiyorum") },
                )
                RotaGonderButonu(
                    etkin = girdi.isNotBlank() && !ui.yukleniyor,
                    onClick = {
                        if (girdi.isNotBlank()) {
                            vm.gonder(girdi)
                            girdi = ""
                        }
                    },
                )
            }
        }

        if (ui.yukleniyor) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TypingIndicator(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                    )
                    Text(
                        text = "Rota hazırlanıyor…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (ui.hata != null) {
            item {
                Text(
                    text = ui.hata ?: "",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        ui.sonuc?.let { sonuc ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(16.dp),
                ) {
                    Text(
                        text = sonuc.ozet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (sonuc.rota.isEmpty()) {
                item {
                    Text(
                        text = "Bu tercihlere uyan bir durak bulamadık. Vaktini ya da ilgi alanını " +
                            "değiştirerek tekrar dener misin?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(sonuc.rota) { durak -> RotaDurakKart(durak) }
            }
        }
    }
}

@Composable
private fun RotaGonderButonu(etkin: Boolean, onClick: () -> Unit) {
    val zeminRenk = MaterialTheme.colorScheme.primary
    val ikonRenk = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(zeminRenk.copy(alpha = if (etkin) 1f else 0.4f))
            .clickable(enabled = etkin, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val w = size.width
            val h = size.height
            val yol = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, h / 2f)
                lineTo(0f, h)
                lineTo(w * 0.35f, h / 2f)
                close()
            }
            drawPath(yol, color = ikonRenk)
        }
    }
}

@Composable
private fun RotaDurakKart(durak: RotaDurak) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${durak.sira}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(
                text = durak.ad,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = durak.tur,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = durak.aciklama,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "~${durak.oncekiNoktadanYolDk} dk yol · ${durak.ziyaretSuresiDk} dk gezi · " +
                "toplam ${durak.varisToplamDk} dk",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = { uriHandler.openUri(durak.googleMapsUrl) },
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text("Google Maps'te Aç")
        }
    }
}
