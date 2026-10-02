package com.kuzeykapisi.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TELEFON_KIRILIMI
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.klavyeOdakHalkasi
import kotlinx.coroutines.delay
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.kuzey_kapisi_logo
import org.jetbrains.compose.resources.painterResource

private val GENIS_EKRAN_ESIGI = TELEFON_KIRILIMI

/** Dar ekranda hamburger'dan açılan yan panelin sabit genişliği. */
private val PANEL_GENISLIGI = 300.dp

/** Panel açılış/kapanış geçiş süresi — App.kt'deki sohbet paneliyle aynı dil (240ms). */
private const val PANEL_GECIS_SURESI = 240

@Composable
fun TopBar(
    onBizKimizClick: () -> Unit,
    onProjeHakkindaClick: () -> Unit,
    onAdminIkonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var panelAcik by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI

        // Pencere daraltılıp genişletildiğinde (platform değil, GENİŞLİK
        // bazlı geçiş) hamburger kalkar — panel de tutarlılık için kapanır.
        LaunchedEffect(genisEkran) {
            if (genisEkran) panelAcik = false
        }

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
                if (genisEkran) {
                    // GENİŞ EKRAN: linkler + admin ikonu doğrudan, açık görünür.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        UstBarBaglantisi(metin = "Biz Kimiz", onClick = onBizKimizClick)
                        UstBarBaglantisi(metin = "Proje Hakkında", onClick = onProjeHakkindaClick)
                        Spacer(modifier = Modifier.width(4.dp))
                        AdminGirisIkonu(onClick = onAdminIkonClick)
                    }
                } else {
                    // DAR EKRAN: yalnızca marka + hamburger; linkler/admin ikonu
                    // yan panele taşınır.
                    HamburgerDugmesi(onClick = { panelAcik = true })
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

    if (panelAcik) {
        UstBarPaneli(
            onKapat = { panelAcik = false },
            onBizKimizClick = onBizKimizClick,
            onProjeHakkindaClick = onProjeHakkindaClick,
            onAdminIkonClick = onAdminIkonClick,
        )
    }
}

@Composable
private fun UstBarBaglantisi(metin: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "ustBarRengi",
    )
    Box(
        modifier = Modifier
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .etkilesimli(interactionSource, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "adminIkonRengi",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(34.dp)
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .background(renk.copy(alpha = 0.10f))
            .border(1.dp, renk.copy(alpha = 0.35f), CircleShape)
            .etkilesimli(interactionSource, contentDescription = "Yönetim paneli girişi", onClick = onClick),
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

/** Dar ekranda TopBar'daki tek eylem — tıklanınca sağdan [UstBarPaneli] açılır. */
@Composable
private fun HamburgerDugmesi(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "hamburgerRengi",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(40.dp)
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .etkilesimli(interactionSource, contentDescription = "Menü", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val w = size.width
            val h = size.height
            val kalinlik = h * 0.09f
            listOf(0.22f, 0.5f, 0.78f).forEach { oran ->
                drawLine(
                    color = renk,
                    start = Offset(0f, h * oran),
                    end = Offset(w, h * oran),
                    strokeWidth = kalinlik,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

/**
 * Dar ekranda hamburger'dan açılan sağdan-kayan yan panel — TopBar'ın DAR
 * ekranda kalkan üç eylemini (Biz Kimiz / Proje Hakkında / Yönetim Paneli)
 * barındırır. Her öğe MEVCUT işlevi tetikler (yeni dialog/akış YOKTUR); bir
 * öğeye tıklanınca panel önce kapanır, geçiş bitince ilgili eylem açılır.
 *
 * Tam ekran kaplayan bir [Dialog] içinde çizilir — TopBar'ın kendi ölçüsü
 * (yalnızca üst çubuk şeridi) sınırlı olduğundan, panelin arkasındaki karartma
 * ve panelin kendisi UYGULAMA PENCERESİNİN TAMAMINI kaplamalıdır; bunu
 * TopBar'ın yerel layout sınırları içinden yapmanın güvenli yolu budur
 * (projede zaten [KuzeyDialogKabugu] / [AdminGirisDialog] aynı mekanizmayı
 * kullanıyor).
 */
@Composable
private fun UstBarPaneli(
    onKapat: () -> Unit,
    onBizKimizClick: () -> Unit,
    onProjeHakkindaClick: () -> Unit,
    onAdminIkonClick: () -> Unit,
) {
    var gorunur by remember { mutableStateOf(false) }
    var kapaniyor by remember { mutableStateOf(false) }
    var kapandiktaTetikle by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) { gorunur = true }

    LaunchedEffect(kapaniyor) {
        if (kapaniyor) {
            gorunur = false
            delay(PANEL_GECIS_SURESI.toLong())
            kapandiktaTetikle?.invoke()
            onKapat()
        }
    }

    fun kapat(sonraTetiklenecek: (() -> Unit)? = null) {
        kapandiktaTetikle = sonraTetiklenecek
        kapaniyor = true
    }

    Dialog(
        onDismissRequest = { kapat() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val scrimInteraction = remember { MutableInteractionSource() }
            AnimatedVisibility(
                visible = gorunur,
                enter = fadeIn(animationSpec = tween(PANEL_GECIS_SURESI)),
                exit = fadeOut(animationSpec = tween(PANEL_GECIS_SURESI)),
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KaranlikLacivert.copy(alpha = 0.62f))
                        .clickable(
                            interactionSource = scrimInteraction,
                            indication = null,
                            onClick = { kapat() },
                        ),
                )
            }

            AnimatedVisibility(
                visible = gorunur,
                enter = slideInHorizontally(animationSpec = tween(PANEL_GECIS_SURESI)) { it },
                exit = slideOutHorizontally(animationSpec = tween(PANEL_GECIS_SURESI)) { it },
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            ) {
                UstBarPaneliIcerik(
                    onKapatTiklandi = { kapat() },
                    onBizKimizTiklandi = { kapat(onBizKimizClick) },
                    onProjeHakkindaTiklandi = { kapat(onProjeHakkindaClick) },
                    onAdminTiklandi = { kapat(onAdminIkonClick) },
                )
            }
        }
    }
}

@Composable
private fun UstBarPaneliIcerik(
    onKapatTiklandi: () -> Unit,
    onBizKimizTiklandi: () -> Unit,
    onProjeHakkindaTiklandi: () -> Unit,
    onAdminTiklandi: () -> Unit,
) {
    // Asimetrik köşe: dış (sağ) kenar ekranın kendi kenarıyla çakıştığı için
    // düz kalır; iç (sol) kenar tasarım sisteminin imza oranını taşır — üstte
    // normal (20dp), altta belirgin küçük (4dp).
    val panelSekli = RoundedCornerShape(topStart = 20.dp, bottomStart = 4.dp)

    Column(
        modifier = Modifier
            .width(PANEL_GENISLIGI)
            .fillMaxHeight()
            .clip(panelSekli)
            .background(DerinDeniz)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            KapatIkonu(onClick = onKapatTiklandi)
        }
        Spacer(modifier = Modifier.height(20.dp))
        PanelOgesi(metin = "Biz Kimiz", onClick = onBizKimizTiklandi)
        PanelOgesi(metin = "Proje Hakkında", onClick = onProjeHakkindaTiklandi)
        PanelOgesi(metin = "Yönetim Paneli", onClick = onAdminTiklandi)
    }
}

@Composable
private fun PanelOgesi(metin: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else TasBeyazi,
        animationSpec = tween(MIKRO_SURE),
        label = "panelOgesiRengi",
    )
    val panelOgesiSekli = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .klavyeOdakHalkasi(odakli, panelOgesiSekli)
            .clip(panelOgesiSekli)
            .etkilesimli(interactionSource, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 15.dp),
    ) {
        Text(
            text = metin,
            style = MaterialTheme.typography.titleSmall,
            color = renk,
        )
    }
}

@Composable
private fun KapatIkonu(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)
    val odakli by interactionSource.collectIsFocusedAsState()
    val renk by animateColorAsState(
        targetValue = if (etkilesim.hoverlu || etkilesim.basili) FenerAlevi else SisGrisi,
        animationSpec = tween(MIKRO_SURE),
        label = "kapatIkonuRengi",
    )
    Box(
        modifier = Modifier
            .scale(etkilesim.olcek)
            .size(38.dp)
            .klavyeOdakHalkasi(odakli, CircleShape)
            .clip(CircleShape)
            .etkilesimli(interactionSource, contentDescription = "Kapat", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
            val kalinlik = w * 0.12f
            drawLine(color = renk, start = Offset(0f, 0f), end = Offset(w, h), strokeWidth = kalinlik, cap = StrokeCap.Round)
            drawLine(color = renk, start = Offset(w, 0f), end = Offset(0f, h), strokeWidth = kalinlik, cap = StrokeCap.Round)
        }
    }
}
