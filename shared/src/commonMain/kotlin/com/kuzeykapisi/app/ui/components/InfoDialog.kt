package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable

const val BIZ_KIMIZ_METNI = "Kuzey Kapısı, Sinop'u tanıtmak için geliştirilmiş bağımsız, " +
    "yapay zeka destekli bir turizm uygulamasıdır. Amacımız; Sinop'un tarihini, " +
    "kültürel mirasını, yöresel mutfağını ve doğal güzelliklerini çağdaş bir " +
    "dijital deneyimle ziyaretçilere ulaştırmaktır."

const val PROJE_HAKKINDA_METNI = "Kuzey Kapısı, Sinop'u dört başlık altında keşfe açar: " +
    "tarihî şahsiyetler, kültürel mekânlar, yöresel lezzetler ve doğal " +
    "güzellikler. Her başlık, o konuya özel bir yapay zeka rehberiyle sohbet etme " +
    "imkânı sunar. Akıllı rota planlayıcı ise ayırdığınız süreye ve ilgi " +
    "alanlarınıza göre size özel bir gezi rotası hazırlar."

const val ACILIS_BILGILENDIRME_METNI = "Buradaki karakterler kurgusaldır ve yapay zeka " +
    "tarafından üretilmektedir. Yanlış veya eksik bilgi verebilirler. Verdikleri " +
    "cevaplar resmi bir görüş niteliği taşımaz."

@Composable
fun InfoDialog(
    baslik: String,
    metin: String,
    onDismiss: () -> Unit,
    onaylaMetni: String = "Kapat",
    dismissOnClickOutside: Boolean = true,
) {
    KuzeyDialogKabugu(
        onDismiss = onDismiss,
        dismissOnClickOutside = dismissOnClickOutside,
    ) {
        DialogBasligi(baslik)
        DialogMetni(metin)
        DialogEylemleri {
            BirincilButon(metin = onaylaMetni, onClick = onDismiss)
        }
    }
}
