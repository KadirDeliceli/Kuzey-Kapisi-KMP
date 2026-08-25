package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuzeykapisi.app.ui.components.BirincilButon
import com.kuzeykapisi.app.ui.components.NabizGostergesi
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

@Composable
fun WipScreen(onGeri: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Fener nabzı burada "çalışıyor" anlamını taşır — sayfa boş değil,
        // sadece henüz hazır değil.
        NabizGostergesi(boyut = 46.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AKILLI ZAMAN VE ROTA DÜZENLEYİCİ",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.4.sp),
            color = FenerAlevi,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Yapım Aşamasında",
            style = MaterialTheme.typography.headlineLarge,
            color = TasBeyazi,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Bu araç üzerinde çalışıyoruz. Gezinizi güne ve saate göre planlayan " +
                "akıllı rota düzenleyici çok yakında burada olacak.",
            style = MaterialTheme.typography.bodyLarge,
            color = SisGrisi,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 520.dp),
        )
        Spacer(modifier = Modifier.height(28.dp))
        BirincilButon(metin = "Başlıklara dön", onClick = onGeri, hale = true)
    }
}
