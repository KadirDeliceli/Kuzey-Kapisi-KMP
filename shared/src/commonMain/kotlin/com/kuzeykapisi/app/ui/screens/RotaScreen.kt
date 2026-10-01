package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.model.KategoriBilgi
import com.kuzeykapisi.app.data.model.RotaDurak
import com.kuzeykapisi.app.data.model.RotaYaniti
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.BirincilButon
import com.kuzeykapisi.app.ui.components.BosDurumGorunumu
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.HataGorunumu
import com.kuzeykapisi.app.ui.components.HataMetni
import com.kuzeykapisi.app.ui.components.IkincilButon
import com.kuzeykapisi.app.ui.components.KonumIzniEfekti
import com.kuzeykapisi.app.ui.components.KuzeyChip
import com.kuzeykapisi.app.ui.components.RotaHaritasiWebView
import com.kuzeykapisi.app.ui.components.SesIkonuButonu
import com.kuzeykapisi.app.ui.components.YukleniyorGorunumu
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.components.rememberRotaHaritasiHtml
import com.kuzeykapisi.app.ui.components.tumRotaGoogleMapsUrl
import com.kuzeykapisi.app.ui.nav.VmKapsami
import com.kuzeykapisi.app.ui.theme.AlcakYuzey
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.LocalVeriStili
import com.kuzeykapisi.app.ui.theme.SatirSekli
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.YosunAcik
import com.kuzeykapisi.app.ui.vm.AnlatimKaynagi
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
    // Kategoriler RotaViewModel oluşurken yüklenir; konum + varsayılan rotalar
    // ise konum izni SONUÇLANINCA başlar (izin diyaloğu açıkken konum istenmez).
    val vm = viewModel { RotaViewModel(repo) }
    val ui by vm.state.collectAsState()
    KonumIzniEfekti(onSonuc = { verildi -> vm.konumIzniSonuclandi(verildi) })

    // Rota durağı anlatım overlay'i: RotaDetayGorunumu'nun ÜSTÜNE bindirilir,
    // altındaki içerik (dolayısıyla vm'nin gosterilenRota state'i ve
    // LazyColumn scroll pozisyonu) hiç kaldırılmaz — App.kt'deki sohbet
    // overlay'iyle aynı "state'i canlı tut" yaklaşımı. Açık durağın yalnızca
    // id'si saklanır (döndürmede korunur); durak, ViewModel'deki rotadan bulunur.
    var acikDurakId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Sistem/donanım geri tuşu: önce açık detay görünümünü kapatır (galeriye
    // döner), galerideyken tekrar basılırsa RotaScreen'den çıkılır — App.kt'deki
    // sohbet overlay'inin BackHandler deseniyle aynı yaklaşım. Anlatım overlay'i
    // açıkken AnlatimEkrani'nin KENDİ BackHandler'ı (daha içeride kayıtlı
    // olduğu için) önce devreye girer ve overlay'i kapatır — burada ayrıca
    // ele almaya gerek yok (Screen.Anlatim'de de aynı iç içe BackHandler
    // deseni kullanılıyor).
    BackHandler(enabled = true) {
        if (ui.gosterilenRota != null) vm.detaydanCik() else onGeri()
    }

    val gosterilenRota = ui.gosterilenRota
    val ilkYuklemeHatasi = ui.ilkYuklemeHatasi
    when {
        gosterilenRota != null -> Box(modifier = modifier.fillMaxSize()) {
            val acikDurak = acikDurakId?.let { id -> gosterilenRota.rota.firstOrNull { it.id == id } }
            RotaDetayGorunumu(
                rota = gosterilenRota,
                onGeri = { vm.detaydanCik() },
                onDurakSesTiklandi = { durak -> acikDurakId = durak.id },
                haritaGizli = acikDurak != null,
                modifier = Modifier.fillMaxSize(),
            )
            if (acikDurak != null) {
                // Her durak anlatımının kendi ViewModel kapsamı var (bu ekranın
                // kapsamının altında): overlay kapanınca AnlatimViewModel
                // temizlenir ve ses durur; başka bir durak açılınca yeni bir
                // ViewModel gelir, öncekinin metni gösterilmez.
                VmKapsami(
                    anahtar = "rota-anlatim-${acikDurak.id}",
                    halaGerekli = { acikDurakId == acikDurak.id },
                ) {
                    AnlatimEkrani(
                        repo = repo,
                        kaynak = AnlatimKaynagi.RotaDuragi(mekanId = acikDurak.id),
                        baslik = acikDurak.ad,
                        onGeri = { acikDurakId = null },
                        modifier = Modifier.fillMaxSize().background(KaranlikLacivert),
                    )
                }
            }
        }
        // Konum + varsayılan rotalar tamamlanana kadar galerinin HİÇBİR
        // parçası çizilmez — tek bir tam ekran gösterge yeterli.
        !ui.ilkYuklemeTamamlandi -> YukleniyorGorunumu(
            modifier = modifier.fillMaxSize(),
            metin = Metinler.ROTALAR_HAZIRLANIYOR,
        )
        ilkYuklemeHatasi != null -> HataGorunumu(
            mesaj = ilkYuklemeHatasi,
            modifier = modifier.fillMaxSize(),
            onTekrarDene = { vm.tekrarDene() },
        )
        else -> RotaGaleriGorunumu(vm = vm, ui = ui, onGeri = onGeri, modifier = modifier)
    }
}

/** Bölüm başlığı — galeri ve detay görünümlerinde bölümleri ayırır. */
@Composable
private fun BolumBasligi(metin: String, modifier: Modifier = Modifier) {
    Text(
        text = metin,
        style = MaterialTheme.typography.titleLarge,
        color = TasBeyazi,
        modifier = modifier,
    )
}

/** Form alanı üstündeki küçük etiket. */
@Composable
private fun AltEtiket(metin: String, modifier: Modifier = Modifier) {
    Text(
        text = metin.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
        color = SisGrisi,
        modifier = modifier,
    )
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
        EkranBasligi(
            baslik = "Ne kadar vaktiniz var, ne görmek istiyorsunuz?",
            etiket = "Akıllı Rota",
            geriMetni = "Başlıklara dön",
            onGeri = onGeri,
        )
        Text(
            text = "İsterseniz aşağıdaki hazır turlardan birini seçin, isterseniz sürenizi ve " +
                "ilgi alanlarınızı belirleyip kendi turunuzu oluşturun.",
            style = MaterialTheme.typography.bodyLarge,
            color = SisGrisi,
            modifier = Modifier.padding(top = 10.dp, start = 6.dp).widthIn(max = 640.dp),
        )

        if (ui.hata != null) {
            HataMetni(ui.hata, modifier = Modifier.padding(top = 16.dp, start = 6.dp))
        }

        BolumBasligi(
            metin = "Önerilen Turlar",
            modifier = Modifier.padding(top = 30.dp, bottom = 14.dp, start = 6.dp),
        )
        val varsayilanlar = ui.varsayilanlar
        if (varsayilanlar.isNullOrEmpty()) {
            BosDurumGorunumu(
                mesaj = Metinler.ROTA_ONERILENLER_YUKLENEMIYOR,
                modifier = Modifier.padding(start = 6.dp),
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            ) {
                items(varsayilanlar) { rota ->
                    TurKart(
                        rota = rota,
                        kategoriler = ui.kategoriler,
                        onClick = { vm.rotaGoster(rota) },
                        modifier = Modifier.width(230.dp),
                    )
                }
            }
        }

        BolumBasligi(
            metin = "Kendi Turunuzu Oluşturun",
            modifier = Modifier.padding(top = 34.dp, bottom = 14.dp, start = 6.dp),
        )

        AltEtiket("Süre", modifier = Modifier.padding(start = 6.dp))
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        ) {
            items(SURE_SECENEKLERI.toList()) { saat ->
                KuzeyChip(
                    etiket = "$saat saat",
                    secili = ui.secilenSureSaat == saat,
                    onClick = { vm.sureSec(saat) },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        AltEtiket("İlgi Alanları (en fazla 4)", modifier = Modifier.padding(start = 6.dp))
        Spacer(modifier = Modifier.height(10.dp))
        val kategoriler = ui.kategoriler
        val kategoriHatasi = ui.kategoriHatasi
        if (kategoriler == null && kategoriHatasi != null) {
            // Yükleme başarısız: "yükleniyor"da takılı kalmaz, tekrar denenebilir.
            Column(modifier = Modifier.padding(start = 6.dp)) {
                HataMetni(kategoriHatasi)
                IkincilButon(
                    metin = "Tekrar dene",
                    onClick = { vm.kategorileriTekrarDene() },
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        } else if (kategoriler == null) {
            BosDurumGorunumu(
                mesaj = Metinler.ROTA_KATEGORILER_YUKLENIYOR,
                modifier = Modifier.padding(start = 6.dp),
            )
        } else {
            FlowRow(
                modifier = Modifier.padding(start = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                kategoriler.forEach { (kod, bilgi) ->
                    Column(modifier = Modifier.widthIn(max = 170.dp)) {
                        KuzeyChip(
                            etiket = bilgi.ad,
                            secili = kod in ui.seciliTurler,
                            onClick = { vm.turSec(kod) },
                        )
                        Text(
                            text = bilgi.aciklama,
                            style = MaterialTheme.typography.labelSmall,
                            color = SisGrisi,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 5.dp, start = 4.dp),
                        )
                    }
                }
            }
        }

        if (ui.uyari != null) {
            HataMetni(ui.uyari, modifier = Modifier.padding(top = 12.dp, start = 6.dp))
        }

        BirincilButon(
            metin = if (ui.yukleniyorOzel) "Aranıyor…" else "Ara",
            onClick = { vm.ara() },
            etkin = ui.secilenSureSaat != null && !ui.yukleniyorOzel,
            hale = true,
            modifier = Modifier.padding(top = 22.dp, start = 6.dp),
        )

        val ozelSonuc = ui.ozelSonuc
        if (ozelSonuc != null) {
            BolumBasligi(
                metin = "Aranan Rota",
                modifier = Modifier.padding(top = 28.dp, bottom = 12.dp, start = 6.dp),
            )
            TurKart(
                rota = ozelSonuc,
                kategoriler = ui.kategoriler,
                onClick = { vm.rotaGoster(ozelSonuc) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun TurKart(
    rota: RotaYaniti,
    kategoriler: Map<String, KategoriBilgi>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)

    Column(
        modifier = modifier
            .scale(etkilesim.olcek)
            .clip(KartSekli)
            .background(DerinDeniz)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(18.dp),
    ) {
        Text(
            text = "${rota.sureSaat} Saatlik Tur",
            style = MaterialTheme.typography.titleMedium,
            color = TasBeyazi,
        )
        val kategoriOzeti = rota.tercihKategorisi
            .mapNotNull { kategoriler?.get(it)?.ad }
            .joinToString(", ")
        if (kategoriOzeti.isNotBlank()) {
            Text(
                text = kategoriOzeti,
                style = MaterialTheme.typography.bodySmall,
                color = SisGrisi,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        // Süre ve durak sayısı "veri"dir — JetBrains Mono ile yazılır ve
        // gövde metninden görsel olarak ayrışır.
        Text(
            text = "${rota.sureSaat} sa · ${rota.rota.size} durak",
            style = LocalVeriStili.current,
            color = FenerAlevi,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun RotaDetayGorunumu(
    rota: RotaYaniti,
    onGeri: () -> Unit,
    onDurakSesTiklandi: (RotaDurak) -> Unit,
    haritaGizli: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                EkranBasligi(
                    baslik = "${rota.sureSaat} Saatlik Tur",
                    etiket = "Rota",
                    geriMetni = "Geri",
                    onGeri = onGeri,
                )
                // Özet kutusu: solunda fener aleviyle işaretli ince bir şerit
                // olan alçak yüzey — okuma önceliğini bozmadan öne çıkar.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(IntrinsicSize.Min)
                        .clip(SatirSekli)
                        .background(AlcakYuzey),
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(FenerAlevi),
                    )
                    Text(
                        text = rota.ozet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TasBeyazi.copy(alpha = 0.92f),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        if (rota.rota.isEmpty()) {
            item {
                BosDurumGorunumu(
                    mesaj = Metinler.ROTA_DURAK_BULUNAMADI,
                )
            }
        } else {
            items(rota.rota) { durak ->
                RotaDurakKart(durak = durak, onSesTiklandi = { onDurakSesTiklandi(durak) })
            }

            item {
                RotaHaritasiBolumu(duraklar = rota.rota, gizli = haritaGizli)
            }
        }
    }
}

@Composable
private fun RotaHaritasiBolumu(duraklar: List<RotaDurak>, gizli: Boolean) {
    val uriHandler = LocalUriHandler.current
    val html = rememberRotaHaritasiHtml(duraklar)

    Column(modifier = Modifier.fillMaxWidth()) {
        BolumBasligi(metin = "Rota Haritası", modifier = Modifier.padding(bottom = 12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(SatirSekli)
                .background(DerinDeniz)
                .border(1.dp, SisGrisi.copy(alpha = 0.18f), SatirSekli),
        ) {
            // Anlatım overlay'i (AnlatimEkrani) bu ekranın ÜSTÜNE bindirildiğinde
            // altındaki LazyColumn kompozisyondan çıkmıyor (scroll pozisyonu bilerek
            // korunuyor). Ama haritanın native görünümü (Android WebView, wasmJs
            // <iframe>) Compose'un çizim Z-sırasına uymuyor ve üstteki overlay'in
            // içinden/üstünden sızabiliyor. Bu yüzden overlay açıkken SADECE bu
            // native görünüm kompozisyondan çıkarılır — listenin geri kalanı ve
            // scroll durumu etkilenmez, overlay kapanınca harita normal şekilde
            // geri gelir.
            if (!gizli && html != null) {
                RotaHaritasiWebView(html = html, modifier = Modifier.fillMaxSize())
            }
        }
        BirincilButon(
            metin = "Rotayı Google Maps'te aç",
            onClick = { uriHandler.openUri(tumRotaGoogleMapsUrl(duraklar)) },
            hale = true,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        )
    }
}

@Composable
private fun RotaDurakKart(durak: RotaDurak, onSesTiklandi: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val veriStili = LocalVeriStili.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KartSekli)
            .background(DerinDeniz)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli)
            .hoverable(interactionSource = interactionSource)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Sıra numarası: fener alevi çemberi içinde koyu rakam — küçük ama
            // rotanın omurgasını okunur kılan tek vurgu.
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FenerAlevi),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${durak.sira}",
                    style = veriStili.copy(fontSize = 13.sp),
                    color = KaranlikLacivert,
                )
            }
            Text(
                text = durak.ad,
                style = MaterialTheme.typography.titleMedium,
                color = TasBeyazi,
                modifier = Modifier.weight(1f),
            )
            if (durak.anlatimVar) {
                SesIkonuButonu(onClick = onSesTiklandi)
            }
        }
        Text(
            text = durak.tur.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = YosunAcik,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = durak.aciklama,
            style = MaterialTheme.typography.bodyMedium,
            color = SisGrisi,
            modifier = Modifier.padding(top = 6.dp),
        )

        // Süre/mesafe satırları "veri"dir: JetBrains Mono ile yazılır ve
        // açıklama metninden görsel olarak ayrışır.
        val onceki = if (durak.sira == 1) "Başlangıç konumunuzdan" else "Bir önceki duraktan"
        Column(
            modifier = Modifier.padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            VeriSatiri("${durak.oncekiNoktadanYolDk} dk yol", "$onceki yaklaşık")
            VeriSatiri("${durak.ziyaretSuresiDk} dk gezi", "Bu mekan için tahmini")
            VeriSatiri("${durak.varisToplamDk} dk toplam", "Buraya kadar (yol + gezi)")
        }
        IkincilButon(
            metin = "Google Maps'te aç",
            onClick = { uriHandler.openUri(durak.googleMapsUrl) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/**
 * Bir açıklama + bir sayısal değer. Değer JetBrains Mono ile yazılır (veri),
 * açıklama gövde yazı tipiyle kalır.
 */
@Composable
private fun VeriSatiri(deger: String, aciklama: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = aciklama,
            style = MaterialTheme.typography.bodySmall,
            color = SisGrisi,
        )
        Text(
            text = deger,
            style = LocalVeriStili.current,
            color = TasBeyazi.copy(alpha = 0.85f),
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
