package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.AlcakYuzey
import com.kuzeykapisi.app.ui.theme.ButonSekli
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

/**
 * Projedeki TEK seçim chip'i — rota süresi/ilgi alanları ve admin kategori
 * seçicileri bunu kullanır.
 *
 * Seçili durum, [FenerAlevi]'nin izin verilen rollerinden biridir: dolu fener
 * alevi zemin + koyu metin. Seçili değilken sakin bir kutu; üzerine
 * gelindiğinde yalnızca kenarı fener alevine döner.
 */
@Composable
fun KuzeyChip(
    etiket: String,
    secili: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val vurgulu = etkilesim.hoverlu || etkilesim.basili

    val zemin by animateColorAsState(
        targetValue = if (secili) FenerAlevi else AlcakYuzey,
        animationSpec = tween(MIKRO_SURE),
        label = "chipZemin",
    )
    val metinRengi by animateColorAsState(
        targetValue = when {
            secili -> KaranlikLacivert
            vurgulu -> TasBeyazi
            else -> SisGrisi
        },
        animationSpec = tween(MIKRO_SURE),
        label = "chipMetin",
    )
    val kenar by animateColorAsState(
        targetValue = when {
            secili -> FenerAlevi
            vurgulu -> FenerAlevi
            else -> SisGrisi.copy(alpha = 0.22f)
        },
        animationSpec = tween(MIKRO_SURE),
        label = "chipKenar",
    )

    Box(
        modifier = modifier
            .scale(etkilesim.olcek)
            .shadow(
                elevation = if (secili) 3.dp else 0.dp,
                shape = ButonSekli,
                ambientColor = FenerAlevi,
                spotColor = FenerAlevi,
            )
            .clip(ButonSekli)
            .background(zemin)
            .border(etkilesim.kenarKalinligi, kenar, ButonSekli)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = etiket,
            style = MaterialTheme.typography.labelLarge,
            color = metinRengi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
