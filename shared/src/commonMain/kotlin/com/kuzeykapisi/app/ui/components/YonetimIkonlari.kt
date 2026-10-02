package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.ButonSekli
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.SinopKirmizisi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.YosunAcik

/**
 * Liste satırlarındaki "düzenle" (kalem) ikon butonu. Gerçek Material3
 * [IconButton] kabuğu (dokunma hedefi, erişilebilirlik rolü, standart ripple)
 * + elle çizilmiş bir Canvas glif — diğer ikonlar gibi (SesIkonuButonu,
 * AdminGirisIkonu, GeriButonu) projede harici bir ikon paketi bağımlılığı
 * yok, tutarlılık için aynı çizim yolu izleniyor. Marka rengine dönen ince
 * hover zemini, ripple'ın üstüne binen ek bir katman.
 */
@Composable
fun DuzenleIkonuButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "duzenleRengi",
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(etkilesim.olcek)
            .size(40.dp)
            .clip(ButonSekli)
            .background(renk.copy(alpha = Opaklik.YUZDE8))
            .semantics { contentDescription = "Düzenle" },
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

/**
 * Liste satırlarındaki "sil" (çöp kutusu) ikon butonu — gerçek Material3
 * [IconButton] kabuğuyla.
 *
 * YIKICI eylem olduğu için [SinopKirmizisi] kullanır — bu rengin uygulamadaki
 * iki dar rolünden biri. Durağan hâlde soluk, üzerine gelindiğinde tam
 * doygunlukta görünür; böylece yanlışlıkla dokunulacak kadar davetkâr olmaz
 * ama nişan alındığında niyeti net söyler.
 */
@Composable
fun SilIkonuButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val vurgulu = etkilesim.hoverlu || etkilesim.basili
    val renk by animateColorAsState(
        targetValue = if (vurgulu) SinopKirmizisi else SinopKirmizisi.copy(alpha = Opaklik.YUZDE62),
        animationSpec = tween(MIKRO_SURE),
        label = "silRengi",
    )
    val zemin by animateColorAsState(
        targetValue = SinopKirmizisi.copy(alpha = if (vurgulu) 0.16f else 0f),
        animationSpec = tween(MIKRO_SURE),
        label = "silZemin",
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(etkilesim.olcek)
            .size(40.dp)
            .clip(ButonSekli)
            .background(zemin)
            .semantics { contentDescription = "Sil" },
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

/**
 * Rota mekan listesinde anlatım metni olduğunu belirten, tıklanamaz küçük ses
 * rozeti. Bilgi verir, eylem değildir — bu yüzden sakin [YosunAcik] tonunda.
 */
@Composable
fun AnlatimRozeti(modifier: Modifier = Modifier) {
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
            drawPath(govde, color = YosunAcik)
            drawArc(
                color = YosunAcik,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.55f, h * 0.15f),
                size = Size(w * 0.35f, h * 0.7f),
                style = Stroke(width = w * 0.09f, cap = StrokeCap.Round),
            )
        }
    }
}
