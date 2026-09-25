package com.kuzeykapisi.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.domain.MAIN_CARDS
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.ui.components.CografiIsaretMuhru
import com.kuzeykapisi.app.ui.components.FenerHalesi
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.fenerHalesiDestekli
import com.kuzeykapisi.app.ui.theme.hareketAzaltilsin
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.default_kapak
import kuzeykapisiapp.shared.generated.resources.sinop_arkaplan
import org.jetbrains.compose.resources.painterResource

// --- Kırılım noktaları --------------------------------------------------------

/** Bunun altı telefon: kartlar dikey (görsel üstte, metin altta), gutter 24dp. */
private val TELEFON_ESIGI = 600.dp

/** Hero başlığının iki satıra bölünüp `displaySmall`'a çıktığı eşik. */
private val ORTA_BASLIK_ESIGI = 700.dp

/** Hero başlığının `displayLarge`'a çıktığı eşik. */
private val BUYUK_BASLIK_ESIGI = 1000.dp

/** Kartların 2x2 ızgaraya geçtiği, gutter'ın 48dp'ye çıktığı eşik. */
private val IKILI_IZGARA_ESIGI = 1200.dp

/**
 * Dört kartın tek sırada yan yana dizildiği eşik. Altında her kartın içindeki
 * görsel/metin yarıları ~160dp'nin altına düşer ve başlıklar sıkışır.
 */
private val DORTLU_IZGARA_ESIGI = 1600.dp

// --- Hero -------------------------------------------------------------------

/** Hero'nun kapladığı asgari viewport yüksekliği oranı (brief: ~%85-90). */
private const val HERO_YUKSEKLIK_ORANI = 0.88f

/**
 * Hero metin bloğunun üstündeki boşluk. Yerel perde bu boşlukta sıfırdan
 * [PERDE_UST_OPAKLIGI]'na yumuşakça koyulaşır; metnin ilk satırı perdenin
 * tam güçte olduğu yerden başlar.
 */
private val HERO_METIN_UST_BOSLUGU = 96.dp

/**
 * Yerel perdenin (KaranlikLacivert) metnin ilk satırındaki ve en altındaki
 * opaklığı. Fotoğrafın metin bandındaki en kötü pikseline göre ölçülerek
 * seçildi: 0.55'te FenerAlevi etiket 1.84:1'e, SisGrisi gövde 2.64:1'e
 * düşüyordu; 0.86'da 360-2560dp arası altı viewport'ta etiket ve gövde
 * >= 4.5:1, başlık >= 3:1 (büyük metin).
 */
private const val PERDE_UST_OPAKLIGI = 0.86f
private const val PERDE_ALT_OPAKLIGI = 0.94f

/** Hero metin sütununun (başlık) azami genişliği. Perde yatayda bunun ötesinde saydamlaşır. */
private val HERO_METIN_MAX_GENISLIK = 960.dp

/** Perdenin metin sütununun sağ kenarından sonra tamamen saydamlaştığı mesafe. */
private val PERDE_YATAY_ERIME = 320.dp

/**
 * Hero'nun en altındaki, tam genişlikte ince erime bandı: metnin olmadığı
 * geniş-ekran sağ yarısında da hero sayfa zeminine sert kenarsız bağlanır.
 */
private val HERO_ALT_ERIME = 96.dp

/** Fener ışığı hâlesinin bir tam turu (ms). Kasıtlı olarak zor fark edilecek kadar yavaş. */
private const val HALE_DONGU_SURESI = 20_000

/** Fener ışığı hâlesinin merkezdeki opaklığı. */
private const val HALE_OPAKLIGI = 0.04f

// --- Kartlar ----------------------------------------------------------------

/** Tescilli Ürünler kartı — coğrafi işaret mührünü taşıyan tek ana kart. */
private const val TESCIL_KART_ID = "tescil"

/** Kart metin panelinin bu genişliğin altında açıklama cümlesi gizlenir (sıkışmasın diye). */
private val ACIKLAMA_MIN_GENISLIK = 240.dp

private val HapSekli = RoundedCornerShape(percent = 50)

private enum class KartYerlesimi { Yatay, Dikey }

private enum class ChevronYonu { Asagi, Sag }

/** Hero metni ile kart bölümünün ORTAK yan boşluğu — iki bölüm aynı sol hizaya oturur. */
private fun kenarBoslugu(genislik: Dp): Dp = when {
    genislik < TELEFON_ESIGI -> 24.dp
    genislik < IKILI_IZGARA_ESIGI -> 32.dp
    else -> 48.dp
}

/**
 * ANA SAYFA — tek bir kaydırılabilir sütun: en üstte tam-ekran Sinop fotoğraflı
 * hero, hemen altında ana kartlar. Hero'daki "Keşfet" butonu aynı scroll
 * state'i üzerinden kartlar bölümünün ÖLÇÜLEN başlangıcına kayar.
 */
@Composable
fun HomeScreen(onKartTiklandi: (MainCard) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val genislik = maxWidth
        val gutter = kenarBoslugu(genislik)
        val heroMinYukseklik = maxHeight * HERO_YUKSEKLIK_ORANI
        val kaydirma = rememberScrollState()
        val kapsam = rememberCoroutineScope()
        var kartlarY by remember { mutableIntStateOf(0) }
        val ilkKartOdagi = remember { FocusRequester() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(kaydirma),
        ) {
            HeroBolumu(
                genislik = genislik,
                gutter = gutter,
                onKesfetTiklandi = { klavyeyle ->
                    kapsam.launch {
                        if (hareketAzaltilsin) {
                            kaydirma.scrollTo(kartlarY)
                        } else {
                            kaydirma.animateScrollTo(kartlarY)
                        }
                        // Klavyeyle basıldıysa odak da kartlara taşınır; fareyle/dokunarak
                        // basıldığında ilk kartta beklenmedik bir odak halkası belirmesin.
                        if (klavyeyle) ilkKartOdagi.requestFocus()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = heroMinYukseklik),
            )

            AnaKartlarBolumu(
                genislik = genislik,
                gutter = gutter,
                ilkKartOdagi = ilkKartOdagi,
                onKartTiklandi = onKartTiklandi,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { kartlarY = it.positionInParent().y.roundToInt() }
                    .padding(start = gutter, end = gutter, top = 40.dp, bottom = 64.dp),
            )
        }
    }
}

/**
 * HERO — kenardan kenara, viewport'un ~%88'i kadar Sinop fotoğrafı. Metin
 * doğrudan fotoğrafın üzerinde durur; arkasında kart/kutu/düz zemin YOKTUR.
 *
 * Katmanlar (alttan üste): fotoğraf → fener ışığı hâlesi → yerel perde → metin.
 * Perde hero'nun sabit bir oranına değil METİN BLOĞUNUN KENDİSİNE bağlıdır
 * ([yerelPerde]): hangi ekran boyu ya da yazı büyüklüğü olursa olsun yalnızca
 * metnin kapladığı bantta koyulaşır, en altta sayfa zeminine erir; fotoğrafın
 * geri kalanı olduğu gibi, net kalır. Tüm fotoğrafa buğu/blur UYGULANMAZ.
 *
 * Yükseklik `heightIn(min)` ile verilir: büyük yazı boyutu tercihinde metin
 * kırpılmaz, hero uzar.
 */
@Composable
private fun HeroBolumu(
    genislik: Dp,
    gutter: Dp,
    onKesfetTiklandi: (klavyeyle: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(Res.drawable.sinop_arkaplan),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        FenerIsigiHalesi(modifier = Modifier.matchParentSize())
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(HERO_ALT_ERIME)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to KaranlikLacivert.copy(alpha = PERDE_ALT_OPAKLIGI),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .yerelPerde(gutter)
                .padding(
                    start = gutter,
                    end = gutter,
                    top = HERO_METIN_UST_BOSLUGU,
                    bottom = if (genislik < TELEFON_ESIGI) 40.dp else 72.dp,
                ),
        ) {
            Text(
                text = "SİNOP · KUZEY KAPISI",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.4.sp),
                color = FenerAlevi,
            )
            Spacer(modifier = Modifier.height(20.dp))
            HeroBasligi(genislik = genislik)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Bir başlık seçin; tarihî bir şahsiyet, bir usta aşçı ya da bir doğa " +
                    "rehberi Sinop'u size kendi diliyle anlatsın.",
                style = MaterialTheme.typography.bodyLarge,
                color = SisGrisi,
                modifier = Modifier.widthIn(max = 560.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            KesfetButonu(onClick = onKesfetTiklandi)
        }
    }
}

/**
 * Metin bloğunun arkasına, yalnızca o bloğun sınırları içinde çizilen perde.
 * Dikeyde: üst boşlukta sıfırdan [PERDE_UST_OPAKLIGI]'na, oradan en alta doğru
 * [PERDE_ALT_OPAKLIGI]'na koyulaşır. Yatayda: metin sütununun sağ kenarına
 * kadar tam güçte, sonra [PERDE_YATAY_ERIME] içinde saydamlaşır (DstIn maskesi)
 * — geniş ekranda fotoğrafın metin olmayan sağ yarısı karartılmaz. Telefonda
 * metin tüm genişliği kapladığından perde de öyle. Yalnızca çizimdir.
 */
private fun Modifier.yerelPerde(gutter: Dp): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawBehind {
            val ustBosluk = (HERO_METIN_UST_BOSLUGU.toPx() / size.height).coerceIn(0f, 1f)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    ustBosluk to KaranlikLacivert.copy(alpha = PERDE_UST_OPAKLIGI),
                    1f to KaranlikLacivert.copy(alpha = PERDE_ALT_OPAKLIGI),
                ),
            )
            val metinSagi = gutter.toPx() + HERO_METIN_MAX_GENISLIK.toPx()
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = metinSagi,
                    endX = metinSagi + PERDE_YATAY_ERIME.toPx(),
                ),
                blendMode = BlendMode.DstIn,
            )
        }

/**
 * İki yarım cümle, iki kesim: birincisi Fraunces SemiBold, ikincisi Fraunces
 * Italic. Vurgu renkle değil tipografiyle kurulur — FenerAlevi hero'da yalnızca
 * etiket ve birincil butona ayrılmıştır.
 */
@Composable
private fun HeroBasligi(genislik: Dp) {
    val ikiSatir = genislik >= ORTA_BASLIK_ESIGI
    val baslik = remember(ikiSatir) {
        buildAnnotatedString {
            append("Karadeniz'in kuzey kapısında,")
            append(if (ikiSatir) "\n" else " ")
            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal)) {
                append("her başlığın bir anlatıcısı var.")
            }
        }
    }
    val stil = when {
        genislik >= BUYUK_BASLIK_ESIGI -> MaterialTheme.typography.displayLarge
        ikiSatir -> MaterialTheme.typography.displaySmall
        else -> MaterialTheme.typography.headlineLarge
    }
    Text(
        text = baslik,
        style = stil,
        color = TasBeyazi,
        modifier = Modifier
            .widthIn(max = HERO_METIN_MAX_GENISLIK)
            .semantics { heading() },
    )
}

/**
 * Hero'nun arkasında çok hafif, yavaşça gezinen FenerAlevi hâlesi. Merkez,
 * 8 şeklinde (Lissajous) bir yolu [HALE_DONGU_SURESI]'nde bir kez dolaşır —
 * başlangıç ve bitiş aynı noktada olduğundan döngü dikişsizdir. Değer yalnızca
 * çizim aşamasında okunur; animasyon yeniden-kompozisyon tetiklemez.
 *
 * "Hareketi azalt" tercihinde animasyon hiç kurulmaz, hâle sabit durur.
 */
@Composable
private fun FenerIsigiHalesi(modifier: Modifier = Modifier) {
    val evre: State<Float> = if (hareketAzaltilsin) {
        remember { mutableFloatStateOf(0f) }
    } else {
        rememberInfiniteTransition(label = "fenerIsigi").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = HALE_DONGU_SURESI, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "fenerIsigiEvresi",
        )
    }

    Box(
        modifier = modifier.drawBehind {
            val aci = evre.value * 2f * PI.toFloat()
            val merkez = Offset(
                x = size.width * (0.55f + 0.25f * sin(aci)),
                y = size.height * (0.35f + 0.12f * sin(2f * aci)),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(FenerAlevi.copy(alpha = HALE_OPAKLIGI), Color.Transparent),
                    center = merkez,
                    radius = max(size.width, size.height) * 0.6f,
                ),
            )
        },
    )
}

/**
 * "Keşfet" — hero'nun tek birincil eylemi: FenerAlevi dolgulu hap buton +
 * aşağı chevron. Tıklanınca sayfa kartlara yumuşakça kayar.
 *
 * Durumlar: hover (yalnız web) → fener halesi + hafif büyüme + chevron aşağı
 * kayar; basılı → %97; klavye odağı → buton dışında TasBeyazi halka.
 */
@Composable
private fun KesfetButonu(onClick: (klavyeyle: Boolean) -> Unit, modifier: Modifier = Modifier) {
    val kaynak = remember { MutableInteractionSource() }
    val hoverlu by kaynak.collectIsHoveredAsState()
    val basili by kaynak.collectIsPressedAsState()
    val odakli by kaynak.collectIsFocusedAsState()
    val olcek by animateFloatAsState(
        targetValue = etkilesimOlcegi(hoverlu, basili),
        animationSpec = tween(MIKRO_SURE),
        label = "kesfetOlcegi",
    )
    val okKaymasi by animateDpAsState(
        targetValue = if (hoverlu || basili) 3.dp else 0.dp,
        animationSpec = tween(MIKRO_SURE),
        label = "kesfetOku",
    )

    Box(modifier = modifier.scale(olcek)) {
        FenerHalesi(gorunur = hoverlu, sekil = HapSekli)
        Row(
            modifier = Modifier
                .odakHalkasi(odakli, HapSekli)
                .clip(HapSekli)
                .background(FenerAlevi)
                .hoverable(interactionSource = kaynak)
                .clickable(
                    interactionSource = kaynak,
                    indication = null,
                    role = Role.Button,
                    onClick = { onClick(odakli) },
                )
                .heightIn(min = 44.dp)
                .padding(start = 22.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Keşfet", style = MaterialTheme.typography.labelLarge, color = KaranlikLacivert)
            ChevronIkonu(
                yon = ChevronYonu.Asagi,
                renk = KaranlikLacivert,
                modifier = Modifier.size(16.dp).offset(y = okKaymasi),
            )
        }
    }
}

/**
 * Ana kartlar. Izgara pencere genişliğine göre kurulur — bkz. eşik sabitleri:
 * geniş web'de 4 kart tek sırada, ara genişlikte 2x2, altında tek sütun.
 * Aynı sıradaki kartlar `IntrinsicSize.Min` ile eşit yüksekliktedir.
 *
 * Geniş/orta ekranda her kartın içi zig-zag akar: 1. ve 3. kartta metin solda
 * görsel sağda, 2. ve 4. kartta görsel solda metin sağda. Telefonda kart dikeydir.
 */
@Composable
private fun AnaKartlarBolumu(
    genislik: Dp,
    gutter: Dp,
    ilkKartOdagi: FocusRequester,
    onKartTiklandi: (MainCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sutun = when {
        genislik >= DORTLU_IZGARA_ESIGI -> 4
        genislik >= IKILI_IZGARA_ESIGI -> 2
        else -> 1
    }
    val yerlesim = if (genislik < TELEFON_ESIGI) KartYerlesimi.Dikey else KartYerlesimi.Yatay
    val aralik = if (sutun == 1) 20.dp else 24.dp
    val kartGenisligi = (genislik - gutter * 2 - aralik * (sutun - 1)) / sutun
    val metinPaneli = if (yerlesim == KartYerlesimi.Yatay) kartGenisligi / 2 else kartGenisligi
    // Görsel yarısının asgari yüksekliği: 4'lüde dik, 2'lide kareye yakın, tek sütunda yatay oran.
    val gorselMinYukseklik = when (sutun) {
        4 -> kartGenisligi / 2 * 1.25f
        2 -> kartGenisligi / 2 * 0.85f
        else -> kartGenisligi / 2 * 0.62f
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(aralik)) {
        MAIN_CARDS.withIndex().chunked(sutun).forEach { sira ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(aralik),
            ) {
                sira.forEach { (index, kart) ->
                    AnaKart(
                        kart = kart,
                        yerlesim = yerlesim,
                        gorselSolda = index % 2 == 1,
                        gorselMinYukseklik = gorselMinYukseklik,
                        aciklamaGoster = metinPaneli >= ACIKLAMA_MIN_GENISLIK,
                        onClick = { onKartTiklandi(kart) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(if (index == 0) Modifier.focusRequester(ilkKartOdagi) else Modifier),
                    )
                }
            }
        }
    }
}

/**
 * Tek ana kart: imza [KartSekli] (sağ-alt köşe kesik) içinde görsel + metin
 * paneli. Görsel, metne bakan kenarında DerinDeniz'e erir; ikisi tek bir taş
 * blok gibi okunur. Metin fotoğrafın üzerine YAZILMAZ.
 *
 * Etkileşim: kenarlık [kartEtkilesimi] ile FenerAlevi'ne döner (~200ms); web'de
 * hover'da %2 büyür ve arkasında [FenerHalesi] belirir; basılıyken %97'ye iner;
 * klavye odağında dışında TasBeyazi halka çizilir.
 */
@Composable
private fun AnaKart(
    kart: MainCard,
    yerlesim: KartYerlesimi,
    gorselSolda: Boolean,
    gorselMinYukseklik: Dp,
    aciklamaGoster: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kaynak = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(kaynak)
    val odakli by kaynak.collectIsFocusedAsState()
    val vurgulu = etkilesim.hoverlu || etkilesim.basili
    val olcek by animateFloatAsState(
        targetValue = etkilesimOlcegi(etkilesim.hoverlu, etkilesim.basili),
        animationSpec = tween(MIKRO_SURE),
        label = "anaKartOlcegi",
    )
    val golge by animateDpAsState(
        targetValue = if (vurgulu) 12.dp else 4.dp,
        animationSpec = tween(MIKRO_SURE),
        label = "anaKartGolgesi",
    )

    val kabuk = Modifier
        .fillMaxSize()
        .odakHalkasi(odakli, KartSekli)
        .shadow(elevation = golge, shape = KartSekli, ambientColor = KaranlikLacivert, spotColor = KaranlikLacivert)
        .clip(KartSekli)
        .background(DerinDeniz)
        .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli)
        .hoverable(interactionSource = kaynak)
        .clickable(interactionSource = kaynak, indication = null, role = Role.Button, onClick = onClick)

    Box(modifier = modifier.scale(olcek)) {
        FenerHalesi(gorunur = etkilesim.hoverlu, sekil = KartSekli)

        when (yerlesim) {
            KartYerlesimi.Dikey -> Column(modifier = kabuk) {
                KartGorseli(
                    kart = kart,
                    erime = Brush.verticalGradient(0.55f to Color.Transparent, 1f to DerinDeniz),
                    muhurHizasi = Alignment.TopEnd,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                )
                KartMetni(
                    kart = kart,
                    aciklamaGoster = aciklamaGoster,
                    vurgulu = vurgulu,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                )
            }

            KartYerlesimi.Yatay -> Row(modifier = kabuk) {
                val gorsel: @Composable () -> Unit = {
                    KartGorseli(
                        kart = kart,
                        erime = if (gorselSolda) {
                            Brush.horizontalGradient(0.55f to Color.Transparent, 1f to DerinDeniz)
                        } else {
                            Brush.horizontalGradient(0f to DerinDeniz, 0.45f to Color.Transparent)
                        },
                        muhurHizasi = if (gorselSolda) Alignment.TopStart else Alignment.TopEnd,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = gorselMinYukseklik),
                    )
                }
                val metin: @Composable () -> Unit = {
                    KartMetni(
                        kart = kart,
                        aciklamaGoster = aciklamaGoster,
                        vurgulu = vurgulu,
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp),
                    )
                }
                if (gorselSolda) {
                    gorsel()
                    metin()
                } else {
                    metin()
                    gorsel()
                }
            }
        }
    }
}

@Composable
private fun KartGorseli(
    kart: MainCard,
    erime: Brush,
    muhurHizasi: Alignment,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = Config.gorselUrl("kart", kart.kapak),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(Res.drawable.default_kapak),
            placeholder = painterResource(Res.drawable.default_kapak),
            modifier = Modifier.matchParentSize(),
        )
        Box(modifier = Modifier.matchParentSize().background(erime))
        if (kart.id == TESCIL_KART_ID) {
            CografiIsaretMuhru(modifier = Modifier.align(muhurHizasi).padding(14.dp))
        }
    }
}

@Composable
private fun KartMetni(
    kart: MainCard,
    aciklamaGoster: Boolean,
    vurgulu: Boolean,
    modifier: Modifier = Modifier,
) {
    val okKaymasi by animateDpAsState(
        targetValue = if (vurgulu) 4.dp else 0.dp,
        animationSpec = tween(MIKRO_SURE),
        label = "kartOku",
    )

    Column(modifier = modifier) {
        Text(
            text = kart.altBaslik.turkceBuyukHarf(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
            color = FenerAlevi,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = kart.ad,
            style = MaterialTheme.typography.titleLarge,
            color = TasBeyazi,
        )
        if (aciklamaGoster && kart.aciklama.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = kart.aciklama,
                style = MaterialTheme.typography.bodyMedium,
                color = SisGrisi,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))
        ChevronIkonu(
            yon = ChevronYonu.Sag,
            renk = if (vurgulu) FenerAlevi else TasBeyazi,
            modifier = Modifier.size(20.dp).offset(x = okKaymasi),
        )
    }
}

/**
 * lucide `chevron-down` / `chevron-right` geometrisi (24'lük ızgara), tek renk
 * çizgi. Emoji ya da metin oku yerine gerçek ikon.
 */
@Composable
private fun ChevronIkonu(yon: ChevronYonu, renk: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val b = size.width / 24f
        val yol = Path().apply {
            when (yon) {
                ChevronYonu.Asagi -> {
                    moveTo(6f * b, 9f * b)
                    lineTo(12f * b, 15f * b)
                    lineTo(18f * b, 9f * b)
                }
                ChevronYonu.Sag -> {
                    moveTo(9f * b, 6f * b)
                    lineTo(15f * b, 12f * b)
                    lineTo(9f * b, 18f * b)
                }
            }
        }
        drawPath(
            path = yol,
            color = renk,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

/** Hover büyümesi yalnızca fareli web'de; dokunmatikte yalnızca basma küçülmesi. */
private fun etkilesimOlcegi(hoverlu: Boolean, basili: Boolean): Float = when {
    basili -> 0.97f
    hoverlu && fenerHalesiDestekli -> 1.02f
    else -> 1f
}

/**
 * Klavye odağı göstergesi: şeklin 3dp dışında 2dp TasBeyazi halka. Yerleşimi
 * değiştirmez (yalnızca çizim), bu yüzden odak gelince içerik kaymaz.
 */
private fun Modifier.odakHalkasi(odakli: Boolean, sekil: Shape): Modifier = drawWithContent {
    drawContent()
    if (odakli) {
        val kalinlik = 2.dp.toPx()
        val pay = 3.dp.toPx() + kalinlik / 2f
        val halka = sekil.createOutline(
            Size(size.width + pay * 2f, size.height + pay * 2f),
            layoutDirection,
            this,
        )
        translate(left = -pay, top = -pay) {
            drawOutline(outline = halka, color = TasBeyazi, style = Stroke(width = kalinlik))
        }
    }
}

/**
 * Türkçe büyük harf: `uppercase()` yerel ayardan bağımsızdır ve "i"yi "I"ya
 * çevirir ("Sinop'un İmzası" → "SINOP'UN"). i/ı burada elle eşlenir.
 */
private fun String.turkceBuyukHarf(): String = buildString(length) {
    for (harf in this@turkceBuyukHarf) {
        append(
            when (harf) {
                'i' -> 'İ'
                'ı' -> 'I'
                else -> harf.uppercaseChar()
            },
        )
    }
}
