package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * Liste satırlarındaki "düzenle" (kalem) ikon butonu. Diğer ikonlar gibi
 * (SesIkonuButonu, AdminGirisIkonu, GeriButonu) elle çizilmiş Canvas path —
 * projede harici bir ikon paketi bağımlılığı yok, tutarlılık için aynı yol
 * izleniyor.
 */
@Composable
fun DuzenleIkonuButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val renk = MaterialTheme.colorScheme.secondary
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            rotate(45f) {
                val govdeGenislik = size.width * 0.22f
                val sol = (size.width - govdeGenislik) / 2f
                val ustY = size.height * 0.05f
                val govdeYukseklik = size.height * 0.62f
                drawRect(
                    color = renk,
                    topLeft = Offset(sol, ustY),
                    size = Size(govdeGenislik, govdeYukseklik),
                )
                val ucYol = Path().apply {
                    moveTo(sol, ustY + govdeYukseklik)
                    lineTo(sol + govdeGenislik, ustY + govdeYukseklik)
                    lineTo(sol + govdeGenislik / 2f, ustY + govdeYukseklik + size.height * 0.18f)
                    close()
                }
                drawPath(ucYol, color = renk)
            }
        }
    }
}

/** Liste satırlarındaki "sil" (çöp kutusu) ikon butonu. */
@Composable
fun SilIkonuButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val renk = MaterialTheme.colorScheme.tertiary
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            drawRect(
                color = renk,
                topLeft = Offset(w * 0.38f, 0f),
                size = Size(w * 0.24f, h * 0.12f),
            )
            drawRect(
                color = renk,
                topLeft = Offset(w * 0.14f, h * 0.16f),
                size = Size(w * 0.72f, h * 0.1f),
            )
            val govde = Path().apply {
                moveTo(w * 0.22f, h * 0.32f)
                lineTo(w * 0.78f, h * 0.32f)
                lineTo(w * 0.68f, h * 0.98f)
                lineTo(w * 0.32f, h * 0.98f)
                close()
            }
            drawPath(govde, color = renk)
        }
    }
}

/** Rota mekan listesinde anlatım metni olduğunu belirten, tıklanamaz küçük ses rozeti. */
@Composable
fun AnlatimRozeti(modifier: Modifier = Modifier) {
    val renk = MaterialTheme.colorScheme.secondary
    Box(
        modifier = modifier.size(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
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
            val kalinlik = w * 0.09f
            drawArc(
                color = renk,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.55f, h * 0.15f),
                size = Size(w * 0.35f, h * 0.7f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = kalinlik,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )
        }
    }
}
