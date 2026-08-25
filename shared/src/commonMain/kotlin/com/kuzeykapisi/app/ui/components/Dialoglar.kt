package com.kuzeykapisi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kuzeykapisi.app.ui.theme.DialogSekli
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.YuksekYuzey

/**
 * Projedeki TÜM dialogların ortak kabuğu: yükseltilmiş koyu yüzey, sağ-alt
 * köşesi kesik [DialogSekli] formu ve üst kenarında ince bir ışık hattı.
 *
 * [vurguRengi] varsayılan olarak [FenerAlevi]'dir; yıkıcı (silme) dialoglarında
 * çağıran taraf SinopKirmizisi geçer — kabuk kenarlığı ve üst hattı o renge
 * döner, böylece "bu geri alınamaz" uyarısı renkle de verilir.
 */
@Composable
fun KuzeyDialogKabugu(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    vurguRengi: Color = FenerAlevi,
    dismissOnClickOutside: Boolean = true,
    icerik: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = dismissOnClickOutside),
    ) {
        Column(
            modifier = modifier
                .widthIn(max = 460.dp)
                .clip(DialogSekli)
                .background(YuksekYuzey)
                .border(1.dp, vurguRengi.copy(alpha = 0.45f), DialogSekli),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, vurguRengi, Color.Transparent),
                        ),
                    ),
            )
            Column(modifier = Modifier.padding(24.dp)) { icerik() }
        }
    }
}

/** Dialog başlığı. */
@Composable
fun DialogBasligi(metin: String) {
    Text(
        text = metin,
        style = MaterialTheme.typography.titleLarge,
        color = TasBeyazi,
    )
}

/**
 * Dialog gövde metni. Uzun metinler dialogu ekrandan taşırmasın diye kendi
 * içinde kayar.
 */
@Composable
fun DialogMetni(metin: String, modifier: Modifier = Modifier) {
    Text(
        text = metin,
        style = MaterialTheme.typography.bodyMedium,
        color = SisGrisi,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .heightIn(max = 380.dp)
            .verticalScroll(rememberScrollState()),
    )
}

/** Dialogun alt eylem satırı — butonlar sağa yaslı. */
@Composable
fun DialogEylemleri(modifier: Modifier = Modifier, icerik: @Composable () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icerik()
    }
}
