@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.data.model

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private external interface JsResimSecSonucu : JsAny {
    val dosyaAdi: String
    val mimeTipi: String
    val base64Veri: String
}

/**
 * Gizli bir <input type=file> oluşturur, tıklatır ve seçilen dosyayı
 * FileReader.readAsDataURL ile base64'e çevirir. Veriyi typed array/ByteArray
 * köprüsü kurmadan tek bir String olarak Kotlin tarafına aktarmak için
 * data URL yaklaşımı tercih edildi (Konum.wasmJs.kt'deki ham JS interop
 * deseniyle aynı stil).
 */
private fun jsResimSecBaslat(
    basarili: (JsResimSecSonucu) -> Unit,
    basarisiz: () -> Unit,
    iptal: () -> Unit,
) {
    js(
        """
        (function() {
            var input = document.createElement('input');
            input.type = 'file';
            input.accept = 'image/*';
            input.style.display = 'none';
            document.body.appendChild(input);

            var tamamlandi = false;
            function temizle() {
                if (input.parentNode) input.parentNode.removeChild(input);
            }

            input.onchange = function() {
                var dosya = input.files && input.files[0];
                tamamlandi = true;
                temizle();
                if (!dosya) { iptal(); return; }
                var okuyucu = new FileReader();
                okuyucu.onload = function(e) {
                    var sonuc = e.target.result;
                    var virgul = sonuc.indexOf(',');
                    var b64 = virgul >= 0 ? sonuc.substring(virgul + 1) : sonuc;
                    basarili({ dosyaAdi: dosya.name, mimeTipi: dosya.type, base64Veri: b64 });
                };
                okuyucu.onerror = function() { basarisiz(); };
                okuyucu.readAsDataURL(dosya);
            };

            var odakDinleyici = function() {
                window.removeEventListener('focus', odakDinleyici);
                setTimeout(function() {
                    if (!tamamlandi) {
                        tamamlandi = true;
                        temizle();
                        iptal();
                    }
                }, 300);
            };
            window.addEventListener('focus', odakDinleyici);

            input.click();
        })();
        """,
    )
}

@OptIn(ExperimentalEncodingApi::class)
actual suspend fun resimSec(): SecilenResim? {
    return suspendCancellableCoroutine { cont ->
        jsResimSecBaslat(
            basarili = { sonuc ->
                if (cont.isActive) {
                    val bytes = runCatching { Base64.decode(sonuc.base64Veri) }.getOrNull()
                    val uzanti = when {
                        sonuc.mimeTipi.contains("png") -> ".png"
                        sonuc.mimeTipi.contains("webp") -> ".webp"
                        else -> ".jpg"
                    }
                    cont.resume(bytes?.let { SecilenResim(it, sonuc.dosyaAdi.ifBlank { "secilen_gorsel$uzanti" }, uzanti) })
                }
            },
            basarisiz = { if (cont.isActive) cont.resume(null) },
            iptal = { if (cont.isActive) cont.resume(null) },
        )
    }
}
