package com.kuzeykapisi.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.FenerHalesi
import com.kuzeykapisi.app.ui.components.IkincilButon
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.NotrGeceCizgi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.fenerHalesiDestekli
import com.kuzeykapisi.app.ui.theme.klavyeOdakHalkasi
import kotlinx.coroutines.CancellationException

private val GENIS_EKRAN_ESIGI = 600.dp

/** Web/masaüstünde yönetim panelinin aşırı yayılmasını önleyen üst sınır. */
private val ICERIK_MAX_GENISLIK = 900.dp

/** İki bölüm (Personalar / Rota Noktaları) arasındaki boşluk. */
private val BOLUM_ARALIGI = 40.dp

/** Bölüm başlığındaki kayıt sayısı: yükleniyor, geldi ya da (hata/yetki yok) gizli. */
private sealed interface KayitSayisi {
    data object Yukleniyor : KayitSayisi
    data class Hazir(val adet: Int) : KayitSayisi
    data object Gizli : KayitSayisi
}

private enum class AdminIkonu { KisiEkle, Liste, KonumEkle, Harita }

@Composable
fun AdminAnaSayfaScreen(
    repo: KuzeyRepository,
    onGeri: () -> Unit,
    onCikisYap: () -> Unit,
    onPersonaEkleTiklandi: () -> Unit,
    onRotaYeriEkleTiklandi: () -> Unit,
    onPersonalariYonetTiklandi: () -> Unit,
    onRotaYerleriniYonetTiklandi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Özet sayıları ikincil bilgidir: birbirinden bağımsız çekilir ve hata
    // (401 dahil) rozeti sessizce gizler; panelin kendisini asla engellemez.
    val personaSayisi by produceState<KayitSayisi>(KayitSayisi.Yukleniyor, repo) {
        value = sayiGetir { repo.katalog().values.sumOf { it.ogeler.size } }
    }
    // Token'ı HttpClient ekler (bkz. AdminTokenEklentisi); bu ekran yalnızca
    // oturum açıkken çizilir (App.kt), o yüzden burada token kontrolü yok.
    val rotaSayisi by produceState<KayitSayisi>(KayitSayisi.Yukleniyor, repo) {
        value = sayiGetir { repo.rotaYerleriListele().size }
    }

    BoxWithConstraints(modifier = modifier) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ICERIK_MAX_GENISLIK)
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                EkranBasligi(
                    baslik = "Yönetim Paneli",
                    etiket = "Yönetim",
                    geriMetni = "Geri",
                    onGeri = onGeri,
                    modifier = Modifier.padding(bottom = 28.dp),
                )

                AdminBolumu(etiket = "PERSONALAR", sayi = personaSayisi, genisEkran = genisEkran) { kartModifier ->
                    AdminKart(
                        baslik = "Persona Ekle",
                        aciklama = "Yeni bir tarihi kişilik, mekan, lezzet, doğa ya da tescilli ürün botu ekleyin",
                        ikon = AdminIkonu.KisiEkle,
                        ikonRengi = FenerAlevi,
                        onClick = onPersonaEkleTiklandi,
                        modifier = kartModifier,
                    )
                    AdminKart(
                        baslik = "Personaları Yönet",
                        aciklama = "Mevcut personaları listeleyin, düzenleyin ya da silin",
                        ikon = AdminIkonu.Liste,
                        ikonRengi = SisGrisi,
                        onClick = onPersonalariYonetTiklandi,
                        modifier = kartModifier,
                    )
                }

                Spacer(modifier = Modifier.height(BOLUM_ARALIGI))

                AdminBolumu(etiket = "ROTA NOKTALARI", sayi = rotaSayisi, genisEkran = genisEkran) { kartModifier ->
                    AdminKart(
                        baslik = "Rota İçin Yeni Yer Ekle",
                        aciklama = "Akıllı Rota Planlayıcı'nın önerebileceği yeni bir mekan ekleyin",
                        ikon = AdminIkonu.KonumEkle,
                        ikonRengi = FenerAlevi,
                        onClick = onRotaYeriEkleTiklandi,
                        modifier = kartModifier,
                    )
                    AdminKart(
                        baslik = "Rota Yerlerini Yönet",
                        aciklama = "Mevcut rota mekanlarını listeleyin, düzenleyin ya da silin",
                        ikon = AdminIkonu.Harita,
                        ikonRengi = SisGrisi,
                        onClick = onRotaYerleriniYonetTiklandi,
                        modifier = kartModifier,
                    )
                }
                Spacer(modifier = Modifier.height(BOLUM_ARALIGI))
                OturumSatiri(genisEkran = genisEkran, onCikisYap = onCikisYap)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Panelin sonu: oturumun nasıl kapandığını söyleyen not ve "Çıkış yap".
 * Çıkış nötr bir eylemdir (veri silmez) — ikincil buton, yıkıcı DEĞİL.
 */
@Composable
private fun OturumSatiri(genisEkran: Boolean, onCikisYap: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NotrGeceCizgi))
        Spacer(modifier = Modifier.height(20.dp))
        val not: @Composable (Modifier) -> Unit = { m ->
            Text(
                text = Metinler.ADMIN_OTURUM_NOTU,
                style = MaterialTheme.typography.bodySmall,
                color = SisGrisi,
                modifier = m,
            )
        }
        val buton: @Composable () -> Unit = { IkincilButon(metin = "Çıkış yap", onClick = onCikisYap) }
        if (genisEkran) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                not(Modifier.weight(1f))
                buton()
            }
        } else {
            not(Modifier)
            Spacer(modifier = Modifier.height(12.dp))
            buton()
        }
    }
}

private suspend fun sayiGetir(islem: suspend () -> Int): KayitSayisi = try {
    KayitSayisi.Hazir(islem())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    KayitSayisi.Gizli
}

/**
 * Bir bölüm: harf aralıklı etiket (+ kayıt sayısı) ve altında iki kart. Geniş
 * ekranda kartlar yan yana ve eşit yükseklikte, dar ekranda alt alta.
 * [kartlar] her kart için uygun genişlik/yükseklik modifier'ını alır.
 */
@Composable
private fun AdminBolumu(
    etiket: String,
    sayi: KayitSayisi,
    genisEkran: Boolean,
    kartlar: @Composable (kartModifier: Modifier) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BolumBasligi(etiket = etiket, sayi = sayi)
        Spacer(modifier = Modifier.height(12.dp))
        if (genisEkran) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                kartlar(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                kartlar(Modifier.fillMaxWidth())
            }
        }
    }
}

/** Ana sayfa kart etiketleriyle aynı stil; yanında SisGrisi kayıt sayısı. */
@Composable
private fun BolumBasligi(etiket: String, sayi: KayitSayisi) {
    val stil = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp)
    Row(
        // EkranBasligi etiket ve başlığı 6dp içeriden başlatır; bölüm etiketi de aynı hizaya oturur.
        modifier = Modifier
            .padding(start = 6.dp)
            .semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = etiket, style = stil, color = FenerAlevi)
        val ek = when (sayi) {
            KayitSayisi.Yukleniyor -> " · …"
            is KayitSayisi.Hazir -> " · ${sayi.adet} kayıt"
            KayitSayisi.Gizli -> null
        }
        if (ek != null) Text(text = ek, style = stil, color = SisGrisi)
    }
}

/**
 * Admin ana ekranı kartı — ana sayfa kartlarıyla aynı [KartSekli] ve aynı
 * etkileşim dili: kenarlık hover/basılıyken FenerAlevi, yalnız fareli web'de
 * hover'da %2 büyüme ve arkasında fener halesi, basılıyken %97, klavye
 * odağında TasBeyazi halka. Solda eylemi anlatan ikon (ekleme FenerAlevi,
 * yönetme nötr SisGrisi), sağda hover'da FenerAlevi'ne dönüp 4dp sağa kayan
 * yön oku — kartın bir yere GÖTÜRDÜĞÜNÜ imler.
 */
@Composable
private fun AdminKart(
    baslik: String,
    aciklama: String,
    ikon: AdminIkonu,
    ikonRengi: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val vurgulu = etkilesim.hoverlu || etkilesim.basili
    val olcek by animateFloatAsState(
        targetValue = when {
            etkilesim.basili -> 0.97f
            etkilesim.hoverlu && fenerHalesiDestekli -> 1.02f
            else -> 1f
        },
        animationSpec = tween(MIKRO_SURE),
        label = "adminKartOlcegi",
    )

    Box(modifier = modifier.scale(olcek)) {
        FenerHalesi(gorunur = etkilesim.hoverlu, sekil = KartSekli)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .klavyeOdakHalkasi(odakli, KartSekli)
                .shadow(
                    elevation = if (vurgulu) 12.dp else 6.dp,
                    shape = KartSekli,
                    ambientColor = KaranlikLacivert,
                    spotColor = KaranlikLacivert,
                )
                .clip(KartSekli)
                .background(DerinDeniz)
                .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli)
                .hoverable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(24.dp),
            verticalAlignment = Alignment.Top,
        ) {
            IkonKutusu(ikon = ikon, renk = ikonRengi)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = baslik,
                    style = MaterialTheme.typography.titleLarge,
                    color = TasBeyazi,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = aciklama,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SisGrisi,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            YonRozeti(vurgulu = vurgulu)
        }
    }
}

/** Kartın solundaki ikon: rengin %12'lik zemini üzerinde 22dp lucide çizimi. Dekoratif. */
@Composable
private fun IkonKutusu(ikon: AdminIkonu, renk: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(renk.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val b = size.width / 24f
            val cizgi = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            fun cizgiCek(x1: Float, y1: Float, x2: Float, y2: Float) =
                drawLine(renk, Offset(x1 * b, y1 * b), Offset(x2 * b, y2 * b), cizgi.width, StrokeCap.Round)

            when (ikon) {
                // lucide user-plus: baş, omuzlar, sağda artı.
                AdminIkonu.KisiEkle -> {
                    drawCircle(renk, radius = 4f * b, center = Offset(9f * b, 7f * b), style = cizgi)
                    val govde = Path().apply {
                        moveTo(16f * b, 21f * b)
                        lineTo(16f * b, 19f * b)
                        quadraticTo(16f * b, 15f * b, 12f * b, 15f * b)
                        lineTo(6f * b, 15f * b)
                        quadraticTo(2f * b, 15f * b, 2f * b, 19f * b)
                        lineTo(2f * b, 21f * b)
                    }
                    drawPath(govde, renk, style = cizgi)
                    cizgiCek(19f, 8f, 19f, 14f)
                    cizgiCek(16f, 11f, 22f, 11f)
                }
                // lucide list: üç satır ve madde noktaları.
                AdminIkonu.Liste -> {
                    for (y in listOf(6f, 12f, 18f)) {
                        cizgiCek(8f, y, 21f, y)
                        drawCircle(renk, radius = 1.2f * b, center = Offset(3.5f * b, y * b))
                    }
                }
                // lucide map-pin-plus: iğne, içinde nokta, sağ altta artı.
                AdminIkonu.KonumEkle -> {
                    val igne = Path().apply {
                        moveTo(10f * b, 21f * b)
                        cubicTo(10f * b, 21f * b, 3f * b, 15f * b, 3f * b, 9f * b)
                        cubicTo(3f * b, 5.13f * b, 6.13f * b, 2f * b, 10f * b, 2f * b)
                        cubicTo(13.87f * b, 2f * b, 17f * b, 5.13f * b, 17f * b, 9f * b)
                        cubicTo(17f * b, 15f * b, 10f * b, 21f * b, 10f * b, 21f * b)
                        close()
                    }
                    drawPath(igne, renk, style = cizgi)
                    drawCircle(renk, radius = 2.5f * b, center = Offset(10f * b, 9f * b), style = cizgi)
                    cizgiCek(20f, 14f, 20f, 20f)
                    cizgiCek(17f, 17f, 23f, 17f)
                }
                // lucide map: üç panelli katlanmış harita.
                AdminIkonu.Harita -> {
                    val harita = Path().apply {
                        moveTo(3f * b, 6f * b)
                        lineTo(9f * b, 3f * b)
                        lineTo(15f * b, 6f * b)
                        lineTo(21f * b, 3f * b)
                        lineTo(21f * b, 18f * b)
                        lineTo(15f * b, 21f * b)
                        lineTo(9f * b, 18f * b)
                        lineTo(3f * b, 21f * b)
                        close()
                    }
                    drawPath(harita, renk, style = cizgi)
                    cizgiCek(9f, 3f, 9f, 18f)
                    cizgiCek(15f, 6f, 15f, 21f)
                }
            }
        }
    }
}

/** Kartın sağındaki yön oku: hover/basılıyken FenerAlevi'ne döner ve 4dp sağa kayar. */
@Composable
private fun YonRozeti(vurgulu: Boolean) {
    val kayma by animateDpAsState(
        targetValue = if (vurgulu) 4.dp else 0.dp,
        animationSpec = tween(MIKRO_SURE),
        label = "yonRozetiKaymasi",
    )
    Box(
        modifier = Modifier
            .offset(x = kayma)
            .size(32.dp)
            .clip(CircleShape)
            .background((if (vurgulu) FenerAlevi else SisGrisi).copy(alpha = if (vurgulu) 0.16f else 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val w = size.width
            val h = size.height
            val renk = if (vurgulu) FenerAlevi else SisGrisi
            val yol = Path().apply {
                moveTo(w * 0.22f, h * 0.22f)
                lineTo(w * 0.82f, h * 0.5f)
                lineTo(w * 0.22f, h * 0.78f)
            }
            drawPath(
                path = yol,
                color = renk,
                style = Stroke(width = w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
