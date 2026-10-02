package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.ButonSekli
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.klavyeOdakHalkasi

/** Breadcrumb'ın bir basamağı. Son basamak aktif (geçerli) sayfadır ve tıklanamaz. */
data class BreadcrumbBasamagi(val ad: String, val onClick: () -> Unit)

/**
 * "Ana Sayfa › Başlık › Alt başlık". Üst basamaklar SisGrisi bağlantılar
 * (üzerine gelince/basılınca TasBeyazi + alt çizgi, ≥40dp dokunma yüksekliği,
 * klavye odak halkası); son basamak [aktif] FenerAlevi ve "geçerli sayfa"
 * olarak bildirilir. Ayraç ince bir chevron çizgisidir, ekran okuyucuya okunmaz.
 * Hanken Grotesk (labelLarge).
 */
@Composable
fun Breadcrumb(
    ustBasamaklar: List<BreadcrumbBasamagi>,
    aktif: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (basamak in ustBasamaklar) {
            BreadcrumbBaglantisi(basamak)
            // 1.5dp kalınlık BİLEREK korunur: ana sayfadaki paylaşılan
            // ChevronIkonu'nun varsayılanı (2dp) bu küçük ayraç için fazla kalın.
            ChevronIkonu(
                yon = ChevronYonu.Sag,
                renk = SisGrisi,
                modifier = Modifier.padding(horizontal = 2.dp).size(14.dp),
                kalinlik = 1.5.dp,
            )
        }
        Text(
            text = aktif,
            style = MaterialTheme.typography.labelLarge,
            color = FenerAlevi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 6.dp)
                .semantics { stateDescription = "geçerli sayfa" },
        )
    }
}

@Composable
private fun BreadcrumbBaglantisi(basamak: BreadcrumbBasamagi) {
    val kaynak = remember { MutableInteractionSource() }
    val hoverlu by kaynak.collectIsHoveredAsState()
    val basili by kaynak.collectIsPressedAsState()
    val odakli by kaynak.collectIsFocusedAsState()
    val vurgulu = hoverlu || basili
    val renk by animateColorAsState(
        targetValue = if (vurgulu) TasBeyazi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "breadcrumbBaglantisi",
    )

    Box(
        modifier = Modifier
            .klavyeOdakHalkasi(odakli, ButonSekli)
            .clip(ButonSekli)
            .hoverable(interactionSource = kaynak)
            .clickable(
                interactionSource = kaynak,
                indication = null,
                role = Role.Button,
                onClick = basamak.onClick,
            )
            .heightIn(min = 40.dp)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = basamak.ad,
            style = MaterialTheme.typography.labelLarge.copy(
                textDecoration = if (vurgulu) TextDecoration.Underline else TextDecoration.None,
            ),
            color = renk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
