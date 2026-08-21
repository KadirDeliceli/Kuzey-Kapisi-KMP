package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.DialogProperties
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yönetici Girişi") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = kullaniciAdi,
                    onValueChange = { kullaniciAdi = it; vm.hataTemizle() },
                    label = { Text("Kullanıcı adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = sifre,
                    onValueChange = { sifre = it; vm.hataTemizle() },
                    label = { Text("Şifre") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (ui.hata != null) {
                    Text(
                        text = ui.hata ?: "",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.girisYap(kullaniciAdi, sifre) },
                enabled = !ui.yukleniyor,
            ) {
                Text(if (ui.yukleniyor) "Giriş yapılıyor…" else "Giriş Yap")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
    )
}
