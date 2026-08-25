package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.kuzey_kapisi_logo
import org.jetbrains.compose.resources.painterResource

private val GENIS_EKRAN_ESIGI = 600.dp

@Composable
fun TopBar(
    onBizKimizClick: () -> Unit,
    onProjeHakkindaClick: () -> Unit,
    onAdminIkonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Uygulama edge-to-edge çalışıyor (MainActivity'de enableEdgeToEdge
                    // + targetSdk 36 ile zorunlu), bu yüzden durum çubuğu inset'i elle
                    // uygulanır. Web'de bu inset sıfır olduğu için fazladan boşluk
                    // oluşmaz — platform dallanmasına gerek yok.
                    .statusBarsPadding()
                    .padding(horizontal = if (genisEkran) 24.dp else 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.kuzey_kapisi_logo),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "KUZEY KAPISI",
                        style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 2.2.sp),
                        color = TasBeyazi,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (genisEkran) 4.dp else 0.dp),
                ) {
                    // Dar ekranda metinler tek satıra sığsın diye kısaltılır ve
                    // iç boşluk daraltılır — admin ikonu da aynı satırda kalabilsin.
                    UstBarBaglantisi(
                        metin = if (genisEkran) "Biz Kimiz" else "Kimiz",
                        dar = !genisEkran,
                        onClick = onBizKimizClick,
                    )
                    UstBarBaglantisi(
                        metin = if (genisEkran) "Proje Hakkında" else "Hakkında",
                        dar = !genisEkran,
                        onClick = onProjeHakkindaClick,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AdminGirisIkonu(onClick = onAdminIkonClick)
                }
            }
            // Üst çubuğu içerikten ayıran ince ışık çizgisi: ortada fener
            // aleviyle hafifçe canlanan, uçlara doğru sönen bir hat.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                FenerAlevi.copy(alpha = 0.28f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun UstBarBaglantisi(metin: String, dar: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "ustBarRengi",
    )
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = if (dar) 8.dp else 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = metin,
            style = MaterialTheme.typography.labelLarge,
            color = renk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Küçük, göze batmayan yuvarlak "kalkan" ikonu — admin paneline giriş
 * noktası. Yazı yok, kasıtlı olarak sade; yalnızca üzerine gelindiğinde
 * fener alevine döner.
 */
@Composable
private fun AdminGirisIkonu(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "adminIkonRengi",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(34.dp)
            .clip(CircleShape)
            .background(renk.copy(alpha = 0.10f))
            .border(1.dp, renk.copy(alpha = 0.35f), CircleShape)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(15.dp)) {
            val w = size.width
            val h = size.height
            val yol = Path().apply {
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.22f)
                lineTo(w, h * 0.55f)
                cubicTo(w, h * 0.85f, w * 0.72f, h * 0.98f, w * 0.5f, h)
                cubicTo(w * 0.28f, h * 0.98f, 0f, h * 0.85f, 0f, h * 0.55f)
                lineTo(0f, h * 0.22f)
                close()
            }
            drawPath(yol, color = renk)
        }
    }
}
