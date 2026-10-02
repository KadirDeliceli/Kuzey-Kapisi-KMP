package com.kuzeykapisi.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.Metinler
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.ui.components.EkranBasligi
import com.kuzeykapisi.app.ui.components.FenerHalesi
import com.kuzeykapisi.app.ui.components.HataGorunumu
import com.kuzeykapisi.app.ui.components.YukleniyorGorunumu
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.MIKRO_SURE
import com.kuzeykapisi.app.ui.theme.NotrGeceCizgi
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import com.kuzeykapisi.app.ui.theme.Yukseklik
import com.kuzeykapisi.app.ui.theme.fenerHalesiDestekli
import com.kuzeykapisi.app.ui.theme.klavyeOdakHalkasi
import kotlinx.coroutines.CancellationException

/** Sohbet butonunun çapı ve ekran kenarından uzaklığı. */
private val FAB_CAPI = 56.dp
private val FAB_KENAR_BOSLUGU = 24.dp

/** Kaydırılan metnin son satırı sabit sohbet butonunun altında kalmasın diye bırakılan pay. */
private val FAB_ICIN_ALT_PAY = FAB_CAPI + FAB_KENAR_BOSLUGU * 2

/** AnlatimEkrani'ndaki okuma sütunuyla aynı satır uzunluğu sınırı. */
private val OKUMA_SUTUNU_GENISLIGI = 680.dp

private sealed interface AnlatimYukleme {
    data object Yukleniyor : AnlatimYukleme
    data class Hazir(val metin: String) : AnlatimYukleme
    data class Hata(val mesaj: String) : AnlatimYukleme
}

/**
 * Persona kartına dokununca açılan önizleme. Anlatımı olan personada metnin
 * tamamını ve sohbete davet eden bir kapanış cümlesini, olmayanda sade bir
 * boş-durum mesajı gösterir. İki durumda da sağ altta sabit duran yapay zeka
 * butonu, [onSohbetAc] ile mevcut sohbet panelini açar.
 *
 * Ses burada çalınmaz: tam sesli dinletme kart üzerindeki ses ikonunun açtığı
 * AnlatimEkrani'nda kalır. Bu yüzden metin, TTS motoru kuran AnlatimViewModel
 * yerine aynı repository fonksiyonuyla ([KuzeyRepository.anlatimGetir]) çekilir
 * ve yalnızca [anlatimVar] true iken istek atılır.
 */
@Composable
fun PersonaOnizlemeEkrani(
    repo: KuzeyRepository,
    kategori: String,
    kod: String,
    ad: String,
    anlatimVar: Boolean,
    onGeri: () -> Unit,
    onSohbetAc: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            EkranBasligi(
                baslik = ad,
                geriMetni = "Geri",
                onGeri = onGeri,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            if (anlatimVar) {
                AnlatimIcerigi(repo = repo, kategori = kategori, kod = kod)
            } else {
                AnlatimYokDurumu()
            }
        }

        // Kaydırılan içeriğin DIŞINDA, dış kutuya hizalı: metin kayarken yerinde durur.
        SohbetButonu(
            onClick = onSohbetAc,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(FAB_KENAR_BOSLUGU),
        )
    }
}

@Composable
private fun AnlatimIcerigi(repo: KuzeyRepository, kategori: String, kod: String) {
    var deneme by remember { mutableIntStateOf(0) }
    val durum by produceState<AnlatimYukleme>(AnlatimYukleme.Yukleniyor, repo, kategori, kod, deneme) {
        value = AnlatimYukleme.Yukleniyor
        value = try {
            // null: backend bu öge için anlatım olmadığını söyledi (404) — hata değil.
            AnlatimYukleme.Hazir(repo.anlatimGetir(kategori, kod).orEmpty())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AnlatimYukleme.Hata(Metinler.hataMesaji(e))
        }
    }

    when (val d = durum) {
        AnlatimYukleme.Yukleniyor -> YukleniyorGorunumu(modifier = Modifier.fillMaxSize())
        is AnlatimYukleme.Hata -> HataGorunumu(
            mesaj = d.mesaj,
            modifier = Modifier.fillMaxSize(),
            onTekrarDene = { deneme++ },
        )
        is AnlatimYukleme.Hazir ->
            if (d.metin.isBlank()) AnlatimYokDurumu() else AnlatimMetni(metin = d.metin)
    }
}

/**
 * Okuma sütunu: sınırlı satır uzunluğu, ferah satır arası. Metnin altında ince
 * bir ayraç ve gövdeden ayrışan kapanış cümlesi (Fraunces italik — gövde
 * ailesinin italik kesimi yok, sahte eğik yerine gerçek italik).
 */
@Composable
private fun AnlatimMetni(metin: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = FAB_ICIN_ALT_PAY),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.widthIn(max = OKUMA_SUTUNU_GENISLIGI).fillMaxWidth()) {
            Text(
                text = metin,
                style = MaterialTheme.typography.bodyLarge,
                color = TasBeyazi,
            )
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(thickness = 1.dp, color = NotrGeceCizgi)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Daha fazlası için yapay zeka destekli konuşmaya geçin.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                ),
                color = SisGrisi,
            )
        }
    }
}

/** Anlatımı olmayan persona: kalan alanın ortasında sakin bir boş durum. */
@Composable
private fun AnlatimYokDurumu() {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Metinler.ANLATIM_YOK,
                style = MaterialTheme.typography.titleMedium,
                color = TasBeyazi,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Metinler.ANLATIM_YOK_ACIKLAMA,
                style = MaterialTheme.typography.bodyMedium,
                color = SisGrisi,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Yüzen yapay zeka sohbet butonu. FenerAlevi dolgu üzerinde KaranlikLacivert
 * simge (7.87:1; TasBeyazi bu dolguda 1.88:1 kalıyordu). Ana sayfadaki
 * etkileşim diliyle aynı: web'de hover'da fener halesi ve %2 büyüme, basılıyken
 * %97, klavye odağında TasBeyazi halka.
 */
@Composable
private fun SohbetButonu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val kaynak = remember { MutableInteractionSource() }
    val hoverlu by kaynak.collectIsHoveredAsState()
    val basili by kaynak.collectIsPressedAsState()
    val odakli by kaynak.collectIsFocusedAsState()
    val olcek by animateFloatAsState(
        targetValue = when {
            basili -> 0.97f
            hoverlu && fenerHalesiDestekli -> 1.02f
            else -> 1f
        },
        animationSpec = tween(MIKRO_SURE),
        label = "sohbetButonuOlcegi",
    )
    val golge by animateDpAsState(
        targetValue = if (hoverlu || basili) Yukseklik.DP12 else Yukseklik.DP6,
        animationSpec = tween(MIKRO_SURE),
        label = "sohbetButonuGolgesi",
    )

    Box(modifier = modifier.scale(olcek)) {
        FenerHalesi(gorunur = hoverlu, sekil = CircleShape)
        Box(
            modifier = Modifier
                .size(FAB_CAPI)
                .klavyeOdakHalkasi(odakli, CircleShape)
                .shadow(elevation = golge, shape = CircleShape, ambientColor = KaranlikLacivert, spotColor = KaranlikLacivert)
                .clip(CircleShape)
                .background(FenerAlevi)
                .hoverable(interactionSource = kaynak)
                .clickable(interactionSource = kaynak, indication = null, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = "Yapay zeka ile sohbet et" },
            contentAlignment = Alignment.Center,
        ) {
            YapayZekaSimgesi(modifier = Modifier.size(24.dp))
        }
    }
}

/** lucide `sparkles` geometrisi (24'lük ızgara): içbükey dört köşeli yıldız + iki küçük artı. */
@Composable
private fun YapayZekaSimgesi(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val b = size.width / 24f
        val cizgi = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val yildiz = Path().apply {
            moveTo(10f * b, 3f * b)
            quadraticTo(11.2f * b, 8.8f * b, 17f * b, 10f * b)
            quadraticTo(11.2f * b, 11.2f * b, 10f * b, 17f * b)
            quadraticTo(8.8f * b, 11.2f * b, 3f * b, 10f * b)
            quadraticTo(8.8f * b, 8.8f * b, 10f * b, 3f * b)
            close()
        }
        drawPath(path = yildiz, color = KaranlikLacivert, style = cizgi)
        drawLine(KaranlikLacivert, Offset(19f * b, 3f * b), Offset(19f * b, 7f * b), cizgi.width, StrokeCap.Round)
        drawLine(KaranlikLacivert, Offset(17f * b, 5f * b), Offset(21f * b, 5f * b), cizgi.width, StrokeCap.Round)
        drawLine(KaranlikLacivert, Offset(18f * b, 16f * b), Offset(18f * b, 20f * b), cizgi.width, StrokeCap.Round)
        drawLine(KaranlikLacivert, Offset(16f * b, 18f * b), Offset(20f * b, 18f * b), cizgi.width, StrokeCap.Round)
    }
}
