package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.data.ses.KayitDurumu
import com.kuzeykapisi.app.data.ses.MikrofonIzniDurumu
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.Kehribar
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.NotrGeceAlcak
import com.kuzeykapisi.app.ui.theme.NotrGeceYuksek
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.SatirSekli
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.Yukseklik
import com.kuzeykapisi.app.ui.theme.hareketAzaltilsin
import com.kuzeykapisi.app.ui.theme.klavyeOdakHalkasi
import com.kuzeykapisi.app.ui.vm.ChatViewModel
import com.kuzeykapisi.app.ui.vm.Mesaj

@Composable
fun ChatSheet(
    repo: KuzeyRepository,
    kategori: String,
    oge: String,
    onKapat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Oturum ViewModel oluşurken açılır; panel kapanıp kapsamı temizlenince
    // ChatViewModel.onCleared oturumu kapatır ve ses kaynaklarını bırakır
    // (kapsam: App.kt'deki "sohbet-..." VmKapsami).
    val vm = viewModel { ChatViewModel(repo, kategori, oge) }
    val ui by vm.state.collectAsState()
    val kayitDurumu by vm.kayitDurumu.collectAsState()
    val kayitHatasi by vm.kayitHatasi.collectAsState()
    val mikrofonIzniDurumu by vm.mikrofonIzniDurumu.collectAsState()
    val oynatilanMesajId by vm.oynatilanMesajId.collectAsState()

    var girdi by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(ui.mesajlar.size) {
        if (ui.mesajlar.isNotEmpty()) listState.animateScrollToItem(ui.mesajlar.size - 1)
    }

    // Mikrofon izni (yalnızca Android'de gerçek bir şey yapar — bkz.
    // MikrofonIzniEfekti). istekNo her artışta izni kontrol eder/ister;
    // yalnızca BOSTA'dan kayda başlarken tetiklenir, kaydı durdururken değil.
    // Sonuç ne olursa olsun mikrofonaBasildi() çağrılır: SesKaydedici izni
    // KENDİSİ de kontrol eder ve reddedilmişse hata'yı doldurur — burada
    // ikinci bir dal açmaya gerek yok (bkz. SesKaydedici.android.kt).
    var mikrofonIstekNo by remember { mutableStateOf(0) }
    MikrofonIzniEfekti(istekNo = mikrofonIstekNo) { vm.mikrofonaBasildi() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KaranlikLacivert),
    ) {
        // Başlık çubuğu: dar ekranda panel tam ekran olduğu için durum çubuğu
        // inset'i burada da uygulanır (edge-to-edge). Bu satır klavyeden
        // etkilenmez — sabit üst bölge olarak kalır.
        Column(modifier = Modifier.fillMaxWidth().background(NotrGeceYuksek)) {
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
                                FenerAlevi.copy(alpha = Opaklik.YUZDE30),
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
                    itemsIndexed(ui.mesajlar) { index, mesaj ->
                        MesajBalonu(
                            mesaj = mesaj,
                            sesDurumu = when {
                                mesaj.benden || mesaj.sistemNotu -> null
                                oynatilanMesajId == index -> SesButonuDurumu.CALIYOR
                                oynatilanMesajId != null -> SesButonuDurumu.PASIF
                                else -> SesButonuDurumu.OYNAT
                            },
                            onSesTikla = { vm.mesajSesiCal(index) },
                        )
                    }
                    if (ui.hata != null) {
                        item { HataMetni(ui.hata ?: "") }
                    }
                    if (ui.yaziyor) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                TypingIndicator(
                                    modifier = Modifier
                                        .clip(SatirSekli)
                                        .background(NotrGeceYuksek)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NotrGeceAlcak)
                .imePadding()
                .navigationBarsPadding()
                .padding(12.dp),
        ) {
            // Gönderim uyarısı: mesaj balonlarının ARASINDA değil, giriş
            // kutusunun hemen üstünde sabit bir bant; sohbet geçmişine girmez.
            AgUyarisiBandi(mesaj = ui.agUyarisi)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KuzeyMetinAlani(
                    deger = girdi,
                    onDegisti = { girdi = it },
                    ipucu = "Mesajınızı yazın…",
                    etkin = kayitDurumu != KayitDurumu.ISLENIYOR,
                    modifier = Modifier.weight(1f),
                )
                MikrofonButonu(
                    durum = kayitDurumu,
                    onClick = {
                        when (kayitDurumu) {
                            // Önceden hatırlanan izin durumu (KALICI_REDDEDILDI
                            // dahil) ASLA bu çağrıyı atlamak için kullanılmaz —
                            // izin gerçekten verilene kadar HER basışta gerçek
                            // platform izin isteme API'si yeniden tetiklenir
                            // (bkz. MikrofonIzniEfekti / SesKaydedici.kayidaBasla).
                            KayitDurumu.BOSTA -> mikrofonIstekNo++
                            KayitDurumu.KAYIT_YAPILIYOR -> vm.mikrofonaBasildi()
                            KayitDurumu.ISLENIYOR -> Unit
                        }
                    },
                )
                GonderButonu(
                    etkin = girdi.isNotBlank() && kayitDurumu == KayitDurumu.BOSTA,
                    onClick = {
                        if (girdi.isNotBlank()) {
                            vm.gonder(girdi)
                            girdi = ""
                        }
                    },
                )
            }
            // Kalıcı olmayan bir toast/snackbar DEĞİL — kaybolmayan, sabit bir
            // uyarı satırı. Kalıcı reddedilmişse (ve platform destekliyorsa)
            // altına doğrudan sistem ayarlarını açan bir buton eklenir; web'de
            // buton yerine yalnızca talimat metni gösterilir (bkz. SesKaydedici.wasmJs.kt).
            if (kayitHatasi != null) {
                Text(
                    text = kayitHatasi ?: "",
                    color = Kehribar,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
                if (mikrofonIzniDurumu == MikrofonIzniDurumu.KALICI_REDDEDILDI && vm.ayarlarDestekleniyor) {
                    IkincilButon(
                        metin = "Ayarları aç",
                        onClick = { vm.ayarlariAc() },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

/**
 * Geçici gönderim uyarısı bandı. [mesaj] null olunca kısa bir fade ile
 * kaybolur; kaybolurken son metni göstermeye devam eder (boş bant görünmez).
 * Ekran okuyucuya "polite" canlı bölge olarak duyurulur. Renk: uyarı tonu
 * [Kehribar] — SinopKirmizisi yıkıcı eylemlere kilitli.
 */
@Composable
private fun AgUyarisiBandi(mesaj: String?) {
    var sonMesaj by remember { mutableStateOf(mesaj.orEmpty()) }
    if (mesaj != null && mesaj != sonMesaj) sonMesaj = mesaj
    val uyariRengi = Kehribar
    AnimatedVisibility(
        visible = mesaj != null,
        enter = fadeIn(animationSpec = tween(MIKRO_SURE)),
        exit = fadeOut(animationSpec = tween(MIKRO_SURE)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(SatirSekli)
                .background(uyariRengi.copy(alpha = Opaklik.YUZDE12))
                .border(1.dp, uyariRengi.copy(alpha = Opaklik.YUZDE45), SatirSekli)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Uyarı işareti (ünlem dairesi) — renk tek başına anlam taşımasın diye.
            Canvas(modifier = Modifier.size(16.dp)) {
                val w = size.width
                val kalinlik = w * 0.11f
                drawCircle(color = uyariRengi, radius = w / 2f - kalinlik / 2f, style = Stroke(width = kalinlik))
                drawLine(
                    color = uyariRengi,
                    start = Offset(w / 2f, w * 0.27f),
                    end = Offset(w / 2f, w * 0.58f),
                    strokeWidth = kalinlik,
                    cap = StrokeCap.Round,
                )
                drawCircle(color = uyariRengi, radius = kalinlik * 0.7f, center = Offset(w / 2f, w * 0.74f))
            }
            Text(
                text = sonMesaj,
                style = MaterialTheme.typography.bodySmall,
                color = uyariRengi,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KapatButonu(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "kapatRengi",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(38.dp)
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .etkilesimli(interactionSource, contentDescription = "Kapat", onClick = onClick),
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
    val odakli by interactionSource.collectIsFocusedAsState()
    val zemin by animateColorAsState(
        targetValue = when {
            !etkin -> FenerAlevi.copy(alpha = Opaklik.YUZDE22)
            etkilesim.hoverlu || etkilesim.basili -> FenerAlevi
            else -> FenerAlevi.copy(alpha = Opaklik.YUZDE92)
        },
        animationSpec = tween(MIKRO_SURE),
        label = "gonderZemin",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(48.dp)
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .background(zemin)
            .etkilesimli(interactionSource, etkin, contentDescription = "Gönder", onClick = onClick),
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
private fun MesajBalonu(
    mesaj: Mesaj,
    sesDurumu: SesButonuDurumu?,
    onSesTikla: () -> Unit,
) {
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
                    .border(1.dp, SisGrisi.copy(alpha = Opaklik.YUZDE25), SatirSekli)
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
                    .background(FenerAlevi.copy(alpha = Opaklik.YUZDE14))
                    .border(1.dp, FenerAlevi.copy(alpha = Opaklik.YUZDE55), balonSekli)
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
                    .shadow(Yukseklik.DP3, balonSekli, ambientColor = KaranlikLacivert, spotColor = KaranlikLacivert)
                    .clip(balonSekli)
                    .background(NotrGeceYuksek)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = mesaj.metin,
                    color = TasBeyazi,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (sesDurumu != null) {
            SesButonu(
                durum = sesDurumu,
                onClick = onSesTikla,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Bot mesajının altındaki ses butonunun görsel durumu. */
enum class SesButonuDurumu { OYNAT, CALIYOR, PASIF }

/**
 * İKON-TABANLI (metinsiz) oynat/durdur butonu — her bot mesajının altında.
 * CALIYOR: DUR ikonu, tıklanabilir. OYNAT: play ikonu, tıklanabilir.
 * PASİF: başka bir mesaj çalarken bu buton soluk ve tıklanamaz — kullanıcı
 * aynı anda iki mesajı BAŞLATAMASIN diye bir savunma kontrolüdür.
 */
@Composable
private fun SesButonu(durum: SesButonuDurumu, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val etkin = durum != SesButonuDurumu.PASIF
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = when {
            !etkin -> SisGrisi.copy(alpha = Opaklik.YUZDE35)
            etkilesim.hoverlu || etkilesim.basili -> FenerAlevi
            else -> SisGrisi
        },
        animationSpec = tween(MIKRO_SURE),
        label = "sesButonuRengi",
    )
    Box(
        modifier = modifier
            .scale(etkilesim.olcek)
            .size(30.dp)
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .etkilesimli(
                interactionSource,
                etkin,
                contentDescription = if (durum == SesButonuDurumu.CALIYOR) "Durdur" else "Dinle",
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            if (durum == SesButonuDurumu.CALIYOR) {
                // İki dikey çubuk — dur ikonu.
                val cubukGenisligi = size.width * 0.3f
                drawRect(color = renk, topLeft = Offset(0f, 0f), size = Size(cubukGenisligi, size.height))
                drawRect(
                    color = renk,
                    topLeft = Offset(size.width - cubukGenisligi, 0f),
                    size = Size(cubukGenisligi, size.height),
                )
            } else {
                // Sağa bakan üçgen — oynat ikonu.
                val w = size.width
                val h = size.height
                val yol = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, h / 2f)
                    lineTo(0f, h)
                    close()
                }
                drawPath(yol, color = renk)
            }
        }
    }
}

/**
 * Mikrofon butonu — BOSTA: düz mikrofon ikonu. KAYIT_YAPILIYOR: vurgulu
 * (FenerAlevi) ikon + hafif nabız (NabizGostergesi'yle aynı dil, burada
 * halka yerine ölçek nabzı). ISLENIYOR: küçük dönen gösterge, tıklanamaz.
 */
@Composable
private fun MikrofonButonu(durum: KayitDurumu, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val kayitta = durum == KayitDurumu.KAYIT_YAPILIYOR
    val islemde = durum == KayitDurumu.ISLENIYOR

    val renk by animateColorAsState(
        targetValue = when {
            kayitta -> FenerAlevi
            etkilesim.hoverlu || etkilesim.basili -> FenerAlevi
            else -> SisGrisi
        },
        animationSpec = tween(MIKRO_SURE),
        label = "mikrofonRengi",
    )

    // "Hareketi azalt" tercihinde ne kayıt nabzı ne de işleniyor spinner'ı
    // kurulur — ikisi de durağan değerinde kalır (bkz. ui/theme/Hareket.kt).
    val nabizOlcek: Float
    val donusDerecesi: Float
    if (hareketAzaltilsin) {
        nabizOlcek = 1f
        donusDerecesi = 0f
    } else {
        val nabizGecisi = rememberInfiniteTransition(label = "mikrofonNabzi")
        nabizOlcek = nabizGecisi.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(animation = tween(700, easing = LinearEasing)),
            label = "mikrofonNabzOlcegi",
        ).value
        donusDerecesi = nabizGecisi.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(900, easing = LinearEasing)),
            label = "mikrofonSpinner",
        ).value
    }

    Box(
        modifier = Modifier
            .scale(if (kayitta) nabizOlcek else etkilesim.olcek)
            .size(44.dp)
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .background(if (kayitta) FenerAlevi.copy(alpha = Opaklik.YUZDE16) else Color.Transparent)
            .etkilesimli(
                interactionSource,
                !islemde,
                contentDescription = when {
                    islemde -> "Ses işleniyor"
                    kayitta -> "Kaydı durdur"
                    else -> "Sesli mesaj kaydet"
                },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (islemde) {
            Canvas(modifier = Modifier.size(18.dp)) {
                rotate(donusDerecesi) {
                    drawArc(
                        color = SisGrisi,
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = size.width * 0.16f, cap = StrokeCap.Round),
                    )
                }
            }
        } else {
            Canvas(modifier = Modifier.size(18.dp)) {
                val w = size.width
                val h = size.height
                val govdeGenislik = w * 0.34f
                drawRoundRect(
                    color = renk,
                    topLeft = Offset((w - govdeGenislik) / 2f, 0f),
                    size = Size(govdeGenislik, h * 0.5f),
                    cornerRadius = CornerRadius(govdeGenislik / 2f, govdeGenislik / 2f),
                )
                val standKalinlik = w * 0.09f
                drawArc(
                    color = renk,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = standKalinlik, cap = StrokeCap.Round),
                    topLeft = Offset(w * 0.12f, h * 0.28f),
                    size = Size(w * 0.76f, h * 0.5f),
                )
                drawLine(
                    color = renk,
                    start = Offset(w / 2f, h * 0.78f),
                    end = Offset(w / 2f, h * 0.96f),
                    strokeWidth = standKalinlik,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = renk,
                    start = Offset(w * 0.30f, h * 0.96f),
                    end = Offset(w * 0.70f, h * 0.96f),
                    strokeWidth = standKalinlik,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
