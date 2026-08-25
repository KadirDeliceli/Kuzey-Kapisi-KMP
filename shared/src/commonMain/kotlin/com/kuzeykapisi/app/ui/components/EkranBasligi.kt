package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

/**
 * Alt ekranların ortak üst bloğu: geri bağlantısı + (varsa) küçük etiket +
 * başlık. Her ekranın aynı ritmi tutturması için tek yerden gelir.
 */
@Composable
fun EkranBasligi(
    baslik: String,
    geriMetni: String,
    onGeri: () -> Unit,
    modifier: Modifier = Modifier,
    etiket: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        GeriButonu(metin = geriMetni, onClick = onGeri, modifier = Modifier.padding(start = 0.dp))
        if (etiket != null) {
            Text(
                text = etiket.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.2.sp),
                color = FenerAlevi,
                modifier = Modifier.padding(top = 10.dp, start = 6.dp),
            )
        }
        Text(
            text = baslik,
            style = MaterialTheme.typography.headlineMedium,
            color = TasBeyazi,
            modifier = Modifier.padding(top = if (etiket != null) 6.dp else 8.dp, start = 6.dp),
        )
    }
}
