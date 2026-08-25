package com.kuzeykapisi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.kartEtkilesimi
import com.kuzeykapisi.app.ui.theme.DerinDeniz
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi

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
            EkranBasligi(
                baslik = "Yönetim Paneli",
                etiket = "Yönetim",
                geriMetni = "Geri",
                onGeri = onGeri,
                modifier = Modifier.padding(bottom = 24.dp),
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Admin ana ekranı kartı — ana sayfa kartlarıyla aynı "kesik taş" formu ve
 * hover kenarlığı, ama görselsiz ve halesiz: burası bir iş ekranı, vitrin
 * değil.
 */
@Composable
private fun AdminKart(baslik: String, aciklama: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val etkilesim = kartEtkilesimi(interactionSource)

    Column(
        modifier = modifier
            .scale(etkilesim.olcek)
            .clip(KartSekli)
            .background(DerinDeniz)
            .border(etkilesim.kenarKalinligi, etkilesim.kenarRengi, KartSekli)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(22.dp),
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.titleLarge,
            color = TasBeyazi,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = aciklama,
            style = MaterialTheme.typography.bodyMedium,
            color = SisGrisi,
        )
    }
}
