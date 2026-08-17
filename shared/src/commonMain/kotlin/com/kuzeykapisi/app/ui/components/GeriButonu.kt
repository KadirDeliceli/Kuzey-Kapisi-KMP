package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Geri oku + etiket. İkon görsel olarak ince ve küçük (18dp), ama dokunma
 * alanı ~40dp yükseklikte tutulur. Çizgi kalınlığı ve yuvarlak uçlar
 * ChatSheet'teki X (kapat) ikonuyla aynı görsel dili paylaşır.
 */
@Composable
fun GeriButonu(metin: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val renk = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .heightIn(min = 40.dp)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            // Dar ve dikeyde hafif içe alınmış bir chevron — kaba bir "<"
            // yerine ince bir ok hissi verir.
            val sagX = w * 0.64f
            val solX = w * 0.30f
            val yol = Path().apply {
                moveTo(sagX, h * 0.22f)
                lineTo(solX, h * 0.5f)
                lineTo(sagX, h * 0.78f)
            }
            drawPath(
                path = yol,
                color = renk,
                style = Stroke(width = w * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        Text(
            text = metin,
            style = MaterialTheme.typography.labelLarge,
            color = renk,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
