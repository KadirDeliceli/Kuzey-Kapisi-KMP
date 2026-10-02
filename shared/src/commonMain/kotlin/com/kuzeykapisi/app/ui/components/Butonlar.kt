package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.ButonSekli
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.SinopKirmizisi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.Yukseklik
import com.kuzeykapisi.app.ui.theme.klavyeOdakHalkasi

/**
 * Birincil eylem butonu — tek dolu [FenerAlevi] yüzey. Uygulamadaki tek
 * "fener alevi dolgusu" burasıdır; bu yüzden bir ekranda genelde bir tane
 * bulunur.
 *
 * [hale] true ise web'de fare üzerine geldiğinde arkasında fener halesi belirir
 * (yalnızca birincil CTA'larda kullanılır).
 *
 * [yukleniyor] true iken buton SOLUKLAŞTIRILMAZ (bu [etkin]=false'un işi,
 * gerçek bir devre-dışı durum içindir) — tam güçte kalır, [metin]in yanına
 * küçük bir dönen gösterge eklenir ve tıklama devre dışı kalır. "Devre dışı
 * buton" görünümü "bunu yapamazsın" der, bu ise "bu şu an oluyor" der.
 */
@Composable
fun BirincilButon(
    metin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etkin: Boolean = true,
    yukleniyor: Boolean = false,
    hale: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val tiklanabilir = etkin && !yukleniyor
    val zemin by animateColorAsState(
        targetValue = when {
            !etkin -> FenerAlevi.copy(alpha = Opaklik.YUZDE30)
            etkilesim.hoverlu || etkilesim.basili -> FenerAlevi
            else -> FenerAlevi.copy(alpha = Opaklik.YUZDE92)
        },
        animationSpec = tween(MIKRO_SURE),
        label = "birincilZemin",
    )

    Box(modifier = modifier.scale(etkilesim.olcek), contentAlignment = Alignment.Center) {
        if (hale && tiklanabilir) FenerHalesi(gorunur = etkilesim.hoverlu, sekil = ButonSekli)
        Box(
            modifier = Modifier
                .shadow(
                    elevation = if (!etkin) Yukseklik.DP0 else if (etkilesim.hoverlu || etkilesim.basili) Yukseklik.DP8 else Yukseklik.DP4,
                    shape = ButonSekli,
                    ambientColor = FenerAlevi,
                    spotColor = FenerAlevi,
                )
                .klavyeOdakHalkasi(odakli, ButonSekli)
                .clip(ButonSekli)
                .background(zemin)
                .etkilesimli(interactionSource, tiklanabilir, onClick = onClick)
                .heightIn(min = 48.dp)
                .padding(horizontal = 24.dp, vertical = 14.dp)
                .let { m -> if (yukleniyor) m.semantics { stateDescription = "Yükleniyor" } else m },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (yukleniyor) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = KaranlikLacivert,
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    text = metin,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (etkin) KaranlikLacivert else KaranlikLacivert.copy(alpha = Opaklik.YUZDE55),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Admin form ekranlarının (Persona/Rota Yeri Ekle/Düzenle) paylaştığı kaydet
 * butonu — dört ekranda ayrı ayrı tekrarlanan AYNI [BirincilButon] çağrısının
 * yerine.
 */
@Composable
fun KaydetButonu(kaydediliyor: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BirincilButon(
        metin = "Kaydet",
        onClick = onClick,
        yukleniyor = kaydediliyor,
        hale = true,
        modifier = modifier,
    )
}

/**
 * İkincil eylem butonu — çerçeveli, dolgusuz. Durağan hâlde soluk bir kenarlık,
 * hover/basılıyken [FenerAlevi] kenarlık.
 */
@Composable
fun IkincilButon(
    metin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etkin: Boolean = true,
    onEk: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = modifier
            .scale(etkilesim.olcek)
            .klavyeOdakHalkasi(odakli, ButonSekli)
            .clip(ButonSekli)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, ButonSekli)
            .etkilesimli(interactionSource, etkin, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        onEk?.invoke()
        Text(
            text = metin,
            style = MaterialTheme.typography.labelLarge,
            color = if (etkin) TasBeyazi else SisGrisi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * YIKICI eylem butonu — YALNIZCA admin panelindeki geri alınamaz işlemlerde
 * (silme onayı) kullanılır. Uygulamanın başka hiçbir yerinde
 * [SinopKirmizisi] bu rolde görünmez.
 */
@Composable
fun YikiciButon(
    metin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etkin: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val zemin by animateColorAsState(
        targetValue = when {
            !etkin -> SinopKirmizisi.copy(alpha = Opaklik.YUZDE30)
            etkilesim.hoverlu || etkilesim.basili -> SinopKirmizisi
            else -> SinopKirmizisi.copy(alpha = Opaklik.YUZDE90)
        },
        animationSpec = tween(MIKRO_SURE),
        label = "yikiciZemin",
    )

    Box(
        modifier = modifier
            .scale(etkilesim.olcek)
            .shadow(
                elevation = if (!etkin) Yukseklik.DP0 else if (etkilesim.hoverlu || etkilesim.basili) Yukseklik.DP8 else Yukseklik.DP4,
                shape = ButonSekli,
                ambientColor = SinopKirmizisi,
                spotColor = SinopKirmizisi,
            )
            .klavyeOdakHalkasi(odakli, ButonSekli)
            .clip(ButonSekli)
            .background(zemin)
            .etkilesimli(interactionSource, etkin, onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = metin,
            style = MaterialTheme.typography.labelLarge,
            color = TasBeyazi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Dialoglarda "Vazgeç" gibi düşük vurgulu, çerçevesiz eylemler. */
@Composable
fun SessizButon(
    metin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etkin: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = when {
            !etkin -> SisGrisi.copy(alpha = Opaklik.YUZDE50)
            etkilesim.hoverlu || etkilesim.basili -> FenerAlevi
            else -> SisGrisi
        },
        animationSpec = tween(MIKRO_SURE),
        label = "sessizRenk",
    )
    Box(
        modifier = modifier
            .klavyeOdakHalkasi(odakli, ButonSekli)
            .clip(ButonSekli)
            .etkilesimli(interactionSource, etkin, onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
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
