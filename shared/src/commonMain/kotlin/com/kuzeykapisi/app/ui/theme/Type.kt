package com.kuzeykapisi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.fraunces_italic
import kuzeykapisiapp.shared.generated.resources.fraunces_medium
import kuzeykapisiapp.shared.generated.resources.fraunces_regular
import kuzeykapisiapp.shared.generated.resources.fraunces_semibold
import kuzeykapisiapp.shared.generated.resources.jetbrains_mono_medium
import kuzeykapisiapp.shared.generated.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

/**
 * Gövde yazı tipi — Hanken Grotesk yerine platformun gruesk/sans varsayılanı.
 * (Bilinçli olarak DEĞİŞTİRİLMEDİ; yeniden tasarımda yalnızca başlık ve veri
 * yazı tipleri yenilendi.)
 */
val GovdeFontu = FontFamily.Default

/**
 * Başlık ailesi: Fraunces (composeResources/font altında bundle edilmiş statik
 * TTF'ler — Regular/Medium/SemiBold/Italic). Dosyalar bulunamazsa Compose
 * kaynak sistemi derleme zamanında hata verir; o durumda [FontFamily.Serif]'e
 * düşmek yeterlidir (görsel olarak yakın sonuç verir).
 */
@Composable
private fun baslikAilesi(): FontFamily {
    val duz = Font(Res.font.fraunces_regular, FontWeight.Normal, FontStyle.Normal)
    val orta = Font(Res.font.fraunces_medium, FontWeight.Medium, FontStyle.Normal)
    val yari = Font(Res.font.fraunces_semibold, FontWeight.SemiBold, FontStyle.Normal)
    val italik = Font(Res.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic)
    return remember(duz, orta, yari, italik) { FontFamily(duz, orta, yari, italik) }
}

/** Veri ailesi: JetBrains Mono — YALNIZCA sayısal/süre etiketlerinde. */
@Composable
private fun veriAilesi(): FontFamily {
    val duz = Font(Res.font.jetbrains_mono_regular, FontWeight.Normal, FontStyle.Normal)
    val orta = Font(Res.font.jetbrains_mono_medium, FontWeight.Medium, FontStyle.Normal)
    return remember(duz, orta) { FontFamily(duz, orta) }
}

/**
 * "Veri" niteliğindeki küçük metinlerin stili (ör. "15 dk yol", "90 dk toplam").
 * Yalnızca süre/mesafe/sayı etiketlerinde kullanılır — düz metinde ASLA.
 * [KuzeyKapisiTheme] tarafından sağlanır.
 */
val LocalVeriStili = staticCompositionLocalOf {
    TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp,
    )
}

@Composable
internal fun veriStili(): TextStyle {
    val aile = veriAilesi()
    return remember(aile) {
        TextStyle(
            fontFamily = aile,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.2.sp,
        )
    }
}

/**
 * Tip ölçeği: başlıklar bir kademe daha cesur/büyük, harf aralığı negatif
 * (sıkı); gövde normal aralıkta ve ferah satır yüksekliğinde.
 */
@Composable
internal fun kuzeyTipografi(): Typography {
    val baslik = baslikAilesi()
    return remember(baslik) {
        Typography(
            displayLarge = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 52.sp,
                lineHeight = 56.sp,
                letterSpacing = (-1.4).sp,
            ),
            displayMedium = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 44.sp,
                lineHeight = 48.sp,
                letterSpacing = (-1.2).sp,
            ),
            headlineLarge = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 38.sp,
                lineHeight = 44.sp,
                letterSpacing = (-1.0).sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 31.sp,
                lineHeight = 37.sp,
                letterSpacing = (-0.7).sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.5).sp,
            ),
            titleLarge = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.4).sp,
            ),
            titleMedium = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.Medium,
                fontSize = 19.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.2).sp,
            ),
            titleSmall = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                letterSpacing = (-0.1).sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 0.3.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.3.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.4.sp,
            ),
        )
    }
}
