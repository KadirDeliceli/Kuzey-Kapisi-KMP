package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TELEFON_KIRILIMI

private val GENIS_EKRAN_ESIGI = TELEFON_KIRILIMI

private const val ILETISIM_METNI =
    "0 (366) 212 58 52 · bilgi@kuzka.gov.tr"

@Composable
fun Footer(modifier: Modifier = Modifier, yil: Int = 2026) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Üst çubuktaki ışık hattının eşi — uçlara doğru sönen ince ayraç.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SisGrisi.copy(alpha = Opaklik.YUZDE30),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        BoxWithConstraints {
            val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
            val telifMetni = "© $yil KUZKA"
            if (genisEkran) {
                // Geniş ekran: solda iletişim, sağda telif — uçlara yaslı.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FooterMetni(ILETISIM_METNI)
                    FooterMetni(telifMetni)
                }
            } else {
                // Dar ekran: alt alta ve ortalanmış.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FooterMetni(ILETISIM_METNI, TextAlign.Center)
                    FooterMetni(telifMetni, TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun FooterMetni(metin: String, hizalama: TextAlign? = null) {
    Text(
        text = metin,
        style = MaterialTheme.typography.bodySmall,
        color = SisGrisi,
        textAlign = hizalama,
    )
}
