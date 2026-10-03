package com.kuzeykapisi.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kuzeykapisi.app.platform.rememberUygulamadanCikis
import com.kuzeykapisi.app.ui.components.etkilesimli
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.ButonSekli
import com.kuzeykapisi.app.ui.theme.DialogSekli
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.SatirSekli
import com.kuzeykapisi.app.ui.theme.SozlesmeButonBasili
import com.kuzeykapisi.app.ui.theme.SozlesmeButonHover
import com.kuzeykapisi.app.ui.theme.SozlesmeMetni
import com.kuzeykapisi.app.ui.theme.SozlesmeMetniIkincil
import com.kuzeykapisi.app.ui.theme.SozlesmeVurguYuzeyi
import com.kuzeykapisi.app.ui.theme.SozlesmeZemini
import com.kuzeykapisi.app.ui.theme.Yukseklik
import com.kuzeykapisi.app.ui.theme.klavyeOdakHalkasi

/** Kartın en geniş hâli; dar ekranda kenarlarda 16dp boşluk bırakarak daralır. */
private val KART_GENISLIGI = 520.dp

/** Kartın en uzun hâli; daha uzun gövde kartın içinde kayar. */
private val KART_YUKSEKLIGI = 640.dp

/** Kart içinde bu genişliğin üstünde onay kutusu ve buton yan yana dizilir. */
private val YAN_YANA_ESIGI = 420.dp

private class KosulBolumu(val baslik: String, val metin: String)

// TASLAK METİN — proje sahibi tarafından gözden geçirilip değiştirilecek.
private val KOSUL_BOLUMLERI = listOf(
    KosulBolumu(
        baslik = "Uygulamanın amacı",
        metin = "Kuzey Kapısı, Sinop'un tarihini, kültürünü, doğasını ve lezzetlerini " +
            "tanıtan bağımsız bir bilgilendirme ve rehberlik uygulamasıdır. Yapay zeka " +
            "rehberleriyle sohbet etmenizi, sesli anlatımlar dinlemenizi ve size özel " +
            "gezi rotaları oluşturmanızı sağlar.",
    ),
    KosulBolumu(
        baslik = "Yapay zeka tarafından üretilen içerik",
        metin = "Sohbet rehberlerinin yanıtları, sesli anlatımlar ve rota önerileri yapay " +
            "zeka yardımıyla üretilir. Bu içerikler her zaman eksiksiz ya da yüzde yüz " +
            "doğru olmayabilir. Karakterler kurgusaldır ve verdikleri yanıtlar resmi bir " +
            "görüş niteliği taşımaz.",
    ),
    KosulBolumu(
        baslik = "Bilgilerin doğrulanması",
        metin = "Uygulamadaki bilgiler genel bilgilendirme amaçlıdır. Sağlık, güvenlik, " +
            "ulaşım, açılış saatleri ve ücretler gibi önemli ya da resmi kararlarınızda " +
            "ilgili kurumların doğrulanmış kaynaklarına başvurun. Rota süre ve " +
            "mesafelerini tahmini olarak değerlendirin; yolculuk sırasında trafik " +
            "kurallarına ve yerel uyarılara uyun.",
    ),
    KosulBolumu(
        baslik = "Kişisel veriler, konum ve mikrofon",
        metin = "Konum izni verirseniz konumunuz yalnızca size uygun rotaları hazırlamak " +
            "için kullanılır ve rota hesaplaması için sunucuya gönderilir. Mikrofon " +
            "yalnızca sesli soru sormak için kaydı siz başlattığınızda kullanılır; kayıt, " +
            "yanıt üretilmesi için sunucuya iletilir. Yazdığınız mesajlar da yanıt " +
            "üretmek için sunucuda işlenir. Verdiğiniz izinleri dilediğiniz zaman " +
            "cihazınızın ya da tarayıcınızın ayarlarından geri alabilirsiniz. " +
            "Sohbetlerde kişisel veya hassas bilgi paylaşmamanızı öneririz.",
    ),
    KosulBolumu(
        baslik = "İletişim",
        metin = "Bu koşullarla ilgili sorularınız ve geri bildirimleriniz için " +
            "kadirdeliceli.dev@gmail.com adresine yazabilirsiniz.",
    ),
)

/**
 * İlk açılışta, koşullar kabul edilene kadar ana sayfanın ÜSTÜNDE duran,
 * ortalanmış Kullanım Koşulları dialogu. Karartmaya tıklamak onu kapatmaz;
 * tek çıkış, onay kutusu işaretlendikten sonra etkinleşen "Devam et"tir.
 * Android'de geri tuşu dialogu kapatmaz, uygulamadan çıkar (bir sonraki
 * açılışta dialog yeniden gelir).
 *
 * Köşe formu ve gölge projenin diğer dialoglarıyla aynıdır ([DialogSekli]);
 * yalnızca renk bilinçli olarak temanın dışındadır: düz beyaz zemin, siyah
 * metin (bkz. Color.kt, "Sözleşme yüzeyi").
 */
@Composable
fun KullanimKosullariDialogu(onKabulEt: () -> Unit) {
    var isaretli by rememberSaveable { mutableStateOf(false) }
    val uygulamadanCik = rememberUygulamadanCikis()

    Dialog(
        // Yalnızca geri tuşu (web'de Escape) buraya düşer; karartma tıklaması
        // kapalı. Android'de uygulamadan çıkar, web/iOS'ta hiçbir şey yapmaz.
        onDismissRequest = uygulamadanCik,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = KART_GENISLIGI)
                .fillMaxWidth()
                .heightIn(max = KART_YUKSEKLIGI)
                .shadow(elevation = Yukseklik.DP20, shape = DialogSekli)
                .clip(DialogSekli)
                .background(SozlesmeZemini),
        ) {
            // Gövde kartın İÇİNDE kayar; alt eylem çubuğu sabit kalır.
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 20.dp),
            ) {
                Text(
                    text = "Kullanım Koşulları",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SozlesmeMetni,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Uygulamayı kullanmaya başlamadan önce lütfen aşağıdaki koşulları okuyun.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SozlesmeMetniIkincil,
                    modifier = Modifier.padding(top = 10.dp),
                )
                KOSUL_BOLUMLERI.forEach { bolum ->
                    Text(
                        text = bolum.baslik,
                        style = MaterialTheme.typography.titleMedium,
                        color = SozlesmeMetni,
                        modifier = Modifier.padding(top = 24.dp).semantics { heading() },
                    )
                    Text(
                        text = bolum.metin,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SozlesmeMetni,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // Kayan gövdeyle sabit eylem çubuğunu ayıran çizgi: metnin bu
            // çizginin altında kesilmesi, devamının kaydırılabildiğini gösterir.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SozlesmeMetniIkincil.copy(alpha = Opaklik.YUZDE30)),
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                if (maxWidth >= YAN_YANA_ESIGI) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OnayKutusu(isaretli, onDegisti = { isaretli = it }, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        DevamButonu(etkin = isaretli, onClick = onKabulEt)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OnayKutusu(isaretli, onDegisti = { isaretli = it }, modifier = Modifier.fillMaxWidth())
                        DevamButonu(etkin = isaretli, onClick = onKabulEt, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

/**
 * Satırın TAMAMI tıklanabilir (yalnızca 20dp'lik kutu değil) — hedef 48dp
 * yüksekliğinde. Ekran okuyucuya tek bir "onay kutusu" olarak duyurulur.
 */
@Composable
private fun OnayKutusu(isaretli: Boolean, onDegisti: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val hoverlu by interactionSource.collectIsHoveredAsState()
    val basili by interactionSource.collectIsPressedAsState()
    val odakli by interactionSource.collectIsFocusedAsState()
    val zemin by animateColorAsState(
        targetValue = if (hoverlu || basili) SozlesmeVurguYuzeyi else Color.Transparent,
        animationSpec = tween(MIKRO_SURE),
        label = "onayZemini",
    )

    Row(
        modifier = modifier
            .klavyeOdakHalkasi(odakli, SatirSekli, renk = SozlesmeMetni)
            .clip(SatirSekli)
            .background(zemin)
            .hoverable(interactionSource)
            .toggleable(
                value = isaretli,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onValueChange = onDegisti,
            )
            .heightIn(min = 48.dp)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isaretli,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = SozlesmeMetni,
                uncheckedColor = SozlesmeMetni,
                checkmarkColor = SozlesmeZemini,
            ),
            modifier = Modifier.padding(12.dp),
        )
        Text(
            text = "Okudum ve kabul ettim.",
            style = MaterialTheme.typography.bodyLarge,
            color = SozlesmeMetni,
        )
    }
}

/**
 * Siyah dolgulu birincil eylem. Devre dışıyken %50 soluk, tıklanamaz ve
 * klavyeyle odaklanamaz; ekran okuyucu "devre dışı" olarak duyurur.
 */
@Composable
private fun DevamButonu(etkin: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val zemin by animateColorAsState(
        targetValue = when {
            etkilesim.basili -> SozlesmeButonBasili
            etkilesim.hoverlu -> SozlesmeButonHover
            else -> SozlesmeMetni
        },
        animationSpec = tween(MIKRO_SURE),
        label = "devamZemini",
    )

    Box(
        modifier = modifier
            .alpha(if (etkin) 1f else Opaklik.YUZDE50)
            .scale(etkilesim.olcek)
            .klavyeOdakHalkasi(odakli, ButonSekli, renk = SozlesmeMetni)
            .clip(ButonSekli)
            .background(zemin)
            .etkilesimli(interactionSource, etkin, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Devam et",
            style = MaterialTheme.typography.labelLarge,
            color = SozlesmeZemini,
        )
    }
}
