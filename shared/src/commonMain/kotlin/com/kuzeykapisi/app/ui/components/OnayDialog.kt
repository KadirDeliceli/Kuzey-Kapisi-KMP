package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import com.kuzeykapisi.app.ui.theme.SinopKirmizisi

/**
 * Silme gibi GERİ ALINAMAZ işlemler için onay/vazgeç dialogu.
 *
 * Kabuğun kenarlığı, üst ışık hattı ve onay butonu [SinopKirmizisi]'dir —
 * bu rengin uygulamadaki iki dar rolünden biri (diğeri: tescilli ürün
 * kartlarındaki coğrafi işaret mührü). Uygulamanın başka hiçbir yerinde
 * bu renk bu şekilde görünmez, bu yüzden burada gördüğünde "dur ve düşün"
 * anlamı taşır.
 */
@Composable
fun OnayDialog(
    baslik: String,
    metin: String,
    onOnay: () -> Unit,
    onVazgec: () -> Unit,
    onayMetni: String = "Evet, Sil",
) {
    KuzeyDialogKabugu(
        onDismiss = onVazgec,
        vurguRengi = SinopKirmizisi,
    ) {
        DialogBasligi(baslik)
        DialogMetni(metin)
        DialogEylemleri {
            SessizButon(metin = "Vazgeç", onClick = onVazgec)
            YikiciButon(metin = onayMetni, onClick = onOnay)
        }
    }
}
