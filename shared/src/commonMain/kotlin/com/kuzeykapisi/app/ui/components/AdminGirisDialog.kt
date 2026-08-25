package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.vm.AdminViewModel

@Composable
fun AdminGirisDialog(
    vm: AdminViewModel,
    onDismiss: () -> Unit,
    onBasarili: () -> Unit,
) {
    val ui by vm.state.collectAsState()
    var kullaniciAdi by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }

    LaunchedEffect(ui.token) {
        if (ui.token != null) onBasarili()
    }

    KuzeyDialogKabugu(onDismiss = onDismiss, dismissOnClickOutside = false) {
        DialogBasligi("Yönetici Girişi")
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KuzeyMetinAlani(
                deger = kullaniciAdi,
                onDegisti = { kullaniciAdi = it; vm.hataTemizle() },
                etiket = "Kullanıcı adı",
                tekSatir = true,
                modifier = Modifier.fillMaxWidth(),
            )
            KuzeyMetinAlani(
                deger = sifre,
                onDegisti = { sifre = it; vm.hataTemizle() },
                etiket = "Şifre",
                tekSatir = true,
                gorselDonusum = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            val hata = ui.hata
            if (hata != null) HataMetni(hata)
        }
        DialogEylemleri {
            SessizButon(metin = "Vazgeç", onClick = onDismiss)
            BirincilButon(
                metin = if (ui.yukleniyor) "Giriş yapılıyor…" else "Giriş Yap",
                onClick = { vm.girisYap(kullaniciAdi, sifre) },
                etkin = !ui.yukleniyor,
            )
        }
    }
}
