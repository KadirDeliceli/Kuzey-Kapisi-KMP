package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.components.GeriButonu

private val GENIS_EKRAN_ESIGI = 600.dp

@Composable
fun AdminAnaSayfaScreen(
    onGeri: () -> Unit,
    onPersonaEkleTiklandi: () -> Unit,
    onRotaYeriEkleTiklandi: () -> Unit,
    onPersonalariYonetTiklandi: () -> Unit,
    onRotaYerleriniYonetTiklandi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val genisEkran = maxWidth >= GENIS_EKRAN_ESIGI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            GeriButonu(metin = "Geri", onClick = onGeri)
            Text(
                text = "Yönetim Paneli",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )

            if (genisEkran) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        AdminKart(
                            baslik = "Persona Ekle",
                            aciklama = "Yeni bir tarihi kişilik, mekan, lezzet, doğa ya da tescilli ürün botu ekle",
                            onClick = onPersonaEkleTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                        AdminKart(
                            baslik = "Personaları Yönet",
                            aciklama = "Mevcut personaları listele, düzenle ya da sil",
                            onClick = onPersonalariYonetTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        AdminKart(
                            baslik = "Rota İçin Yeni Yer Ekle",
                            aciklama = "Akıllı Rota Planlayıcı'nın önerebileceği yeni bir mekan ekle",
                            onClick = onRotaYeriEkleTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                        AdminKart(
                            baslik = "Rota Yerlerini Yönet",
                            aciklama = "Mevcut rota mekanlarını listele, düzenle ya da sil",
                            onClick = onRotaYerleriniYonetTiklandi,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminKart(
                        baslik = "Persona Ekle",
                        aciklama = "Yeni bir tarihi kişilik, mekan, lezzet, doğa ya da tescilli ürün botu ekle",
                        onClick = onPersonaEkleTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AdminKart(
                        baslik = "Personaları Yönet",
                        aciklama = "Mevcut personaları listele, düzenle ya da sil",
                        onClick = onPersonalariYonetTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AdminKart(
                        baslik = "Rota İçin Yeni Yer Ekle",
                        aciklama = "Akıllı Rota Planlayıcı'nın önerebileceği yeni bir mekan ekle",
                        onClick = onRotaYeriEkleTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AdminKart(
                        baslik = "Rota Yerlerini Yönet",
                        aciklama = "Mevcut rota mekanlarını listele, düzenle ya da sil",
                        onClick = onRotaYerleriniYonetTiklandi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminKart(baslik: String, aciklama: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = aciklama,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
        )
    }
}
