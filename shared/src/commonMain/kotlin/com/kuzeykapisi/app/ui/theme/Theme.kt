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
 * KOYU ŞEMA — uygulamanın varsayılan görünümü. "Gece denizi".
 *
 * Rol dağılımı (brief'e bire bir):
 *  - primary   → [FenerAlevi]: BİRİCİL vurgu. Hover/aktif durum, birincil
 *    buton dolgusu, odak halkası, glow. Geniş zeminde KULLANILMAZ.
 *  - onPrimary → [KaranlikLacivert]: ambar dolgusu her zaman KOYU metin alır,
 *    asla açık-üstü-açık olmaz (7.87:1).
 *  - secondary → [Yosun]: dar rol, "doğa" kategorisi rozet/nokta vurgusu.
 *  - error     → [Kehribar]: form/doğrulama hatası. SinopKirmizisi DEĞİLDİR —
 *    o, admin silme + tescil mührüyle dar role kilitlidir (token-by-intent).
 *  - surface   → [DerinDeniz]: kart/panel zemini.
 */
private val KoyuSema = darkColorScheme(
    primary = FenerAlevi,
    onPrimary = KaranlikLacivert,
    primaryContainer = FenerGece,
    onPrimaryContainer = FenerAlevi,
    inversePrimary = FenerAleviDerin,

    secondary = Yosun,
    onSecondary = TasBeyazi,
    secondaryContainer = NotrGeceAlcak,
    onSecondaryContainer = YosunAcik,

    tertiary = YosunAcik,
    onTertiary = KaranlikLacivert,
    tertiaryContainer = NotrGeceAlcak,
    onTertiaryContainer = YosunAcik,

    background = KaranlikLacivert,
    onBackground = TasBeyazi,
    surface = DerinDeniz,
    onSurface = TasBeyazi,
    surfaceVariant = NotrGeceAlcak,
    onSurfaceVariant = SisGrisi,
    surfaceTint = FenerAlevi,

    surfaceContainerLowest = KaranlikLacivert,
    surfaceContainerLow = NotrGeceAlcak,
    surfaceContainer = DerinDeniz,
    surfaceContainerHigh = NotrGeceYuksek,
    surfaceContainerHighest = NotrGeceEnYuksek,

    outline = NotrGeceCizgiGuclu,
    outlineVariant = NotrGeceCizgi,

    error = Kehribar,
    onError = KaranlikLacivert,
    errorContainer = NotrGeceAlcak,
    onErrorContainer = Kehribar,

    scrim = KaranlikLacivert,
    inverseSurface = TasBeyazi,
    inverseOnSurface = KaranlikLacivert,
)

/**
 * AÇIK ŞEMA — aynı marka, gündüz karşılığı. FenerAlevi metin/ikon olarak
 * kullanıldığında derinleşir ([FenerAleviDerin]), zeminler kâğıt/taş tarafına
 * geçer.
 *
 * NOT: Ekranların bir kısmı renkleri hâlâ doğrudan [KaranlikLacivert] /
 * [TasBeyazi] gibi koyu-tema sabitleriyle çağırıyor (bkz. Color.kt "eski
 * adlar" köprüsü). Bu şema hazır ve [KuzeyKapisiTheme]'e `karanlik = false`
 * verilerek açılır; ekranlar bu sabitlerden `MaterialTheme.colorScheme`
 * rollerine taşındığında açık tema tam devreye girer. Varsayılan bu yüzden
 * koyudur.
 */
private val AcikSema = lightColorScheme(
    primary = FenerAleviDerin,
    onPrimary = TasBeyazi,
    primaryContainer = NotrGunYuksek,
    onPrimaryContainer = FenerAleviDerin,
    inversePrimary = FenerAlevi,

    secondary = Yosun,
    onSecondary = TasBeyazi,
    secondaryContainer = NotrGunYuksek,
    onSecondaryContainer = Yosun,

    tertiary = Yosun,
    onTertiary = TasBeyazi,
    tertiaryContainer = NotrGunYuksek,
    onTertiaryContainer = Yosun,

    background = NotrGun,
    onBackground = NotrGunMetin,
    surface = NotrGunYuzey,
    onSurface = NotrGunMetin,
    surfaceVariant = NotrGunAlcak,
    onSurfaceVariant = NotrGunMetinIkincil,
    surfaceTint = FenerAleviDerin,

    surfaceContainerLowest = NotrGunYuzey,
    surfaceContainerLow = NotrGunAlcak,
    surfaceContainer = NotrGunYuksek,
    surfaceContainerHigh = NotrGunEnYuksek,
    surfaceContainerHighest = NotrGunEnYuksek,

    outline = NotrGunCizgiGuclu,
    outlineVariant = NotrGunCizgi,

    error = KehribarDerin,
    onError = TasBeyazi,
    errorContainer = NotrGunYuksek,
    onErrorContainer = KehribarDerin,

    scrim = NotrGunMetin,
    inverseSurface = NotrGunMetin,
    inverseOnSurface = NotrGun,
)

/**
 * Material3 şekil ölçeği. Küçük/orta bileşenlerde simetrik yumuşak köşe
 * (16dp); kartlarda (large) ve dialoglarda (extraLarge) imza asimetrik "elle
 * kesilmiş taş" geometrisi — bkz. Sekiller.kt → [KartSekli], [DialogSekli].
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
