package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    }
}
