package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kuzeykapisi.app.config.Config
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.default_kapak
import org.jetbrains.compose.resources.painterResource

private val KART_KOSE_YARICAPI = 16.dp

/**
 * Tüm kart tipleri (ana kart, alt kart, bot/kişilik kartı) için TEK paylaşılan
 * kapak bileşeni. Dış boyut (aspectRatio) çağıran taraf tarafından `modifier`
 * ile verilir; bu bileşen sadece o sabit alanı görselle eksiksiz doldurur.
 */
@Composable
fun CoverCard(
    kategori: String,
    kod: String,
    baslik: String,
    etiket: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    anlatimVar: Boolean = false,
    onSesTiklandi: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hoverlu by interactionSource.collectIsHoveredAsState()
    val olcek by animateFloatAsState(
        targetValue = if (hoverlu) 1.04f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "kartOlcek",
    )

    Box(
        modifier = modifier
            .scale(olcek)
            .clip(RoundedCornerShape(KART_KOSE_YARICAPI))
            .shadow(4.dp, RoundedCornerShape(KART_KOSE_YARICAPI))
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = Config.gorselUrl(kategori, kod),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(Res.drawable.default_kapak),
            placeholder = painterResource(Res.drawable.default_kapak),
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(KART_KOSE_YARICAPI)),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.55f),
                        ),
                    )
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (etiket != null) {
                Text(
                    text = etiket,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = baslik,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
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
}

/**
 * Kartın sağ alt köşesine bindirilen, yarı saydam yuvarlak zemin üzerinde
 * hoparlör + ses dalgası ikonu. Kendi `clickable`ı kartın altındaki
 * `clickable`a "bubble" ETMEZ (Compose'ta iç içe clickable'larda dokunuş
 * en derindeki tarafından tüketilir) — bu yüzden ikona dokunmak karta
 * dokunmuş gibi davranmaz.
 */
@Composable
private fun SesIkonuButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            val renk = Color.White

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
