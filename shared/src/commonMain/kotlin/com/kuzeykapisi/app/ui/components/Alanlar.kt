package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuzeykapisi.app.ui.theme.AlanSekli
import com.kuzeykapisi.app.ui.theme.AlcakYuzey
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.SinopKirmizisi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

/**
 * Tüm form alanlarının ortak renk şeması: koyu, kendi zeminine hafifçe gömülü
 * bir kutu; odaklanınca kenarı [FenerAlevi]'ne döner (aktif durum göstergesi).
 */
@Composable
fun kuzeyAlanRenkleri(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TasBeyazi,
    unfocusedTextColor = TasBeyazi,
    disabledTextColor = SisGrisi,
    focusedContainerColor = AlcakYuzey,
    unfocusedContainerColor = AlcakYuzey,
    disabledContainerColor = AlcakYuzey.copy(alpha = 0.5f),
    cursorColor = FenerAlevi,
    focusedBorderColor = FenerAlevi,
    unfocusedBorderColor = SisGrisi.copy(alpha = 0.28f),
    disabledBorderColor = SisGrisi.copy(alpha = 0.15f),
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = FenerAlevi,
    unfocusedLabelColor = SisGrisi,
    disabledLabelColor = SisGrisi.copy(alpha = 0.5f),
    focusedPlaceholderColor = SisGrisi,
    unfocusedPlaceholderColor = SisGrisi,
    errorLabelColor = MaterialTheme.colorScheme.error,
    errorCursorColor = MaterialTheme.colorScheme.error,
)

/**
 * Projedeki TEK metin alanı bileşeni — tam yuvarlak köşeli [AlanSekli] formu
 * ve ortak renkleriyle. Admin formları, sohbet girdisi ve giriş dialogu bunu
 * kullanır; böylece tek noktadan güncellenir.
 */
@Composable
fun KuzeyMetinAlani(
    deger: String,
    onDegisti: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiket: String? = null,
    ipucu: String? = null,
    yardimMetni: String? = null,
    hataMetni: String? = null,
    tekSatir: Boolean = false,
    enAzSatir: Int = 1,
    etkin: Boolean = true,
    gorselDonusum: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = deger,
        onValueChange = onDegisti,
        modifier = modifier,
        enabled = etkin,
        label = etiket?.let { { Text(it, style = MaterialTheme.typography.labelLarge) } },
        placeholder = ipucu?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
        singleLine = tekSatir,
        minLines = enAzSatir,
        isError = hataMetni != null,
        visualTransformation = gorselDonusum,
        shape = AlanSekli,
        colors = kuzeyAlanRenkleri(),
        textStyle = MaterialTheme.typography.bodyLarge,
        supportingText = when {
            hataMetni != null -> {
                { Text(hataMetni, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
            yardimMetni != null -> {
                { Text(yardimMetni, color = SisGrisi, style = MaterialTheme.typography.bodySmall) }
            }
            else -> null
        },
    )
}

/** Form bölümlerinin üstündeki küçük, harf aralığı açık başlık. */
@Composable
fun AlanBasligi(metin: String, modifier: Modifier = Modifier) {
    Text(
        text = metin.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
        color = SisGrisi,
        modifier = modifier,
    )
}

/** Düzenlenemez ama gösterilmesi gereken alan (kategori, kod gibi). */
@Composable
fun SaltOkunurAlan(etiket: String, deger: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        AlanBasligi(etiket)
        Box(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = deger,
                style = MaterialTheme.typography.bodyLarge,
                color = TasBeyazi,
            )
        }
    }
}

/**
 * "Anlatımı kaldır" seçimi — işaretliyken YIKICI bir eylemdir (mevcut anlatım
 * geri alınamaz şekilde silinir), bu yüzden işaretliyken kutu ve etiket
 * [SinopKirmizisi]'ne döner. Bu rengin izin verilen dar kullanımlarından biri.
 */
@Composable
fun AnlatimiKaldirSecimi(
    isaretli: Boolean,
    onDegisti: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 6.dp),
    ) {
        Checkbox(
            checked = isaretli,
            onCheckedChange = onDegisti,
            colors = CheckboxDefaults.colors(
                checkedColor = SinopKirmizisi,
                checkmarkColor = KaranlikLacivert,
                uncheckedColor = SisGrisi,
            ),
        )
        Text(
            text = "Anlatımı kaldır",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isaretli) SinopKirmizisi else TasBeyazi,
        )
    }
}
