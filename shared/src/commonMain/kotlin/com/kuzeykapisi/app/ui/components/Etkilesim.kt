package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.HALE_GECIKMESI
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.fenerHalesiDestekli

/**
 * Bir kartın/butonun etkileşim durumundan türeyen görsel değerler. Tüm kart
 * tiplerinin (ana kart, alt kart, bot kartı, rota durak kartı, admin liste
 * satırı, tur kartı) aynı dili konuşması için TEK kaynaktan üretilir.
 *
 * - Durağan hâlde kenarlık neredeyse görünmezdir (SisGrisi, %10 opaklık).
 * - Hover'da (web) / basılıyken (mobil) kenarlık [FenerAlevi]'ne döner ve
 *   kalınlığı 1dp → 1.5dp artar; geçiş ~200ms yumuşaktır.
 * - Basılıyken mobil geri bildirimi olarak kart %97'ye küçülür.
 * - [hoverdeBuyur] true ise (ana sayfa/kapak kartları) fareli web'de hover'da
 *   kart ayrıca %102'ye büyür — her kart bu tek kaynaktan [olcek] okumalı,
 *   kendi ayrı animateFloatAsState'ini kurmamalı (aynı ölçeğin iki kez
 *   hesaplanmasını önler).
 */
@Immutable
data class KartEtkilesimi(
    val hoverlu: Boolean,
    val basili: Boolean,
    val kenarRengi: Color,
    val kenarKalinligi: Dp,
    val olcek: Float,
)

@Composable
fun kartEtkilesimi(interactionSource: MutableInteractionSource, hoverdeBuyur: Boolean = false): KartEtkilesimi {
    val hoverlu by interactionSource.collectIsHoveredAsState()
    val basili by interactionSource.collectIsPressedAsState()
    val vurgulu = hoverlu || basili

    val kenarRengi by animateColorAsState(
        targetValue = if (vurgulu) FenerAlevi else SisGrisi.copy(alpha = Opaklik.YUZDE10),
        animationSpec = tween(MIKRO_SURE),
        label = "kenarRengi",
    )
    val kenarKalinligi by animateDpAsState(
        targetValue = if (vurgulu) 1.5.dp else 1.dp,
        animationSpec = tween(MIKRO_SURE),
        label = "kenarKalinligi",
    )
    val olcek by animateFloatAsState(
        targetValue = if (hoverdeBuyur) etkilesimOlcegi(hoverlu, basili) else if (basili) 0.97f else 1f,
        animationSpec = tween(MIKRO_SURE),
        label = "kartOlcegi",
    )

    return KartEtkilesimi(
        hoverlu = hoverlu,
        basili = basili,
        kenarRengi = kenarRengi,
        kenarKalinligi = kenarKalinligi,
        olcek = olcek,
    )
}

/**
 * Hover büyümesi yalnızca fareli web'de; dokunmatikte yalnızca basma
 * küçülmesi. [kartEtkilesimi]'nin `hoverdeBuyur=true` hâli ve kart
 * soyutlamasına uymayan bileşenler (ör. KesfetButonu) bu TEK formülü paylaşır.
 */
fun etkilesimOlcegi(hoverlu: Boolean, basili: Boolean): Float = when {
    basili -> 0.97f
    hoverlu && fenerHalesiDestekli -> 1.02f
    else -> 1f
}

/**
 * TÜM tıklanabilir bileşenlerin (buton, chip, kart, bağlantı) paylaştığı
 * ORTAK etkileşim modifier'ı: hover (web) + basma + [Role.Button] semantiği,
 * ve (verilirse) [contentDescription] TEK yerden uygulanır. Klavye odak
 * halkası [klavyeOdakHalkasi] ile AYRI kalır — şeklin dışına taştığı için
 * `clip`'ten önce, bu modifier'dan (ki genelde clip'ten SONRA gelir) önce
 * uygulanmalıdır.
 */
fun Modifier.etkilesimli(
    interactionSource: MutableInteractionSource,
    etkin: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit,
): Modifier = this
    .hoverable(interactionSource = interactionSource, enabled = etkin)
    .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = etkin,
        role = Role.Button,
        onClick = onClick,
    )
    .let { m ->
        if (contentDescription != null) {
            m.semantics { this.contentDescription = contentDescription }
        } else {
            m
        }
    }

/**
 * İMZA MİKRO-ETKİLEŞİM — fener ışığı halesi.
 *
 * Kartın/butonun ARKASINDA beliren, çok hafif ve bulanık [FenerAlevi] halesi.
 * YALNIZCA web'de ([fenerHalesiDestekli]) ve fare imleci üzerine geldiğinde
 * çalışır; mobilde hiç çizilmez (orada geri bildirim basma küçülmesidir).
 *
 * Kasıtlı olarak yalnızca birkaç yerde kullanılır: ana sayfa kartları ve
 * birincil CTA butonları. Her kartta kullanılmaz — nadirliği efektin
 * imzalık kalmasını sağlar.
 */
@Composable
fun BoxScope.FenerHalesi(gorunur: Boolean, sekil: Shape = KartSekli) {
    if (!fenerHalesiDestekli) return

    val yogunluk by animateFloatAsState(
        targetValue = if (gorunur) 0.16f else 0f,
        animationSpec = tween(
            durationMillis = 260,
            delayMillis = if (gorunur) HALE_GECIKMESI else 0,
        ),
        label = "haleYogunlugu",
    )
    if (yogunluk <= 0.001f) return

    Box(
        modifier = Modifier
            .matchParentSize()
            .blur(radius = 26.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FenerAlevi.copy(alpha = yogunluk),
                        FenerAlevi.copy(alpha = yogunluk * 0.45f),
                        Color.Transparent,
                    ),
                ),
                shape = sekil,
            ),
    )
}
