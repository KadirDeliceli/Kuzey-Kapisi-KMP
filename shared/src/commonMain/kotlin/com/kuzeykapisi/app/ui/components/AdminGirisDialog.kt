package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.FenerAleviDerin
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.YuksekYuzey
import com.kuzeykapisi.app.ui.vm.AdminViewModel

/** Giriş kartının köşe yarıçapı — projedeki genel [DialogSekli]'den (20dp) bilinçli olarak daha büyük; bu ekranın kendine ait, daha prestijli bir imzası var. */
private val GIRIS_KART_SEKLI = RoundedCornerShape(24.dp)

/**
 * Yönetici giriş ekranı — projedeki genel [KuzeyDialogKabugu]'nu KASITLI OLARAK
 * kullanmaz. Bu, uygulamanın tek "portal" anı: cam/buzlu bir kart, arkasında
 * yumuşak bir marka halesi, ortalanmış ve diğer tüm onay/bilgi dialoglarından
 * görsel olarak ayrışan, daha prestijli bir kompozisyon.
 */
@Composable
fun AdminGirisDialog(
    vm: AdminViewModel,
    onDismiss: () -> Unit,
    onBasarili: () -> Unit,
) {
    val ui by vm.state.collectAsState()
    var kullaniciAdi by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }

    LaunchedEffect(ui.oturumAcik) {
        if (ui.oturumAcik) onBasarili()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Kartın arkasında, markanın rengiyle yumuşak bir hale — "portal"
            // hissini güçlendiren, bulanık ve düşük yoğunluklu bir ışıma.
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .blur(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FenerAlevi.copy(alpha = 0.28f),
                                FenerAlevi.copy(alpha = 0f),
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .shadow(
                        elevation = 32.dp,
                        shape = GIRIS_KART_SEKLI,
                        ambientColor = KaranlikLacivert,
                        spotColor = FenerAlevi.copy(alpha = 0.5f),
                    )
                    .clip(GIRIS_KART_SEKLI)
                    // Cam/buzlu yüzey: yarı saydam bir dikey degrade — arkadaki
                    // bulanık marka halesi kenarlarda hafifçe sızar.
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                YuksekYuzey.copy(alpha = 0.90f),
                                YuksekYuzey.copy(alpha = 0.97f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                FenerAlevi.copy(alpha = 0.55f),
                                SisGrisi.copy(alpha = 0.10f),
                                FenerAleviDerin.copy(alpha = 0.30f),
                            ),
                        ),
                        shape = GIRIS_KART_SEKLI,
                    )
                    .padding(horizontal = 32.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                KilitRozeti()
                Text(
                    text = "YÖNETİM PANELİ",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.4.sp),
                    color = FenerAlevi,
                    modifier = Modifier.padding(top = 18.dp),
                )
                Text(
                    text = "Yönetici Girişi",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TasBeyazi,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = "Devam etmek için kimlik bilgilerinizi girin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SisGrisi,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    KuzeyMetinAlani(
                        deger = kullaniciAdi,
                        onDegisti = { kullaniciAdi = it; vm.hataTemizle() },
                        etiket = "Kullanıcı adı",
                        tekSatir = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    KuzeyMetinAlani(
                        deger = sifre,
                        onDegisti = { sifre = it; vm.hataTemizle() },
                        etiket = "Şifre",
                        tekSatir = true,
                        gorselDonusum = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val hata = ui.hata
                    if (hata != null) HataMetni(hata)
                }

                BirincilButon(
                    metin = if (ui.yukleniyor) "Giriş yapılıyor…" else "Giriş yap",
                    onClick = { vm.girisYap(kullaniciAdi, sifre) },
                    etkin = !ui.yukleniyor,
                    hale = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                )
                SessizButon(
                    metin = "Vazgeç",
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/**
 * Giriş kartının tepesindeki kilit rozeti — projenin geri kalanındaki elle
 * çizilmiş ikon dilinde (bkz. YonetimIkonlari.kt), harici bir ikon paketi
 * olmadan. Yalnızca bu ekrana özgü, "güvenli alan" hissini imzalar.
 */
@Composable
private fun KilitRozeti() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(FenerAlevi.copy(alpha = 0.14f))
            .border(1.dp, FenerAlevi.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val kalinlik = w * 0.11f

            // Kilit kavisi (şekil): üstte yarım daire yay.
            drawArc(
                color = FenerAlevi,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.22f, h * 0.04f),
                size = Size(w * 0.56f, h * 0.56f),
                style = Stroke(width = kalinlik, cap = StrokeCap.Round),
            )
            // Kilit gövdesi: yuvarlatılmış dikdörtgen.
            drawRoundRect(
                color = FenerAlevi,
                topLeft = Offset(w * 0.16f, h * 0.42f),
                size = Size(w * 0.68f, h * 0.50f),
                cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
            )
            // Anahtar deliği: küçük daire + gövdeye inen ince çizgi.
            drawCircle(
                color = KaranlikLacivert,
                radius = w * 0.06f,
                center = Offset(w * 0.5f, h * 0.63f),
            )
            drawLine(
                color = KaranlikLacivert,
                start = Offset(w * 0.5f, h * 0.63f),
                end = Offset(w * 0.5f, h * 0.80f),
                strokeWidth = w * 0.07f,
                cap = StrokeCap.Round,
            )
        }
    }
}
