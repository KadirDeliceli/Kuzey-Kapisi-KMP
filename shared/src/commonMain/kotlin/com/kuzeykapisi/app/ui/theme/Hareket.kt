package com.kuzeykapisi.app.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate

/** "Kapı açılma" geçişinin temel süresi (ms). */
const val KAPI_SURESI = 380

/** Hareket azaltıldığında kullanılan kısa süre (ms). */
private const val KAPI_SURESI_KISA = 90

/** Kart hover/basma geçişleri gibi mikro-etkileşimlerin süresi (ms). */
const val MIKRO_SURE = 200

/** Fener halesinin hover'dan sonraki gecikmesi (ms). */
const val HALE_GECIKMESI = 150

internal val gecisSuresi: Int
    get() = if (hareketAzaltilsin) KAPI_SURESI_KISA else KAPI_SURESI

/**
 * İMZA HAREKET — "Kapı Açılma".
 *
 * Ekran hiyerarşisinde DERİNE inerken içerik ortadan açılarak gelir: sol yarısı
 * sola, sağ yarısı sağa kayar ve ortadan dışa doğru genişleyen bir pencere
 * içinden belirir. GERİ dönerken aynı hareket tersine işler — iki yarı ortaya
 * doğru kapanır ve üst (bir önceki) ekran ortaya çıkar.
 *
 * Kapı her zaman hiyerarşide DAHA DERİN olan ekrana uygulanır; daha sığ olan
 * ekran yalnızca yumuşakça çapraz-solar. Bu yüzden ileri/geri yönü tek bir
 * [ileri] bayrağıyla verilir.
 *
 * NOT: Bu bileşen SADECE görsel geçişi yönetir — hangi ekranın gösterileceği
 * kararı çağıran tarafta kalır, navigasyon mantığına dokunmaz.
 */
@Composable
fun <T> KapiGecisi(
    hedef: T,
    ileri: Boolean,
    modifier: Modifier = Modifier,
    icerik: @Composable (T) -> Unit,
) {
    val sure = gecisSuresi
    AnimatedContent(
        targetState = hedef,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(sure, easing = LinearOutSlowInEasing)) togetherWith
                fadeOut(animationSpec = tween(sure, easing = FastOutSlowInEasing)) using
                SizeTransform(clip = false)
        },
        label = "kapiGecisi",
    ) { durum ->
        // Bu örnek hedef ekran mı? İleri giderken kapı hedefe (yeni, daha derin
        // ekran), geri gelirken çıkan ekrana (eski, daha derin ekran) uygulanır.
        val budurHedef = durum == hedef
        val kapiUygula = if (ileri) budurHedef else !budurHedef

        val ilerleme by transition.animateFloat(
            transitionSpec = { tween(sure, easing = FastOutSlowInEasing) },
            label = "kapiIlerlemesi",
        ) { asama -> if (asama == EnterExitState.Visible) 1f else 0f }

        Box(
            modifier = if (kapiUygula) {
                Modifier.fillMaxSize().kapiPerdesi(ilerleme)
            } else {
                Modifier.fillMaxSize()
            },
        ) {
            icerik(durum)
        }
    }
}

/**
 * İçeriği iki dikey panele bölüp ortadan dışa doğru açar.
 *
 * `ilerleme` 0f → kapalı (hiçbir şey görünmez), 1f → tamamen açık (içerik
 * olduğu gibi). Ortadan dışa genişleyen pencereye ek olarak sol yarı sola,
 * sağ yarı sağa doğru küçük bir mesafe kayar — iki kanadı olan bir kapı hissi.
 *
 * Tamamen açık durumda (durağan hâl) hiçbir ek çizim yapılmaz; kırpma ve
 * çift çizim yalnızca animasyon karelerinde devrededir.
 */
private fun Modifier.kapiPerdesi(ilerleme: Float): Modifier = drawWithContent {
    if (ilerleme >= 0.995f) {
        drawContent()
        return@drawWithContent
    }
    if (ilerleme <= 0.005f) return@drawWithContent

    val kapsam = this
    val genislik = size.width
    val yukseklik = size.height
    val orta = genislik / 2f
    val yariPencere = orta * ilerleme
    // Kanatların yerine oturmadan önceki kayma payı: sol kanat sağdan sola,
    // sağ kanat soldan sağa gelir.
    val kayma = genislik * 0.10f * (1f - ilerleme)

    clipRect(left = orta - yariPencere, top = 0f, right = orta, bottom = yukseklik) {
        translate(left = kayma) { kapsam.drawContent() }
    }
    clipRect(left = orta, top = 0f, right = orta + yariPencere, bottom = yukseklik) {
        translate(left = -kayma) { kapsam.drawContent() }
    }
}
