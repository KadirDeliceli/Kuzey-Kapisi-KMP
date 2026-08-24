package com.kuzeykapisi.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Silme gibi geri alınamaz işlemler için onay/vazgeç dialogu — InfoDialog'un
 * tek butonlu (yalnızca "Kapat") halinden farklı olarak iki seçenek sunar.
 */
@Composable
fun OnayDialog(
    baslik: String,
    metin: String,
    onOnay: () -> Unit,
    onVazgec: () -> Unit,
    onayMetni: String = "Sil",
) {
    AlertDialog(
        onDismissRequest = onVazgec,
        title = { Text(baslik) },
        text = { Text(metin) },
        confirmButton = {
            TextButton(onClick = onOnay) {
                Text(onayMetni, color = MaterialTheme.colorScheme.tertiary)
            }
        },
        dismissButton = {
            TextButton(onClick = onVazgec) { Text("Vazgeç") }
        },
    )
}
