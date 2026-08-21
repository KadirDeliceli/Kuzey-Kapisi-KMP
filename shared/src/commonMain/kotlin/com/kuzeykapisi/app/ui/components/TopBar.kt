package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.kuzey_kapisi_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun TopBar(
    onBizKimizClick: () -> Unit,
    onProjeHakkindaClick: () -> Unit,
    onAdminIkonClick: () -> Unit,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.kuzey_kapisi_logo),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KUZEY KAPISI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row {
            TextButton(onClick = onBizKimizClick) {
                Text("Biz Kimiz", color = MaterialTheme.colorScheme.secondary)
            }
            TextButton(onClick = onProjeHakkindaClick) {
                Text("Proje Hakkında", color = MaterialTheme.colorScheme.secondary)
            }
            AdminGirisIkonu(onClick = onAdminIkonClick)
        }
    }
}

/**
 * Küçük, göze batmayan yuvarlak "kalkan" ikonu — admin paneline giriş
 * noktası. Yazı yok, kasıtlı olarak sade.
 */
@Composable
private fun AdminGirisIkonu(onClick: () -> Unit) {
    val renk = MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(renk.copy(alpha = 0.10f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
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
