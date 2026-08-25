package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable

const val BIZ_KIMIZ_METNI = "Kuzey Kapısı, Kuzey Anadolu Kalkınma Ajansı (KUZKA) Sinop " +
    "Yatırım Destek Ofisi bünyesinde yürütülen bölgesel bir turizm ve yapay zeka " +
    "rehberlik projesidir. Amacımız; Sinop'un tarihini, kültürel mirasını, yöresel " +
    "mutfağını ve doğal güzelliklerini çağdaş bir dijital deneyimle ziyaretçilere " +
    "ulaştırmaktır."

const val PROJE_HAKKINDA_METNI = "Kuzey Kapısı, Sinop'u dört başlık altında keşfe açar: " +
    "tarihî şahsiyetler, kültürel mekânlar, yöresel lezzetler ve doğal " +
    "güzellikler. Her başlık, o konuya özel bir yapay zeka rehberiyle sohbet etme " +
    "imkânı sunar."

const val ACILIS_BILGILENDIRME_METNI = "Buradaki karakterler kurgusaldır ve yapay zeka " +
    "tarafından üretilmektedir. Yanlış veya eksik bilgi verebilirler. Verdikleri " +
    "cevaplar kurumumuzun resmi görüşünü yansıtmaz."

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
