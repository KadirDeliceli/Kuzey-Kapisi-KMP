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
 * Gövde yazı tipi — platformun grotesk/sans varsayılanı. Başlık ailesinin
 * (Fraunces) karakterine karşı nötr bir zemin kurar; uzun metinde okunurluğu
 * platformun kendi optimize edilmiş sistem fontuna bırakırız.
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
 * TİP ÖLÇEĞİ
 *
 * İki aile, net bir iş bölümü:
 *  - BAŞLIK (Fraunces, serif): display → title. Anlatısal, "kitabî" ton.
 *  - GÖVDE ([GovdeFontu], sans): body → label. Nötr, uzun metinde yorulmayan.
 *
 * Ölçek ~1.2 katsayılı bir modüler diziye oturur; böylece kademeler arasındaki
 * fark her seviyede aynı oranda hissedilir.
 *
 * HARF ARALIĞI EĞRİSİ — modern hiyerarşinin asıl taşıyıcısı:
 *  - Büyük punto → belirgin NEGATİF aralık (kelime tek bir blok gibi okunur),
 *  - Orta punto → nötre yaklaşır,
 *  - Küçük punto ve etiketler → POZİTİF aralık (küçükte okunurluk ve
 *    "eyebrow/etiket" karakteri).
 *
 * SATIR YÜKSEKLİĞİ: başlıklarda sıkı (~1.17), gövdede ferah (~1.6) — başlık
 * bir görsel nesne, gövde ise okunan bir doku olarak davranır.
 */
@Composable
internal fun kuzeyTipografi(): Typography {
    val baslik = baslikAilesi()
    return remember(baslik) {
        Typography(
            // --- Display: yalnızca kahraman (hero) alanları -----------------
            displayLarge = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 56.sp,
                lineHeight = 60.sp,
                letterSpacing = (-1.8).sp,
            ),
            displayMedium = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 45.sp,
                lineHeight = 50.sp,
                letterSpacing = (-1.4).sp,
            ),
            displaySmall = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 37.sp,
                lineHeight = 43.sp,
                letterSpacing = (-1.0).sp,
            ),

            // --- Headline: ekran ve bölüm başlıkları ------------------------
            headlineLarge = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 34.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.8).sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.6).sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.4).sp,
            ),

            // --- Title: kart ve satır başlıkları ----------------------------
            titleLarge = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.SemiBold,
                fontSize = 21.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.3).sp,
            ),
            titleMedium = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.15).sp,
            ),
            titleSmall = TextStyle(
                fontFamily = baslik,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
            ),

            // --- Body: okunan metin, ferah satır aralığı --------------------
            bodyLarge = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                letterSpacing = 0.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.1.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp,
            ),

            // --- Label: buton, chip, üst etiket (eyebrow) -------------------
            labelLarge = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.2.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = GovdeFontu,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                letterSpacing = 0.9.sp,
            ),
        )
    }
}
