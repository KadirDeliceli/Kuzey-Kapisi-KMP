package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    onBizKimizClick: () -> Unit,
    onProjeHakkindaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Uygulama edge-to-edge çalışıyor (MainActivity'de enableEdgeToEdge
            // + targetSdk 36 ile zorunlu), bu yüzden durum çubuğu inset'i elle
            // uygulanır. Web'de bu inset sıfır olduğu için fazladan boşluk
            // oluşmaz — platform dallanmasına gerek yok.
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "KUZEY KAPISI",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Row {
            TextButton(onClick = onBizKimizClick) {
                Text("Biz Kimiz", color = MaterialTheme.colorScheme.secondary)
            }
            TextButton(onClick = onProjeHakkindaClick) {
                Text("Proje Hakkında", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
