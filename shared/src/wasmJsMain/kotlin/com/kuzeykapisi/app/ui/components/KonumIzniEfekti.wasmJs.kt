@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.kuzeykapisi.app.log.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Kullanıcı tarayıcının izin penceresinde hiç karar vermezse rotaların
 * sonsuza dek "hazırlanıyor"da kalmaması için GÜVENLİK sınırı. Sabit bir
 * "bekleme" süresi DEĞİL: bu süre dolunca varsayılan konumla devam edilir,
 * ama izin dinleyicisi açık kalır — kullanıcı dakikalar sonra bile izin
 * verse rotalar o anda gerçek konuma göre yenilenir.
 */
private const val KARARSIZLIK_GUVENLIK_SINIRI_MS = 25_000L

/** GeolocationPositionError.PERMISSION_DENIED */
private const val KONUM_IZNI_REDDEDILDI = 1

/**
 * Web'de izin kararı tarayıcının kendi penceresinde verilir. Bu efekt
 * sabit süre beklemek yerine GERÇEK sonucu dinler:
 *  - İzin zaten verilmiş / reddedilmişse sonuç hemen bildirilir.
 *  - Henüz sorulmamışsa ("prompt") izin penceresi açılır ve kullanıcının
 *    kararı beklenir (getCurrentPosition'ın kendi sonucu). Reddedilirse hemen
 *    false bildirilir.
 *  - Permissions API'nin "change" olayı ekran açık kaldıkça dinlenir: izin
 *    sonradan verilir/geri alınırsa yeni sonuç bildirilir, RotaViewModel
 *    konumu ve rotaları yeniler (bkz. RotaViewModel.konumIzniSonuclandi).
 *  - Ekran kapanınca dinleyici kaldırılır, güvenlik zamanlayıcısı iptal
 *    edilir ve geç gelen geri çağrılar yok sayılır.
 * Permissions API yoksa (eski tarayıcı) eski davranışa düşülür: true
 * bildirilir, izin penceresini guncelKonumAl() kendisi açar.
 */
@Composable
actual fun KonumIzniEfekti(onSonuc: (verildi: Boolean) -> Unit) {
    val guncelOnSonuc by rememberUpdatedState(onSonuc)
    val kapsam = rememberCoroutineScope()

    DisposableEffect(Unit) {
        var kapandi = false
        val bildir = { verildi: Boolean -> if (!kapandi) guncelOnSonuc(verildi) }

        val guvenlikIsi = kapsam.launch {
            delay(KARARSIZLIK_GUVENLIK_SINIRI_MS)
            Logger.d { "konum izni: kullanıcı karar vermedi, varsayılan konumla devam (izin dinleniyor)" }
            bildir(false)
        }

        val kayit = runCatching {
            jsIzinDurumunuDinle { olay ->
                if (kapandi) return@jsIzinDurumunuDinle
                Logger.d { "konum izni olayı: $olay" }
                when (olay) {
                    "ilk:granted", "degisti:granted" -> { guvenlikIsi.cancel(); bildir(true) }
                    "ilk:denied", "degisti:denied" -> { guvenlikIsi.cancel(); bildir(false) }
                    "ilk:prompt" -> {
                        // İzin penceresini aç ve kullanıcının kararını bekle (bizim
                        // sabit süremiz yok; yalnızca yukarıdaki güvenlik sınırı).
                        runCatching {
                            jsKonumIzniIste { hataKodu ->
                                if (kapandi) return@jsKonumIzniIste
                                guvenlikIsi.cancel()
                                // 0: izin verildi (konum alındı). PERMISSION_DENIED: gerçek
                                // red ya da pencere kapatıldı -> hemen varsayılan konum.
                                // Diğer hatalar (konum yok/zaman aşımı): izin var sayılır,
                                // konumu guncelKonumAl() yeniden dener, olmazsa varsayılana düşer.
                                bildir(hataKodu != KONUM_IZNI_REDDEDILDI)
                            }
                        }.onFailure { guvenlikIsi.cancel(); bildir(true) }
                    }
                    // Kullanıcı izni sıfırladı ("prompt"a döndü): mevcut rotalar kalır.
                    "degisti:prompt" -> Unit
                    else -> { guvenlikIsi.cancel(); bildir(true) } // API yok: eski davranış
                }
            }
        }.getOrElse {
            guvenlikIsi.cancel()
            bildir(true)
            null
        }

        onDispose {
            kapandi = true
            guvenlikIsi.cancel()
            kayit?.let { runCatching { jsIzinDinlemesiniBitir(it) } }
        }
    }
}

/**
 * navigator.permissions ile geolocation izin durumunu sorar ve "change"
 * olayına abone olur. [olay]: "ilk:<durum>", "degisti:<durum>" ya da
 * "desteklenmiyor". Dönen kayıt [jsIzinDinlemesiniBitir] ile kapatılır.
 */
private fun jsIzinDurumunuDinle(olay: (String) -> Unit): JsAny = js(
    """
    (function() {
        var kayit = { iptal: false, durum: null, dinleyici: null };
        if (!navigator.permissions || !navigator.permissions.query) { olay('desteklenmiyor'); return kayit; }
        navigator.permissions.query({ name: 'geolocation' }).then(function(durum) {
            if (kayit.iptal) return;
            kayit.durum = durum;
            kayit.dinleyici = function() { if (!kayit.iptal) olay('degisti:' + durum.state); };
            durum.addEventListener('change', kayit.dinleyici);
            olay('ilk:' + durum.state);
        }, function() { if (!kayit.iptal) olay('desteklenmiyor'); });
        return kayit;
    })()
    """,
)

private fun jsIzinDinlemesiniBitir(kayit: JsAny): Unit = js(
    """
    (function() {
        kayit.iptal = true;
        if (kayit.durum && kayit.dinleyici) kayit.durum.removeEventListener('change', kayit.dinleyici);
        kayit.durum = null; kayit.dinleyici = null;
    })()
    """,
)

/**
 * Konum ister (izin "prompt" durumundaysa tarayıcı izin penceresini açar).
 * [sonuc]: 0 = başarılı, aksi hâlde GeolocationPositionError.code.
 * Konumun kendisi burada kullanılmaz; asıl konumu RotaViewModel alır.
 */
private fun jsKonumIzniIste(sonuc: (Int) -> Unit): Unit = js(
    """
    (function() {
        if (!navigator.geolocation) { sonuc(2); return; }
        navigator.geolocation.getCurrentPosition(
            function() { sonuc(0); },
            function(hata) { sonuc((hata && hata.code) || 2); }
        );
    })()
    """,
)
