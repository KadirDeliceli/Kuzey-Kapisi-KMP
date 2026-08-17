package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val GENIS_EKRAN_ESIGI = 600.dp

private const val ILETISIM_METNI =
    "Sinop, Gerze · Telefon: +90 530 000 00 00 · E-posta: mail@gmail.com"

@Composable
fun Footer(modifier: Modifier = Modifier, yil: Int = 2026) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        )
        BoxWithConstraints {
            val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
            val telifMetni = "© $yil KUZKA Sinop YDO"
            if (genisEkran) {
                // Geniş ekran: solda iletişim, sağda telif — uçlara yaslı.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
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
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = hizalama,
    )
}
