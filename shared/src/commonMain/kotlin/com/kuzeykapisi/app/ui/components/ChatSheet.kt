package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.Mesaj
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.theme.AlcakYuzey
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SatirSekli
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.YuksekYuzey
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
            .background(KaranlikLacivert),
    ) {
        // Başlık çubuğu: dar ekranda panel tam ekran olduğu için durum çubuğu
        // inset'i burada da uygulanır (edge-to-edge). Bu satır klavyeden
        // etkilenmez — sabit üst bölge olarak kalır.
        Column(modifier = Modifier.fillMaxWidth().background(YuksekYuzey)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ui.baslik.ifBlank { "Sohbet" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TasBeyazi,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                KapatButonu(onClick = onKapat)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                FenerAlevi.copy(alpha = 0.30f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (ui.yukleniyor && ui.mesajlar.isEmpty()) {
                YukleniyorGorunumu(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(ui.mesajlar) { mesaj -> MesajBalonu(mesaj) }
                    if (ui.hata != null) {
                        item { HataMetni(ui.hata ?: "") }
                    }
                    if (ui.yaziyor) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                TypingIndicator(
                                    modifier = Modifier
                                        .clip(SatirSekli)
                                        .background(YuksekYuzey)
                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Girdi satırı: klavye açıldığında yalnızca BU bölge yukarı itilir
        // (imePadding), böylece başlık çubuğu yerinde kalır ve yazma kutusu
        // klavyenin hemen üstünde görünür. Klavye kapalıyken gezinme çubuğu
        // inset'i devreye girer; klavye açıkken ime inset'i onu kapsar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AlcakYuzey)
                .imePadding()
                .navigationBarsPadding()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KuzeyMetinAlani(
                deger = girdi,
                onDegisti = { girdi = it },
                ipucu = "Mesajınızı yazın…",
                modifier = Modifier.weight(1f),
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
private fun KapatButonu(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "kapatRengi",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(38.dp)
            .clip(CircleShape)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
            val kalinlik = w * 0.12f
            drawLine(color = renk, start = Offset(0f, 0f), end = Offset(w, h), strokeWidth = kalinlik, cap = StrokeCap.Round)
            drawLine(color = renk, start = Offset(w, 0f), end = Offset(0f, h), strokeWidth = kalinlik, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun GonderButonu(etkin: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val zemin by animateColorAsState(
        targetValue = when {
            !etkin -> FenerAlevi.copy(alpha = 0.22f)
            etkilesim.hoverlu || etkilesim.basili -> FenerAlevi
            else -> FenerAlevi.copy(alpha = 0.92f)
        },
        animationSpec = tween(MIKRO_SURE),
        label = "gonderZemin",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(48.dp)
            .clip(CircleShape)
            .background(zemin)
            .hoverable(interactionSource = interactionSource, enabled = etkin)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = etkin,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(19.dp)) {
            val w = size.width
            val h = size.height
            val yol = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, h / 2f)
                lineTo(0f, h)
                lineTo(w * 0.35f, h / 2f)
                close()
            }
            drawPath(yol, color = KaranlikLacivert.copy(alpha = if (etkin) 1f else 0.55f))
        }
    }
}

/**
 * Mesaj balonları tam yuvarlak, tek bir [SatirSekli] köşe yarıçapı konuşur;
 * konuşmacı yönü köşe kesmekle değil hizalama + renkle okunur — benim
 * mesajım sağa yaslı ve fener alevi kenarlıklı, botunki sola yaslı ve
 * yükseltilmiş yüzey rengiyle, hafif bir gölgeyle bir tık öne çıkar.
 *
 * Benim mesajım fener aleviyle işaretlidir ama DOLU DEĞİLDİR: çok düşük
 * opaklıkta bir zemin + belirgin bir kenarlık. Böylece uzun sohbetlerde bile
 * turuncu geniş bir alan kaplamaz.
 */
@Composable
private fun MesajBalonu(mesaj: Mesaj) {
    val balonSekli = SatirSekli

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mesaj.benden) Alignment.End else Alignment.Start,
    ) {
        when {
            // Sistem notu ("Bağlantı yenilendi…"): balon değil, ortalanmış ve
            // çerçeveli ince bir ayraç — sohbetin akışını bölmez.
            mesaj.sistemNotu -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SatirSekli)
                    .border(1.dp, SisGrisi.copy(alpha = 0.25f), SatirSekli)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = mesaj.metin,
                    color = SisGrisi,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            mesaj.benden -> Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(balonSekli)
                    .background(FenerAlevi.copy(alpha = 0.14f))
                    .border(1.dp, FenerAlevi.copy(alpha = 0.55f), balonSekli)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = mesaj.metin,
                    color = TasBeyazi,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            else -> Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .shadow(3.dp, balonSekli, ambientColor = KaranlikLacivert, spotColor = KaranlikLacivert)
                    .clip(balonSekli)
                    .background(YuksekYuzey)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = mesaj.metin,
                    color = TasBeyazi,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
