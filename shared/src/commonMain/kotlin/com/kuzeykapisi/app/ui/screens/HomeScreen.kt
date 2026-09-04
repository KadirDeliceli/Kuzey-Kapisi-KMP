package com.kuzeykapisi.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.domain.MAIN_CARDS
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.ui.components.CografiIsaretMuhru
import com.kuzeykapisi.app.ui.components.CoverCard
import com.kuzeykapisi.app.ui.components.FenerHalesi
import com.kuzeykapisi.app.ui.components.KartEtkilesimi
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import kotlinx.coroutines.launch
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.default_kapak
import kuzeykapisiapp.shared.generated.resources.sinop_arkaplan
import org.jetbrains.compose.resources.painterResource

/** Hero başlığının büyük/küçük tipografiye geçtiği eşik. */
private val GENIS_EKRAN_ESIGI = 600.dp

/**
 * Zig-zag (fermuar) satır düzeninin devreye girdiği eşik. Bunun altında satır
 * görsel+metni yan yana sıkıştırmak yerine, [CoverCard] ile alt alta dizilen
 * sade bir tek sütuna düşer — dar ekranda iki yarıya bölünmüş bir satır
 * okunaksız olurdu.
 */
private val ZIGZAG_ESIGI = 760.dp

/** Web/masaüstünde içeriğin aşırı yayılmasını önleyen, ortalanmış üst sınır. */
private val ICERIK_MAX_GENISLIK = 1000.dp

/** Tescilli Ürünler kartı — coğrafi işaret mührünü taşıyan tek ana kart. */
private const val TESCIL_KART_ID = "tescil"

/**
 * Hero'nun kapladığı viewport yüksekliği oranı — Apple.com tarzı tam-ekran
 * fotoğraf hissi için ~%88, ama Footer/TopBar'ın altında/üstünde kalan payı
 * tamamen yutmayacak kadar bırakır.
 */
private const val HERO_YUKSEKLIK_ORANI = 0.88f

@Composable
fun HomeScreen(onKartTiklandi: (MainCard) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
        val zigzag = maxWidth >= ZIGZAG_ESIGI
        val heroYuksekligi = maxHeight * HERO_YUKSEKLIK_ORANI
        val yogunluk = LocalDensity.current
        val kaydirmaDurumu = rememberScrollState()
        val kaydirmaKapsami = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(kaydirmaDurumu),
        ) {
            // --- HERO: tam-ekran, kesintisiz fotoğraf; yazı doğrudan üzerinde --
            HeroBolumu(
                genisEkran = genisEkran,
                onKesfetTiklandi = {
                    kaydirmaKapsami.launch {
                        kaydirmaDurumu.animateScrollTo(with(yogunluk) { heroYuksekligi.roundToPx() })
                    }
                },
                modifier = Modifier.height(heroYuksekligi),
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .widthIn(max = ICERIK_MAX_GENISLIK)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    // Hero ile kartlar arasındaki belirgin, kasıtlı boşluk.
                    Spacer(modifier = Modifier.height(48.dp))

                    // --- İÇERİK: zig-zag satırlar (geniş) / tek sütun (dar) --
                    // Kartlar doğrudan, animasyonsuz ve koşulsuz render edilir —
                    // görünürlük takibi/gecikmeli belirme YOKTUR; bu satırlar
                    // her zaman, kararlı biçimde ekrandadır.
                    if (zigzag) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MAIN_CARDS.forEachIndexed { index, kart ->
                                AnaSatir(
                                    kart = kart,
                                    ters = index % 2 == 1,
                                    onClick = { onKartTiklandi(kart) },
                                )
                                if (index != MAIN_CARDS.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 28.dp)
                                            .height(1.dp)
                                            .background(SisGrisi.copy(alpha = 0.14f)),
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            for (kart in MAIN_CARDS) {
                                AnaKart(
                                    kart = kart,
                                    onKartTiklandi = onKartTiklandi,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

/**
 * HERO — Apple.com ürün sayfaları tarzı tam-genişlik/tam-yükseklik fotoğraf.
 * Metin doğrudan fotoğrafın üzerinde durur; arkasında kart/kutu/düz zemin
 * YOKTUR. Okunabilirlik yalnızca metnin oturduğu alt bölgede, alttan yukarı
 * kararan yerel bir gradientle sağlanır — fotoğrafın tamamı asla bulanıklaşmaz.
 *
 * NOT — GÖRSEL: `sinop_arkaplan.jpg` projede zaten mevcut (uygulamanın genel
 * arka planında da kullanılıyor, bkz. App.kt). Daha net/farklı bir Sinop
 * fotoğrafı eklemek isterseniz aynı dosyayı
 * `shared/src/commonMain/composeResources/drawable/sinop_arkaplan.jpg`
 * yolunda değiştirmeniz yeterli — kod tarafında başka hiçbir değişiklik
 * gerekmez.
 */
@Composable
private fun HeroBolumu(
    genisEkran: Boolean,
    onKesfetTiklandi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(Res.drawable.sinop_arkaplan),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )

        // Metnin hemen arkasında, YALNIZCA o dar bölgede kararan yerel perde.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, KaranlikLacivert.copy(alpha = 0.82f)),
                    ),
                ),
        )

        Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .widthIn(max = ICERIK_MAX_GENISLIK)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = if (genisEkran) 64.dp else 36.dp,
                    ),
            ) {
                Text(
                    text = "SİNOP · KUZEY KAPISI",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.4.sp),
                    color = FenerAlevi,
                )
                Spacer(modifier = Modifier.height(24.dp))
                val anaRenk = TasBeyazi
                val vurguRenk = FenerAlevi
                val heroBaslik = remember(anaRenk, vurguRenk) {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = anaRenk)) {
                            append("Karadeniz'in kuzey kapısında, ")
                        }
                        withStyle(
                            SpanStyle(
                                color = vurguRenk,
                                fontStyle = FontStyle.Italic,
                            )
                        ) {
                            append("her başlığın bir anlatıcısı var.")
                        }
                    }
                }
                Text(
                    text = heroBaslik,
                    style = if (genisEkran) {
                        MaterialTheme.typography.displayLarge
                    } else {
                        MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            lineHeight = 38.sp,
                        )
                    },
                    modifier = Modifier.widthIn(max = 820.dp),
                )
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = "Bir başlık seçin; tarihî bir şahsiyet, bir usta aşçı ya da bir doğa " +
                        "rehberi Sinop'u size kendi diliyle anlatsın.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SisGrisi,
                    modifier = Modifier.widthIn(max = 640.dp),
                )
                Spacer(modifier = Modifier.height(28.dp))
                KesfetButonu(onClick = onKesfetTiklandi)
            }
        }
    }
}

/**
 * "Keşfet" — hap biçimli, sade bir CTA. Tıklanınca sayfa hemen altındaki
 * kartlara doğru yumuşak kayar. Ok, hover/basmada hafifçe aşağı kayar —
 * projedeki diğer "Keşfet →" ok mikro-etkileşimiyle aynı dil ([AnaSatirMetni]).
 */
@Composable
private fun KesfetButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val okKaymasi by animateDpAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) 4.dp else 0.dp,
        animationSpec = tween(MIKRO_SURE),
        label = "kesfetButonuOku",
    )
    val hapSekli = RoundedCornerShape(percent = 50)

    Row(
        modifier = modifier
            .scale(etkilesim.olcek)
            .clip(hapSekli)
            .background(KaranlikLacivert.copy(alpha = 0.38f), hapSekli)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, hapSekli)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Keşfet", style = MaterialTheme.typography.labelLarge, color = TasBeyazi)
        Text(
            text = "↓",
            style = MaterialTheme.typography.labelLarge,
            color = TasBeyazi,
            modifier = Modifier.offset(y = okKaymasi),
        )
    }
}

/**
 * Zig-zag (fermuar) satırı — geniş ekranda ana kartların gösterildiği biçim.
 * Görsel bir yanda, başlık+açıklama diğer yanda; [ters] true olduğunda taraflar
 * değişir (çift index'te görsel solda, tek index'te görsel sağda). Satırın
 * TAMAMI tek bir tıklanabilir hedef, tek bir [kartEtkilesimi] paylaşır —
 * görselin üstüne gelmek de metnin üstüne gelmek de aynı hover/basma tepkisini
 * (kenarlık, gölge, "Keşfet" okunun kayması) tetikler.
 */
@Composable
private fun AnaSatir(
    kart: MainCard,
    ters: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val gorselBlok: @Composable () -> Unit = {
            AnaSatirGorseli(
                kart = kart,
                etkilesim = etkilesim,
                modifier = Modifier.weight(1f),
            )
        }
        val metinBlok: @Composable () -> Unit = {
            AnaSatirMetni(
                kart = kart,
                etkilesim = etkilesim,
                modifier = Modifier.weight(1f),
            )
        }
        if (ters) {
            metinBlok()
            gorselBlok()
        } else {
            gorselBlok()
            metinBlok()
        }
    }
}

@Composable
private fun AnaSatirGorseli(
    kart: MainCard,
    etkilesim: KartEtkilesimi,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.scale(etkilesim.olcek)) {
        FenerHalesi(gorunur = etkilesim.hoverlu, sekil = KartSekli)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .shadow(
                    elevation = if (etkilesim.hoverlu || etkilesim.basili) 14.dp else 8.dp,
                    shape = KartSekli,
                    ambientColor = KaranlikLacivert,
                    spotColor = KaranlikLacivert,
                )
                .clip(KartSekli)
                .background(DerinDeniz)
                .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli),
        ) {
            AsyncImage(
                model = Config.gorselUrl("kart", kart.kapak),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = painterResource(Res.drawable.default_kapak),
                placeholder = painterResource(Res.drawable.default_kapak),
                modifier = Modifier.matchParentSize(),
            )
            if (kart.id == TESCIL_KART_ID) {
                CografiIsaretMuhru(
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                )
            }
        }
    }
}

@Composable
private fun AnaSatirMetni(
    kart: MainCard,
    etkilesim: KartEtkilesimi,
    modifier: Modifier = Modifier,
) {
    val vurgulu = etkilesim.hoverlu || etkilesim.basili
    val okKaymasi by animateDpAsState(
        targetValue = if (vurgulu) 5.dp else 0.dp,
        animationSpec = tween(MIKRO_SURE),
        label = "kesfetOku",
    )

    Column(modifier = modifier) {
        Text(
            text = kart.altBaslik.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
            color = FenerAlevi,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = kart.ad,
            style = MaterialTheme.typography.titleLarge,
            color = TasBeyazi,
        )
        if (kart.aciklama.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = kart.aciklama,
                style = MaterialTheme.typography.bodyMedium,
                color = SisGrisi,
                modifier = Modifier.widthIn(max = 380.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keşfet →",
            style = MaterialTheme.typography.labelLarge,
            color = if (vurgulu) FenerAlevi else TasBeyazi,
            modifier = Modifier.offset(x = okKaymasi),
        )
    }
}

/**
 * Dar ekranda gösterilen tek sütunlu, düşey kart — zig-zag satırın yerini
 * alan basit yedek. Uygulamadaki fener halesini taşıyan iki yerden biri
 * (diğeri birincil CTA butonları). Tescilli Ürünler kartı ayrıca köşesinde
 * coğrafi işaret mührünü taşır.
 */
@Composable
private fun AnaKart(
    kart: MainCard,
    onKartTiklandi: (MainCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        kategori = "kart",
        kod = kart.kapak,
        baslik = kart.ad,
        etiket = kart.altBaslik,
        onClick = { onKartTiklandi(kart) },
        modifier = modifier,
        gorselOran = 16f / 9f,
        hale = true,
        muhur = kart.id == TESCIL_KART_ID,
    )
}
