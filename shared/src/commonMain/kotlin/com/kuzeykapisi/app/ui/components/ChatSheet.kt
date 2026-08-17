package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.Mesaj
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.vm.ChatViewModel

@Composable
fun ChatSheet(
    repo: KuzeyRepository,
    kategori: String,
    oge: String,
    onKapat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(kategori, oge) { ChatViewModel(repo, kategori, oge) }
    val ui by vm.state.collectAsState()
    DisposableEffect(vm) {
        vm.basla()
        onDispose { vm.temizle() }
    }

    var girdi by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(ui.mesajlar.size) {
        if (ui.mesajlar.isNotEmpty()) listState.animateScrollToItem(ui.mesajlar.size - 1)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = ui.baslik.ifBlank { "Sohbet" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            IconButton(onClick = onKapat) {
                val kapatRenk = MaterialTheme.colorScheme.onPrimary
                Canvas(modifier = Modifier.size(18.dp)) {
                    val w = size.width
                    val h = size.height
                    val genislik = w * 0.12f
                    drawLine(color = kapatRenk, start = Offset(0f, 0f), end = Offset(w, h), strokeWidth = genislik, cap = StrokeCap.Round)
                    drawLine(color = kapatRenk, start = Offset(w, 0f), end = Offset(0f, h), strokeWidth = genislik, cap = StrokeCap.Round)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (ui.yukleniyor && ui.mesajlar.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(ui.mesajlar) { mesaj -> MesajBalonu(mesaj) }
                    if (ui.hata != null) {
                        item {
                            Text(
                                text = ui.hata ?: "",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    if (ui.yaziyor) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                TypingIndicator(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = girdi,
                onValueChange = { girdi = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mesajınızı yazın...") },
            )
            GonderButonu(
                etkin = girdi.isNotBlank(),
                onClick = {
                    if (girdi.isNotBlank()) {
                        vm.gonder(girdi)
                        girdi = ""
                    }
                },
            )
        }
    }
}

@Composable
private fun GonderButonu(etkin: Boolean, onClick: () -> Unit) {
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
private fun MesajBalonu(mesaj: Mesaj) {
    val hizalama = if (mesaj.benden) Alignment.End else Alignment.Start
    val renk = when {
        mesaj.sistemNotu -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        mesaj.benden -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val metinRenk = when {
        mesaj.sistemNotu -> MaterialTheme.colorScheme.onSurface
        mesaj.benden -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = hizalama) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(renk)
                .padding(12.dp),
        ) {
            Text(text = mesaj.metin, color = metinRenk, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
