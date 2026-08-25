package com.kuzeykapisi.app.ui.screens

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.data.tts.AnlatimDurumu
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataGorunumu
import com.kuzeykapisi.app.ui.components.YukleniyorGorunumu
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.ButonSekli
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.vm.AnlatimKaynagi
import com.kuzeykapisi.app.ui.vm.AnlatimViewModel

// BackHandler, App.kt/RotaScreen.kt'deki aynı gerekçeyle (CMP 1.11'de
// deprecated ama wasm/iOS'ta tek çalışan seçenek) burada da opt-in gerektiriyor.
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
fun AnlatimEkrani(
    repo: KuzeyRepository,
    kaynak: AnlatimKaynagi,
    baslik: String,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(repo, kaynak) { AnlatimViewModel(repo, kaynak) }
    val ui by vm.state.collectAsState()
    val durum by vm.oynatici.durum.collectAsState()
    val sesHatasi by vm.oynatici.hata.collectAsState()

    LaunchedEffect(vm) { vm.yukle() }
    // Ekrandan her çıkışta (geri tuşu/buton, ekran değişimi) ses MUTLAKA
    // durdurulur ve motor kaynakları serbest bırakılır — arka planda çalmaya
    // devam etmemeli.
    DisposableEffect(vm) {
        onDispose { vm.temizle() }
    }

    BackHandler(enabled = true) { onGeri() }

    Column(modifier = modifier.fillMaxSize()) {
        EkranBasligi(
            baslik = baslik,
            etiket = "Sesli Anlatım",
            geriMetni = "Geri",
            onGeri = onGeri,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )

        when {
            ui.yukleniyor -> YukleniyorGorunumu(modifier = Modifier.fillMaxSize())
            ui.hata != null -> HataGorunumu(
                mesaj = ui.hata ?: "Anlatım yüklenemedi.",
                modifier = Modifier.fillMaxSize(),
                onTekrarDene = { vm.yukle() },
            )
            else -> Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OynatimKontrolleri(
                        durum = durum,
                        onDinle = { ui.metin?.let { vm.oynatici.oynat(it) } },
                        onDuraklat = { vm.oynatici.duraklat() },
                        onDevamEt = { vm.oynatici.devamEt() },
                        onBastanBasla = { ui.metin?.let { vm.oynatici.oynat(it) } },
                    )
                }
                if (sesHatasi != null) {
                    Text(
                        text = sesHatasi ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                // Uzun anlatım metni bir "okuma sütunu" gibi ele alınır:
                // satır uzunluğu sınırlı, ortalanmış, ferah satır arası.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp, bottom = 24.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = ui.metin ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TasBeyazi.copy(alpha = 0.92f),
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    )
                }
            }
        }
    }
}

@Composable
private fun OynatimKontrolleri(
    durum: AnlatimDurumu,
    onDinle: () -> Unit,
    onDuraklat: () -> Unit,
    onDevamEt: () -> Unit,
    onBastanBasla: () -> Unit,
) {
    when (durum) {
        AnlatimDurumu.DURDU -> AnlatimButonu(
            metin = "Dinle",
            simge = AnlatimSimgesi.OYNAT,
            dolgu = true,
            onClick = onDinle,
        )
        AnlatimDurumu.OYNUYOR -> AnlatimButonu(
            metin = "Duraklat",
            simge = AnlatimSimgesi.DURAKLAT,
            dolgu = true,
            onClick = onDuraklat,
        )
        AnlatimDurumu.DURAKLATILDI -> BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // Dar (mobil) ekranlarda iki buton yan yana taşıyor/sıkışıyor —
            // bu genişlikte alt alta (Column, tam genişlik) dizilir; geniş
            // ekranda (web/tablet) yan yana (Row, eşit paylaşımlı) kalır.
            val darEkran = maxWidth < 420.dp
            if (darEkran) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AnlatimButonu(
                        metin = "Devam Et",
                        simge = AnlatimSimgesi.OYNAT,
                        dolgu = true,
                        kompakt = true,
                        onClick = onDevamEt,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AnlatimButonu(
                        metin = "Baştan Başla",
                        simge = AnlatimSimgesi.BASTAN_BASLA,
                        dolgu = false,
                        kompakt = true,
                        onClick = onBastanBasla,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnlatimButonu(
                        metin = "Devam Et",
                        simge = AnlatimSimgesi.OYNAT,
                        dolgu = true,
                        kompakt = true,
                        onClick = onDevamEt,
                        modifier = Modifier.weight(1f),
                    )
                    AnlatimButonu(
                        metin = "Baştan Başla",
                        simge = AnlatimSimgesi.BASTAN_BASLA,
                        dolgu = false,
                        kompakt = true,
                        onClick = onBastanBasla,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private enum class AnlatimSimgesi { OYNAT, DURAKLAT, BASTAN_BASLA }

/**
 * Anlatım kontrol butonu. Dolu hâli fener alevidir (birincil eylem: dinle /
 * duraklat), çerçeveli hâli ikincildir (baştan başla) ve üzerine gelindiğinde
 * kenarı fener alevine döner — tüm kart/buton dilinde olduğu gibi.
 */
@Composable
private fun AnlatimButonu(
    metin: String,
    simge: AnlatimSimgesi,
    dolgu: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kompakt: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val vurgulu = etkilesim.hoverlu || etkilesim.basili

    val zemin by animateColorAsState(
        targetValue = if (vurgulu) FenerAlevi else FenerAlevi.copy(alpha = 0.92f),
        animationSpec = tween(MIKRO_SURE),
        label = "anlatimZemin",
    )
    val cerceveMetin by animateColorAsState(
        targetValue = if (vurgulu) FenerAlevi else TasBeyazi,
        animationSpec = tween(MIKRO_SURE),
        label = "anlatimCerceveMetin",
    )
    val vurguRenk = if (dolgu) KaranlikLacivert else cerceveMetin

    val yatayBosluk = if (kompakt) 16.dp else 24.dp
    val dikeyBosluk = if (kompakt) 12.dp else 15.dp

    Row(
        modifier = modifier
            .scale(etkilesim.olcek)
            .clip(ButonSekli)
            .then(
                if (dolgu) {
                    Modifier.background(zemin)
                } else {
                    Modifier.border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, ButonSekli)
                },
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = yatayBosluk, vertical = dikeyBosluk),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
    ) {
        Canvas(modifier = Modifier.size(if (kompakt) 15.dp else 17.dp)) {
            when (simge) {
                AnlatimSimgesi.DURAKLAT -> {
                    // İki dikey çubuk.
                    val cubukGenisligi = size.width * 0.3f
                    drawRect(
                        color = vurguRenk,
                        topLeft = Offset(0f, 0f),
                        size = Size(cubukGenisligi, size.height),
                    )
                    drawRect(
                        color = vurguRenk,
                        topLeft = Offset(size.width - cubukGenisligi, 0f),
                        size = Size(cubukGenisligi, size.height),
                    )
                }
                AnlatimSimgesi.OYNAT -> {
                    // Sağa bakan üçgen.
                    val w = size.width
                    val h = size.height
                    val yol = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, h / 2f)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(yol, color = vurguRenk)
                }
                AnlatimSimgesi.BASTAN_BASLA -> {
                    // Geri sarma oku: yaklaşık 300°'lik yay + ok başı.
                    val kalinlik = size.width * 0.14f
                    drawArc(
                        color = vurguRenk,
                        startAngle = -40f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = kalinlik, cap = StrokeCap.Round),
                    )
                    val okUcu = Path().apply {
                        moveTo(size.width * 0.62f, -size.height * 0.05f)
                        lineTo(size.width * 1.05f, size.height * 0.18f)
                        lineTo(size.width * 0.78f, size.height * 0.48f)
                        close()
                    }
                    drawPath(okUcu, color = vurguRenk)
                }
            }
        }
        Text(
            text = metin,
            style = MaterialTheme.typography.labelLarge,
            color = if (dolgu) KaranlikLacivert else cerceveMetin,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
