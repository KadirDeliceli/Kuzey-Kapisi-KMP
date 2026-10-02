package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** [ChevronIkonu]'nun yönü. */
enum class ChevronYonu { Asagi, Sag }

/**
 * lucide `chevron-down` / `chevron-right` geometrisi (24'lük ızgara), tek
 * renk çizgi. Emoji ya da metin oku yerine gerçek ikon. Tek kaynak — ana
 * sayfadaki "Keşfet"/kart okları ve breadcrumb ayracı bunu paylaşır.
 */
@Composable
fun ChevronIkonu(yon: ChevronYonu, renk: Color, modifier: Modifier = Modifier, kalinlik: Dp = 2.dp) {
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
            style = Stroke(width = kalinlik.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
