package com.kuzeykapisi.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

/**
 * KOYU ŞEMA — uygulamanın varsayılan görünümü.
 *
 * Rol dağılımı:
 *  - primary   → [Turkuaz]: birincil buton dolgusu, odaklanmış alan kenarı,
 *    seçili chip, ilerleme göstergesi. Geniş zemin olarak KULLANILMAZ.
 *  - secondary → [Kum]: sıcak, tamamlayıcı ikincil vurgu (rozet, ikincil buton).
 *  - tertiary  → [KumAcik]: koyu zeminde okunan, sakin bilgi işareti tonu.
 *  - surface   → [NotrGeceYuzey]: kart/panel zemini; renk yükü vurgulardadır.
 *  - error     → [Kehribar]: hata/uyarı. Kırmızı DEĞİL; [SinopKirmizisi]
 *    yalnızca yıkıcı admin eylemlerine ayrılmıştır.
 */
private val KoyuSema = darkColorScheme(
    primary = Turkuaz,
    onPrimary = NotrGece,
    primaryContainer = TurkuazGece,
    onPrimaryContainer = Turkuaz,
    inversePrimary = TurkuazDerin,

    secondary = Kum,
    onSecondary = NotrGeceMetin,
    secondaryContainer = KumGece,
    onSecondaryContainer = KumAcik,

    tertiary = KumAcik,
    onTertiary = NotrGece,
    tertiaryContainer = KumGece,
    onTertiaryContainer = KumAcik,

    background = NotrGece,
    onBackground = NotrGeceMetin,
    surface = NotrGeceYuzey,
    onSurface = NotrGeceMetin,
    surfaceVariant = NotrGeceAlcak,
    onSurfaceVariant = NotrGeceMetinIkincil,
    surfaceTint = Turkuaz,

    surfaceContainerLowest = NotrGece,
    surfaceContainerLow = NotrGeceAlcak,
    surfaceContainer = NotrGeceYuzey,
    surfaceContainerHigh = NotrGeceYuksek,
    surfaceContainerHighest = NotrGeceEnYuksek,

    outline = NotrGeceCizgi,
    outlineVariant = NotrGeceCizgi.copy(alpha = 0.45f),

    error = Kehribar,
    onError = NotrGece,
    errorContainer = NotrGeceAlcak,
    onErrorContainer = Kehribar,

    scrim = NotrGece,
    inverseSurface = NotrGeceMetin,
    inverseOnSurface = NotrGece,
)

/**
 * AÇIK ŞEMA — aynı marka, gündüz karşılığı. Turkuaz koyulaşır ([TurkuazDerin]),
 * kum derinleşir, nötrler kâğıt tarafına geçer.
 *
 * NOT: Ekranların bir kısmı renkleri hâlâ doğrudan koyu tema sabitleriyle
 * (`TasBeyazi`, `DerinDeniz` gibi) çağırıyor. Bu şema hazırdır ve
 * [KuzeyKapisiTheme]'e `karanlik = false` verilerek açılır; ekranlar bu
 * sabitlerden `MaterialTheme.colorScheme` rollerine taşındığında açık tema
 * tam olarak devreye girer. Varsayılan bu yüzden koyudur.
 */
private val AcikSema = lightColorScheme(
    primary = TurkuazDerin,
    onPrimary = NotrGunYuzey,
    primaryContainer = TurkuazSis,
    onPrimaryContainer = TurkuazGece,
    inversePrimary = Turkuaz,

    secondary = Kum,
    onSecondary = NotrGunYuzey,
    secondaryContainer = KumSis,
    onSecondaryContainer = KumGece,

    tertiary = Kum,
    onTertiary = NotrGunYuzey,
    tertiaryContainer = KumSis,
    onTertiaryContainer = KumGece,

    background = NotrGun,
    onBackground = NotrGunMetin,
    surface = NotrGunYuzey,
    onSurface = NotrGunMetin,
    surfaceVariant = NotrGunAlcak,
    onSurfaceVariant = NotrGunMetinIkincil,
    surfaceTint = TurkuazDerin,

    surfaceContainerLowest = NotrGunYuzey,
    surfaceContainerLow = NotrGunAlcak,
    surfaceContainer = NotrGunYuksek,
    surfaceContainerHigh = NotrGunEnYuksek,
    surfaceContainerHighest = NotrGunEnYuksek,

    outline = NotrGunCizgi,
    outlineVariant = NotrGunCizgi.copy(alpha = 0.55f),

    error = KehribarDerin,
    onError = NotrGunYuzey,
    errorContainer = KumSis,
    onErrorContainer = KehribarDerin,

    scrim = NotrGunMetin,
    inverseSurface = NotrGunMetin,
    inverseOnSurface = NotrGun,
)

/**
 * Material3 şekil ölçeği — tam simetrik, yumuşak köşeler (bkz. Sekiller.kt).
 * Elle çağrılan bileşenlerin (kart, buton, dialog) yanı sıra tema üzerinden
 * gelen varsayılan bileşenlerde de (Card, Button, TextField, Menu) aynı
 * yumuşak köşe dili geçerli olur.
 */
private val KuzeySekilleri = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = AlanSekli,
    medium = SatirSekli,
    large = KartSekli,
    extraLarge = DialogSekli,
)

/**
 * Uygulama teması.
 *
 * @param karanlik `true` (varsayılan) koyu şema, `false` açık şema. Sistem
 *   tercihine bağlamak için çağrı yerinde `isSystemInDarkTheme()` verilebilir —
 *   yukarıdaki [AcikSema] notunu okumadan varsayılanı değiştirme.
 */
@Composable
fun KuzeyKapisiTheme(
    karanlik: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalVeriStili provides veriStili()) {
        MaterialTheme(
            colorScheme = if (karanlik) KoyuSema else AcikSema,
            typography = kuzeyTipografi(),
            shapes = KuzeySekilleri,
            content = content,
        )
    }
}
