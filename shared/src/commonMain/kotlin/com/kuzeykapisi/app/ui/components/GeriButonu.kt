package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
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

@Composable
fun GeriButonu(metin: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val renk = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
            val yol = Path().apply {
                moveTo(w, 0f)
                lineTo(0f, h / 2f)
                lineTo(w, h)
            }
            drawPath(
                path = yol,
                color = renk,
                style = Stroke(width = w * 0.2f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        Text(
            text = metin,
            style = MaterialTheme.typography.labelLarge,
            color = renk,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
