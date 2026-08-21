package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.data.model.KategoriBilgi
import com.kuzeykapisi.app.data.model.RotaDurak
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.GeriButonu
import com.kuzeykapisi.app.ui.components.KonumIzniEfekti
import com.kuzeykapisi.app.ui.vm.RotaUiState
import com.kuzeykapisi.app.ui.vm.RotaViewModel
import kotlinx.coroutines.delay

private val SURE_SECENEKLERI = 3..15

// BackHandler, App.kt'deki aynı gerekçeyle (CMP 1.11'de deprecated ama
// wasm/iOS'ta tek çalışan seçenek) burada da opt-in gerektiriyor.
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
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

    // Sistem/donanım geri tuşu: önce açık detay görünümünü kapatır (galeriye
    // döner), galerideyken tekrar basılırsa RotaScreen'den çıkılır — App.kt'deki
    // sohbet overlay'inin BackHandler deseniyle aynı yaklaşım.
    BackHandler(enabled = true) {
        if (ui.gosterilenRota != null) vm.detaydanCik() else onGeri()
    }

    val gosterilenRota = ui.gosterilenRota
    val ilkYuklemeHatasi = ui.ilkYuklemeHatasi
    when {
        gosterilenRota != null -> RotaDetayGorunumu(
            rota = gosterilenRota,
            onGeri = { vm.detaydanCik() },
            modifier = modifier,
        )
        // Konum + varsayılan rotalar tamamlanana kadar galerinin HİÇBİR
        // parçası çizilmez — tek bir tam ekran gösterge yeterli.
        !ui.ilkYuklemeTamamlandi -> RotaTamEkranYukleniyor(modifier = modifier)
        ilkYuklemeHatasi != null -> RotaTamEkranHata(
            mesaj = ilkYuklemeHatasi,
            onTekrarDene = { vm.tekrarDene() },
            modifier = modifier,
        )
        else -> RotaGaleriGorunumu(vm = vm, ui = ui, onGeri = onGeri, modifier = modifier)
    }
}

@Composable
private fun RotaTamEkranYukleniyor(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = "Rotalar hazırlanıyor…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun RotaTamEkranHata(mesaj: String, onTekrarDene: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = mesaj,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Button(onClick = onTekrarDene, modifier = Modifier.padding(top = 12.dp)) {
                Text("Tekrar Dene")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RotaGaleriGorunumu(
    vm: RotaViewModel,
    ui: RotaUiState,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(ui.uyari) {
        if (ui.uyari != null) {
            delay(2500)
            vm.uyariTemizle()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        GeriButonu(metin = "Başlıklara dön", onClick = onGeri)
        Text(
            text = "Ne kadar vaktin var, ne görmek istiyorsun?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "İstersen aşağıdaki hazır turlardan birini seç, istersen süreni ve " +
                "ilgi alanlarını belirleyip kendi turunu oluştur.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (ui.hata != null) {
            Text(
                text = ui.hata,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Text(
            text = "Önerilen Turlar",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
        )
        val varsayilanlar = ui.varsayilanlar
        if (varsayilanlar.isNullOrEmpty()) {
            Text(
                text = "Önerilen turlar şu an yüklenemiyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(varsayilanlar) { rota ->
                    TurKart(
                        rota = rota,
                        kategoriler = ui.kategoriler,
                        onClick = { vm.rotaGoster(rota) },
                        modifier = Modifier.width(220.dp),
                    )
                }
            }
        }

        Text(
            text = "Kendi Turunu Oluştur",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
        )

        Text(
            text = "Süre",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
        ) {
            items(SURE_SECENEKLERI.toList()) { saat ->
                FilterChip(
                    selected = ui.secilenSureSaat == saat,
                    onClick = { vm.sureSec(saat) },
                    label = { Text("$saat saat") },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "İlgi Alanları (en fazla 4)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        val kategoriler = ui.kategoriler
        if (kategoriler == null) {
            Text(
                text = "Kategoriler yükleniyor…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                kategoriler.forEach { (kod, bilgi) ->
                    Column(modifier = Modifier.widthIn(max = 160.dp)) {
                        FilterChip(
                            selected = kod in ui.seciliTurler,
                            onClick = { vm.turSec(kod) },
                            label = { Text(bilgi.ad) },
                        )
                        Text(
                            text = bilgi.aciklama,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp),
                        )
                    }
                }
            }
        }

        if (ui.uyari != null) {
            Text(
                text = ui.uyari,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Button(
            onClick = { vm.ara() },
            enabled = ui.secilenSureSaat != null && !ui.yukleniyorOzel,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(if (ui.yukleniyorOzel) "Aranıyor…" else "Ara")
        }

        val ozelSonuc = ui.ozelSonuc
        if (ozelSonuc != null) {
            Text(
                text = "Aranan Rota",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )
            TurKart(
                rota = ozelSonuc,
                kategoriler = ui.kategoriler,
                onClick = { vm.rotaGoster(ozelSonuc) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TurKart(
    rota: RotaYaniti,
    kategoriler: Map<String, KategoriBilgi>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(
            text = "${rota.sureSaat} Saatlik Tur",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        val kategoriOzeti = rota.tercihKategorisi
            .mapNotNull { kategoriler?.get(it)?.ad }
            .joinToString(", ")
        if (kategoriOzeti.isNotBlank()) {
            Text(
                text = kategoriOzeti,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Text(
            text = "${rota.rota.size} durak",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun RotaDetayGorunumu(
    rota: RotaYaniti,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                GeriButonu(metin = "Geri", onClick = onGeri)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(16.dp)
                        .padding(top = 8.dp),
                ) {
                    Text(
                        text = rota.ozet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        if (rota.rota.isEmpty()) {
            item {
                Text(
                    text = "Bu tercihlere uyan bir durak bulamadık. Süreyi ya da ilgi alanlarını " +
                        "değiştirerek tekrar dener misin?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(rota.rota) { durak -> RotaDurakKart(durak) }
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
        val onceki = if (durak.sira == 1) "Başlangıç konumunuzdan" else "Bir önceki duraktan"
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "$onceki yaklaşık ${durak.oncekiNoktadanYolDk} dk yol mesafesi var.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "Bu mekan için tahmini gezi süreniz ${durak.ziyaretSuresiDk} dk olarak " +
                    "tahmin edilmektedir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "Bu durağa kadar (yol + gezi dahil) şu ana kadar geçirdiğiniz toplam süre: " +
                    "${durak.varisToplamDk} dk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Button(
            onClick = { uriHandler.openUri(durak.googleMapsUrl) },
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text("Google Maps'te Aç")
        }
    }
}
