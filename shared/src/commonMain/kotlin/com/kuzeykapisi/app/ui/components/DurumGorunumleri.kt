package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

/**
 * Yükleniyor göstergesi — dönen halka yerine bir fener nabzı: ortada sabit bir
 * ışık noktası, dışa doğru soluklaşarak genişleyen iki halka.
 */
@Composable
fun NabizGostergesi(modifier: Modifier = Modifier, boyut: Dp = 40.dp) {
    val gecis = rememberInfiniteTransition(label = "nabiz")
    val evre by gecis.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1600, easing = LinearEasing)),
        label = "nabizEvresi",
    )

    Canvas(modifier = modifier.size(boyut)) {
        val merkezYaricap = size.minDimension * 0.11f
        drawCircle(color = FenerAlevi, radius = merkezYaricap)

        // İki halka yarım evre kaydırılır — kesintisiz bir nabız hissi verir.
        listOf(evre, (evre + 0.5f) % 1f).forEach { p ->
            val yaricap = size.minDimension * (0.14f + 0.36f * p)
            drawCircle(
                color = FenerAlevi.copy(alpha = (1f - p) * 0.55f),
                radius = yaricap,
                style = Stroke(width = size.minDimension * 0.045f),
            )
        }
    }
}

/** Ortalanmış yükleniyor durumu; [metin] verilirse nabzın altına yazılır. */
@Composable
fun YukleniyorGorunumu(modifier: Modifier = Modifier, metin: String? = null) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NabizGostergesi()
            if (metin != null) {
                Text(
                    text = metin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SisGrisi,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Hata durumu — kısa, doğrudan, özür dilemeyen bir dil. [onTekrarDene]
 * verilirse altında bir "Tekrar dene" butonu görünür.
 */
@Composable
fun HataGorunumu(
    mesaj: String,
    modifier: Modifier = Modifier,
    onTekrarDene: (() -> Unit)? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = mesaj,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            if (onTekrarDene != null) {
                IkincilButon(
                    metin = "Tekrar dene",
                    onClick = onTekrarDene,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

/** Satır içi (tam ekran olmayan) hata/uyarı metni. */
@Composable
fun HataMetni(mesaj: String, modifier: Modifier = Modifier) {
    Text(
        text = mesaj,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}

/** Satır içi başarı/onay metni. */
@Composable
fun BasariMetni(mesaj: String, modifier: Modifier = Modifier) {
    Text(
        text = mesaj,
        style = MaterialTheme.typography.bodyMedium,
        color = TasBeyazi,
        modifier = modifier,
    )
}

/** Boş liste durumu — sakin, suçlayıcı olmayan tek satır. */
@Composable
fun BosDurumGorunumu(mesaj: String, modifier: Modifier = Modifier) {
    Text(
        text = mesaj,
        style = MaterialTheme.typography.bodyMedium,
        color = SisGrisi,
        modifier = modifier.fillMaxWidth(),
    )
}
