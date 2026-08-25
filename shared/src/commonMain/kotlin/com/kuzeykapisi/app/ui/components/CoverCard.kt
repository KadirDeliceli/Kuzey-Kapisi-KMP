package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.SinopKirmizisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.default_kapak
import org.jetbrains.compose.resources.painterResource

/**
 * Tüm kart tipleri (ana kart, alt kart, bot/kişilik kartı) için TEK paylaşılan
 * kapak bileşeni. Modern bir "medya kartı" yapısı: görsel yazı taşımaz, kendi
 * oranını ([gorselOran]) koruyan ayrı bir üst bölge olarak durur; başlık ve
 * etiket, görselin ALTINDA ayrı bir içerik [Surface]'inde, bol boşluk ve net
 * bir tipografi hiyerarşisiyle gösterilir. Dış genişlik çağıran taraf
 * tarafından `modifier` ile verilir (`fillMaxWidth()`/`weight()`); toplam
 * yükseklik görsel + içerik bölgesinin doğal boyutundan gelir — çağıran taraf
 * KARTIN TAMAMINA `aspectRatio` UYGULAMAMALIDIR, yalnızca [gorselOran]'ı
 * kullanmalıdır.
 *
 * Görsel dil:
 *  - Tam simetrik, yumuşak köşeli [KartSekli] (20dp) formu + zeminden ayrışan
 *    tonal gölge (hover/basılıyken gölge biraz büyür).
 *  - Durağan hâlde neredeyse görünmez kenarlık; hover/basılıyken fener alevi.
 *  - İçerik bölgesi [DerinDeniz] yüzey rengiyle görselden net biçimde ayrışır
 *    — böylece hiçbir metin artık doğrudan fotoğrafın üzerine yazılmaz.
 *  - [hale] yalnızca ana sayfa kartlarında açılır (web'de fener halesi).
 *  - [muhur] yalnızca Tescilli Ürünler kategorisinde açılır; görselin sağ-üst
 *    köşesine ince bir [SinopKirmizisi] coğrafi işaret mührü koyar.
 */
@Composable
fun CoverCard(
    kategori: String,
    kod: String,
    baslik: String,
    etiket: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gorselOran: Float = 4f / 3f,
    anlatimVar: Boolean = false,
    onSesTiklandi: (() -> Unit)? = null,
    hale: Boolean = false,
    muhur: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)

    Box(modifier = modifier.scale(etkilesim.olcek)) {
        if (hale) FenerHalesi(gorunur = etkilesim.hoverlu, sekil = KartSekli)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (etkilesim.hoverlu || etkilesim.basili) 12.dp else 6.dp,
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
                    onClick = onClick,
                ),
        ) {
            // --- Görsel bölgesi: yalnızca fotoğraf, üstünde METİN YOK. ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(gorselOran),
            ) {
                AsyncImage(
                    model = Config.gorselUrl(kategori, kod),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = painterResource(Res.drawable.default_kapak),
                    placeholder = painterResource(Res.drawable.default_kapak),
                    modifier = Modifier.matchParentSize(),
                )

                if (muhur) {
                    CografiIsaretMuhru(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                    )
                }

                if (anlatimVar && onSesTiklandi != null) {
                    SesIkonuButonu(
                        onClick = onSesTiklandi,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                    )
                }
            }

            // --- İçerik bölgesi: görselden ayrı bir yüzey, bol padding. ---
            Surface(color = DerinDeniz) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    if (etiket != null) {
                        Text(
                            text = etiket.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                            color = FenerAlevi,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Text(
                        text = baslik,
                        style = MaterialTheme.typography.titleLarge,
                        color = TasBeyazi,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Tescilli ürün kartlarına konan küçük mühür/damga detayı — resmi bir coğrafi
 * işaret hissi verir, göze batmaz. [SinopKirmizisi]'nin izin verilen iki
 * kullanımından biri (diğeri: admin panelindeki yıkıcı eylemler). [CoverCard]
 * dışında yalnızca ana sayfanın zig-zag satırındaki görsel bloğunda ([AnaSatir])
 * tekrar kullanılır — bu yüzden `internal` görünürlüktedir.
 */
@Composable
internal fun CografiIsaretMuhru(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(KaranlikLacivert.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(15.dp)) {
            val w = size.width
            val h = size.height
            val kalinlik = w * 0.09f
            // Dış halka + içeride tırtıklı bir yıldız izlenimi: mühür baskısı.
            drawCircle(
                color = SinopKirmizisi,
                radius = w * 0.46f,
                style = Stroke(width = kalinlik),
            )
            val yildiz = Path().apply {
                moveTo(w * 0.5f, h * 0.20f)
                lineTo(w * 0.60f, h * 0.44f)
                lineTo(w * 0.82f, h * 0.46f)
                lineTo(w * 0.65f, h * 0.62f)
                lineTo(w * 0.71f, h * 0.83f)
                lineTo(w * 0.5f, h * 0.71f)
                lineTo(w * 0.29f, h * 0.83f)
                lineTo(w * 0.35f, h * 0.62f)
                lineTo(w * 0.18f, h * 0.46f)
                lineTo(w * 0.40f, h * 0.44f)
                close()
            }
            drawPath(yildiz, color = SinopKirmizisi)
        }
    }
}

/**
 * Kartın görsel bölgesinin sağ alt köşesine bindirilen, yarı saydam yuvarlak
 * zemin üzerinde hoparlör + ses dalgası ikonu — bir video kapağındaki "oynat"
 * rozetiyle aynı dili konuşur. Kendi `clickable`ı kartın altındaki
 * `clickable`a "bubble" ETMEZ (Compose'ta iç içe clickable'larda dokunuş
 * en derindeki tarafından tüketilir) — bu yüzden ikona dokunmak karta
 * dokunmuş gibi davranmaz.
 */
@Composable
fun SesIkonuButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val renk = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else TasBeyazi

    Box(
        modifier = modifier
            .scale(etkilesim.olcek)
            .size(34.dp)
            .clip(CircleShape)
            .background(KaranlikLacivert.copy(alpha = 0.62f))
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height

            // Hoparlör gövdesi: kare + sağa açılan huni.
            val govde = Path().apply {
                moveTo(0f, h * 0.35f)
                lineTo(w * 0.35f, h * 0.35f)
                lineTo(w * 0.62f, h * 0.1f)
                lineTo(w * 0.62f, h * 0.9f)
                lineTo(w * 0.35f, h * 0.65f)
                lineTo(0f, h * 0.65f)
                close()
            }
            drawPath(govde, color = renk)

            // Ses dalgaları: hoparlörün sağında iki iç içe yay.
            val kalinlik = w * 0.09f
            drawArc(
                color = renk,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.55f, h * 0.15f),
                size = Size(w * 0.35f, h * 0.7f),
                style = Stroke(width = kalinlik, cap = StrokeCap.Round),
            )
            drawArc(
                color = renk,
                startAngle = -35f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(w * 0.72f, h * 0.25f),
                size = Size(w * 0.28f, h * 0.5f),
                style = Stroke(width = kalinlik, cap = StrokeCap.Round),
            )
        }
    }
}
