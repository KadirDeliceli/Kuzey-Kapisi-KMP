package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.hareketAzaltilsin

/** "Hareketi azalt" tercihinde nabzın durağan kaldığı orta değer. */
private const val NABIZ_DURAGAN = 0.5f

/**
 * "Yazıyor…" göstergesi — üç noktanın fener alevi tonunda nabız gibi
 * canlanması; opaklıkla birlikte hafif bir ölçek değişimi de var, böylece
 * yanıp sönmek yerine soluk alıp veriyormuş gibi görünür. "Hareketi azalt"
 * tercihinde animasyon hiç kurulmaz, noktalar durağan kalır.
 */
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val gecis = if (hareketAzaltilsin) null else rememberInfiniteTransition(label = "yaziyor")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { sira ->
            val nabiz: Float = if (gecis != null) {
                val canli by gecis.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(620, delayMillis = sira * 160, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "nokta$sira",
                )
                canli
            } else {
                NABIZ_DURAGAN
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(7.dp)
                    .scale(0.72f + 0.28f * nabiz)
                    .clip(CircleShape)
                    .background(FenerAlevi.copy(alpha = Opaklik.YUZDE28 + Opaklik.YUZDE62 * nabiz)),
            )
        }
    }
}
