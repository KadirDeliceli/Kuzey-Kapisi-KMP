@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.data.ses

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val MESAJ_IZIN_YOK = "Mikrofon izni verilmedi."
private const val MESAJ_KALICI_RET =
    "Mikrofon erişimi engellenmiş. Tarayıcının adres çubuğundaki kilit/site bilgisi " +
        "simgesine tıklayıp mikrofon iznini 'İzin Ver' yapın, sonra sayfayı yenileyin."
private const val MESAJ_KAYDEDILEMEDI = "Ses kaydedilemedi."

/** getUserMedia'nın reddettiğinde verdiği hata adı — standart ve eski (legacy) isimlerin ikisi de kontrol edilir. */
private fun izinReddiMi(hataAdi: String): Boolean = hataAdi == "NotAllowedError" || hataAdi == "PermissionDeniedError"

/** JS tarafında oluşturulan, kaydı durdurup base64 sonucunu üreten opak tutamaç. */
private external interface JsSesKayitTutamaci : JsAny {
    fun durdurVeAl(basarili: (JsSesKayitSonucu) -> Unit, hataOldu: (String) -> Unit)
    fun iptalEt()
}

private external interface JsSesKayitSonucu : JsAny {
    val base64Veri: String
    val mimeTipi: String
}

/**
 * navigator.mediaDevices.getUserMedia({audio:true}) + JS MediaRecorder ile
 * kayda başlar. TAMAMEN ASENKRONDUR (izin diyaloğu beklenir); [basladi] ya
 * da [hataOldu] callback'i asıl sonucu bildirir. Veri, Konum.wasmJs.kt /
 * SecilenResim.wasmJs.kt'deki aynı stille (ham JS interop + base64 köprüsü)
 * Kotlin tarafına aktarılır.
 */
private fun jsSesKaydiBaslat(
    basladi: (JsSesKayitTutamaci) -> Unit,
    hataOldu: (String) -> Unit,
) {
    js(
        """
        (function() {
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia || !window.MediaRecorder) {
                hataOldu('desteklenmiyor');
                return;
            }
            navigator.mediaDevices.getUserMedia({ audio: true }).then(function(stream) {
                var mimeType = 'audio/webm';
                try {
                    if (MediaRecorder.isTypeSupported && MediaRecorder.isTypeSupported('audio/webm;codecs=opus')) {
                        mimeType = 'audio/webm;codecs=opus';
                    }
                } catch (e) { /* varsayılan mimeType ile devam */ }

                var recorder;
                try { recorder = new MediaRecorder(stream, { mimeType: mimeType }); }
                catch (e) { recorder = new MediaRecorder(stream); }

                var chunks = [];
                recorder.ondataavailable = function(e) {
                    if (e.data && e.data.size > 0) chunks.push(e.data);
                };

                var durduruldu = false;
                function akisiKapat() {
                    try { stream.getTracks().forEach(function(t) { t.stop(); }); } catch (e) { /* yut */ }
                }

                var tutamac = {
                    durdurVeAl: function(basarili, hataOldu) {
                        if (durduruldu) { hataOldu('zaten-durduruldu'); return; }
                        durduruldu = true;
                        recorder.onstop = function() {
                            try {
                                var blob = new Blob(chunks, { type: recorder.mimeType || mimeType });
                                var okuyucu = new FileReader();
                                okuyucu.onload = function(e) {
                                    var sonuc = e.target.result;
                                    var virgul = sonuc.indexOf(',');
                                    var b64 = virgul >= 0 ? sonuc.substring(virgul + 1) : sonuc;
                                    akisiKapat();
                                    basarili({ base64Veri: b64, mimeTipi: blob.type || mimeType });
                                };
                                okuyucu.onerror = function() { akisiKapat(); hataOldu('okuma-hatasi'); };
                                okuyucu.readAsDataURL(blob);
                            } catch (e) { akisiKapat(); hataOldu('durdurma-hatasi'); }
                        };
                        try { recorder.stop(); } catch (e) { akisiKapat(); hataOldu('durdurma-hatasi'); }
                    },
                    iptalEt: function() {
                        durduruldu = true;
                        try { recorder.onstop = null; recorder.stop(); } catch (e) { /* yut */ }
                        akisiKapat();
                    },
                };

                try {
                    recorder.start();
                    basladi(tutamac);
                } catch (e) {
                    akisiKapat();
                    hataOldu('baslatma-hatasi');
                }
            }).catch(function(err) {
                hataOldu((err && err.name) ? err.name : 'izin-reddedildi');
            });
        })();
        """,
    )
}

/**
 * navigator.permissions.query({name:'microphone'}) DESTEKLENİYORSA mevcut
 * izin durumunu proaktif olarak bildirir (kullanıcı mikrofon butonuna hiç
 * basmadan) ve durum değiştikçe (onchange) tekrar bildirir. Safari/Firefox
 * gibi bu sorguyu desteklemeyen tarayıcılarda [desteklenmiyor] çağrılır —
 * o durumda izin durumu yalnızca gerçek bir getUserMedia denemesiyle anlaşılır.
 */
private fun jsIzinDurumunuIzle(sonuc: (String) -> Unit, desteklenmiyor: () -> Unit) {
    js(
        """
        (function() {
            if (!navigator.permissions || !navigator.permissions.query) { desteklenmiyor(); return; }
            navigator.permissions.query({ name: 'microphone' }).then(function(durum) {
                sonuc(durum.state);
                durum.onchange = function() { sonuc(durum.state); };
            }).catch(function() { desteklenmiyor(); });
        })();
        """,
    )
}

@OptIn(ExperimentalEncodingApi::class)
actual class SesKaydedici actual constructor() {
    private val _durum = MutableStateFlow(KayitDurumu.BOSTA)
    actual val durum: StateFlow<KayitDurumu> = _durum.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    actual val hata: StateFlow<String?> = _hata.asStateFlow()

    private val _izinDurumu = MutableStateFlow(MikrofonIzniDurumu.SORULMADI)
    actual val izinDurumu: StateFlow<MikrofonIzniDurumu> = _izinDurumu.asStateFlow()

    actual val ayarlarDestekleniyor: Boolean = false

    private var tutamac: JsSesKayitTutamaci? = null

    // kayidaBasla() senkron değil (getUserMedia asenkron) — bu bayrak,
    // izin/kayıt sonuçlanmadan ÖNCE kayidiDurdurVeAl() çağrılırsa sonucu
    // sessizce iptal etmek için kullanılır.
    private var durdurmaBeklemede = false

    init {
        // Kullanıcı daha mikrofon butonuna hiç basmadan tarayıcının mevcut
        // izin durumunu bilsin — desteklenmiyorsa (Safari/Firefox) sessizce
        // no-op, gerçek durum ilk kayıt denemesinde getUserMedia'dan öğrenilir.
        runCatching {
            jsIzinDurumunuIzle(
                sonuc = { durum ->
                    when (durum) {
                        "denied" -> {
                            _izinDurumu.value = MikrofonIzniDurumu.KALICI_REDDEDILDI
                            _hata.value = MESAJ_KALICI_RET
                        }
                        "granted" -> {
                            _izinDurumu.value = MikrofonIzniDurumu.SORULMADI
                            if (_hata.value == MESAJ_KALICI_RET) _hata.value = null
                        }
                        else -> Unit // "prompt" — henüz sorulmadı.
                    }
                },
                desteklenmiyor = {},
            )
        }
    }

    actual fun kayidaBasla() {
        if (_durum.value != KayitDurumu.BOSTA) return
        _hata.value = null
        durdurmaBeklemede = false
        _durum.value = KayitDurumu.KAYIT_YAPILIYOR

        jsSesKaydiBaslat(
            basladi = { t ->
                if (durdurmaBeklemede) {
                    durdurmaBeklemede = false
                    t.iptalEt()
                    _durum.value = KayitDurumu.BOSTA
                } else {
                    tutamac = t
                }
            },
            hataOldu = { hataAdi ->
                tutamac = null
                durdurmaBeklemede = false
                _durum.value = KayitDurumu.BOSTA
                if (izinReddiMi(hataAdi)) {
                    _izinDurumu.value = MikrofonIzniDurumu.KALICI_REDDEDILDI
                    _hata.value = MESAJ_KALICI_RET
                } else {
                    _hata.value = MESAJ_IZIN_YOK
                }
            },
        )
    }

    actual suspend fun kayidiDurdurVeAl(): KaydedilenSes? {
        if (_durum.value != KayitDurumu.KAYIT_YAPILIYOR) return null

        val t = tutamac
        if (t == null) {
            // getUserMedia/izin diyaloğu hâlâ beklemede — sonuçlandığında iptal edilecek.
            durdurmaBeklemede = true
            _durum.value = KayitDurumu.BOSTA
            return null
        }
        tutamac = null
        _durum.value = KayitDurumu.ISLENIYOR

        val sonuc = try {
            suspendCancellableCoroutine<JsSesKayitSonucu?> { cont ->
                t.durdurVeAl(
                    basarili = { s -> if (cont.isActive) cont.resume(s) },
                    hataOldu = { if (cont.isActive) cont.resume(null) },
                )
            }
        } catch (e: Throwable) {
            null
        }

        _durum.value = KayitDurumu.BOSTA

        if (sonuc == null) {
            _hata.value = MESAJ_KAYDEDILEMEDI
            return null
        }
        val bytes = runCatching { Base64.decode(sonuc.base64Veri) }.getOrNull()
        if (bytes == null || bytes.isEmpty()) {
            _hata.value = MESAJ_KAYDEDILEMEDI
            return null
        }
        return KaydedilenSes(bytes, "ses_kaydi.webm", sonuc.mimeTipi.ifBlank { "audio/webm" })
    }

    actual fun ayarlariAc() {
        // no-op — tarayıcıdan ayarlara güvenlik nedeniyle deep-link yapılamaz;
        // kullanıcı MESAJ_KALICI_RET'teki talimatı elle izlemeli.
    }
}
