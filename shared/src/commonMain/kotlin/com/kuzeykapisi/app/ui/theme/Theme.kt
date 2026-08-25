package com.kuzeykapisi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Koyu "gece denizi" şeması. Material3 rollerinin karşılıkları:
 *  - primary  → [FenerAlevi]: birincil buton dolgusu, odaklanmış alan kenarı,
 *    seçili chip, ilerleme göstergesi. Geniş zemin olarak KULLANILMAZ.
 *  - surface  → [DerinDeniz]: kart/panel zemini.
 *  - error    → [HataRengi] (= FenerAlevi): hata/uyarı metinleri. Kırmızı
 *    DEĞİL; [SinopKirmizisi] yalnızca yıkıcı admin eylemlerine ayrılmıştır.
 */
private val KuzeyColorScheme = darkColorScheme(
    primary = FenerAlevi,
    onPrimary = KaranlikLacivert,
    primaryContainer = YuksekYuzey,
    onPrimaryContainer = TasBeyazi,

    secondary = Yosun,
    onSecondary = TasBeyazi,
    secondaryContainer = Yosun,
    onSecondaryContainer = TasBeyazi,

    tertiary = Yosun,
    onTertiary = TasBeyazi,
    tertiaryContainer = YuksekYuzey,
    onTertiaryContainer = TasBeyazi,

    background = KaranlikLacivert,
    onBackground = TasBeyazi,
    surface = DerinDeniz,
    onSurface = TasBeyazi,
    surfaceVariant = AlcakYuzey,
    onSurfaceVariant = SisGrisi,

    surfaceContainerLowest = KaranlikLacivert,
    surfaceContainerLow = AlcakYuzey,
    surfaceContainer = DerinDeniz,
    surfaceContainerHigh = YuksekYuzey,
    surfaceContainerHighest = YuksekYuzey,

    outline = SisGrisi,
    outlineVariant = SisGrisi.copy(alpha = 0.25f),

    error = HataRengi,
    onError = KaranlikLacivert,
    errorContainer = AlcakYuzey,
    onErrorContainer = HataRengi,

    scrim = KaranlikLacivert,
    inverseSurface = TasBeyazi,
    inverseOnSurface = KaranlikLacivert,
    inversePrimary = DerinDeniz,
)

@Composable
fun KuzeyKapisiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalVeriStili provides veriStili()) {
        MaterialTheme(
            colorScheme = KuzeyColorScheme,
            typography = kuzeyTipografi(),
            content = content,
        )
    }
}
