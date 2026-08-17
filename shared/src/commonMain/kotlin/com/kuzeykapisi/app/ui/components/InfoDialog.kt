package com.kuzeykapisi.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun InfoDialog(baslik: String, metin: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(baslik) },
        text = { Text(metin) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        },
    )
}
