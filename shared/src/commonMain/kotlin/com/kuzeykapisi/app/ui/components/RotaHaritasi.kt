package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import com.kuzeykapisi.app.data.model.RotaDurak
import com.kuzeykapisi.app.ui.theme.FenerAlevi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KartSekli
import com.kuzeykapisi.app.ui.theme.NotrGeceYuksek
import com.kuzeykapisi.app.ui.theme.SisGrisi
import com.kuzeykapisi.app.ui.theme.TasBeyazi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kuzeykapisiapp.shared.generated.resources.Res
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// Leaflet unpkg'den, Subresource Integrity (SRI) ile yüklenir: CDN'deki dosya
// değişirse tarayıcı onu ÇALIŞTIRMAZ. Hash'ler indirilen 1.9.4 dosyalarından
// hesaplandı (openssl dgst -sha256 | base64) ve leafletjs.com'un yayınladığı
// değerlerle aynıdır. Sürüm yükseltilirse ikisi birlikte güncellenmelidir.
private const val LEAFLET_CSS_SRI = "sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
private const val LEAFLET_JS_SRI = "sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="

/** Gövde fontunun HTML içindeki adı (bkz. [govdeFontuCss]). */
private const val GOVDE_FONT_AILESI = "KK Govde"

// Renkler, köşe yarıçapları ve font __AD__ yer tutucularıyla gelir; değerler
// rotaHaritasiHtmlOlustur içinde tasarım sistemi token'larından (Color.kt,
// Sekiller.kt, Type.kt'nin kullandığı font dosyası) üretilir. Bu şablonda
// elle yazılmış renk YOKTUR.
private const val HTML_SABLONU = """<!DOCTYPE html>
<html lang="tr">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
      integrity="__LEAFLET_CSS_SRI__" crossorigin="" />
<style>
  __GOVDE_FONTU__
  /* Harita, uygulamanın gece denizi paletine oturur: karo katmanı hafifçe
     karartılıp doygunluğu düşürülür, üzerindeki rota ve pinler fener
     aleviyle öne çıkar. */
  html, body, #map { height: 100%; margin: 0; padding: 0; background: __ZEMIN__; }
  .leaflet-tile-pane { filter: brightness(0.72) saturate(0.55) contrast(1.05); }
  .leaflet-container { background: __ZEMIN__; font-family: __FONT__; }
  .kk-pin {
    width: 24px; height: 24px; border-radius: 50% 50% 50% 4px;
    background: __VURGU__; color: __ZEMIN__;
    font: 600 12px/24px __FONT__;
    text-align: center; box-shadow: 0 0 0 2px __PIN_GOLGE__;
  }
  .leaflet-popup-content-wrapper, .leaflet-popup-tip {
    background: __POPUP_ZEMIN__; color: __METIN__;
  }
  .leaflet-popup-content-wrapper { border-radius: __POPUP_KOSE__; }
  .leaflet-popup-content { font: 600 14px/1.4 __FONT__; margin: 12px 16px; }
  .leaflet-container a.leaflet-popup-close-button { color: __IKINCIL__; }
  .leaflet-control-attribution { background: __ATIF_ZEMIN__ !important; color: __IKINCIL__ !important; }
  .leaflet-control-attribution a { color: __IKINCIL__ !important; }
</style>
</head>
<body>
<div id="map"></div>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="__LEAFLET_JS_SRI__" crossorigin=""></script>
<script>
  var duraklar = __DURAKLAR_JSON__;
  var map = L.map('map');
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap katkıda bulunanlar',
    maxZoom: 19
  }).addTo(map);
  var noktalar = [];
  duraklar.forEach(function(d) {
    // Veriden gelen hiçbir değer HTML olarak yorumlanmaz: pin ve popup
    // içeriği DOM düğümü olarak kurulur, metin textContent ile yazılır.
    // Admin panelinden girilen "<img onerror=...>" gibi bir mekan adı düz
    // metin olarak görünür, çalışmaz.
    var pin = document.createElement('div');
    pin.className = 'kk-pin';
    pin.textContent = String(d.sira);
    var ikon = L.divIcon({
      className: '',
      html: pin,
      iconSize: [24, 24],
      iconAnchor: [12, 24],
      popupAnchor: [0, -22]
    });
    var marker = L.marker([d.lat, d.lng], {icon: ikon}).addTo(map);
    var baslik = document.createElement('b');
    baslik.textContent = d.sira + '. ' + d.ad;
    marker.bindPopup(baslik);
    noktalar.push([d.lat, d.lng]);
  });
  if (noktalar.length > 1) {
    L.polyline(noktalar, {color: '__VURGU__', weight: 3, opacity: 0.9}).addTo(map);
  }
  if (noktalar.length > 0) {
    map.fitBounds(noktalar, {padding: [30, 30]});
  } else {
    map.setView([41.0, 35.0], 6);
  }
</script>
</body>
</html>"""

/** Token rengini CSS'e çevirir: opaksa #RRGGBB, değilse rgba(...). */
private fun Color.css(alfa: Float = alpha): String {
    val argb = toArgb()
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    if (alfa >= 1f) {
        return "#" + listOf(r, g, b).joinToString("") { it.toString(16).padStart(2, '0').uppercase() }
    }
    val alfaMetni = (kotlin.math.round(alfa * 100) / 100.0).toString()
    return "rgba($r,$g,$b,$alfaMetni)"
}

/**
 * Popup köşeleri, kartların imza geometrisinden ([KartSekli]: üç köşe 20,
 * sağ-alt 4) okunur — sayı burada tekrar yazılmaz. CSS sırası: sol-üst,
 * sağ-üst, sağ-alt, sol-alt.
 */
private fun imzaKoseCss(): String {
    val olcu = Size(1000f, 1000f)
    val yogunluk = Density(1f) // 1 px = 1 dp: dp cinsinden değer döner
    val px = { k: androidx.compose.foundation.shape.CornerSize -> "${k.toPx(olcu, yogunluk).toInt()}px" }
    return listOf(KartSekli.topStart, KartSekli.topEnd, KartSekli.bottomEnd, KartSekli.bottomStart)
        .joinToString(" ") { px(it) }
}

private fun govdeFontuCss(fontBase64: String?): String =
    if (fontBase64.isNullOrEmpty()) {
        ""
    } else {
        "@font-face { font-family: '$GOVDE_FONT_AILESI'; font-weight: 100 900; " +
            "src: url(data:font/ttf;base64,$fontBase64) format('truetype'); }"
    }

/**
 * JSON'u bir <script> bloğuna güvenle gömmek için kaçışlar: "<" (özellikle
 * "</script>" ve "<!--"), ve JS'de satır sonu sayılan U+2028/U+2029.
 * Sonuç hâlâ aynı değeri temsil eden geçerli JSON'dur.
 */
private fun scriptIcinGuvenliJson(json: String): String =
    json.replace("<", "\\u003c")
        .replace(" ", "\\u2028")
        .replace(" ", "\\u2029")

/**
 * Durak listesinden Leaflet tabanlı, sıralı pinli ve rota çizgili bir harita
 * HTML'i üretir. `ad` alanındaki tırnak/özel karakterlerin JS içine güvenli
 * kaçışlanması için manuel string birleştirme yerine kotlinx.serialization.json
 * kullanılır. [govdeFontuBase64] verilirse pin/popup metni uygulamanın gövde
 * fontuyla (Hanken Grotesk) çizilir; yoksa sistem sans'ına düşülür.
 */
fun rotaHaritasiHtmlOlustur(duraklar: List<RotaDurak>, govdeFontuBase64: String? = null): String {
    val duraklarJson = JsonArray(
        duraklar.map { durak ->
            buildJsonObject {
                put("lat", durak.enlem)
                put("lng", durak.boylam)
                put("ad", JsonPrimitive(durak.ad))
                put("sira", durak.sira)
            }
        },
    )
    val yerTutucular = linkedMapOf(
        "__LEAFLET_CSS_SRI__" to LEAFLET_CSS_SRI,
        "__LEAFLET_JS_SRI__" to LEAFLET_JS_SRI,
        "__GOVDE_FONTU__" to govdeFontuCss(govdeFontuBase64),
        "__FONT__" to "'$GOVDE_FONT_AILESI', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif",
        "__ZEMIN__" to KaranlikLacivert.css(),
        "__VURGU__" to FenerAlevi.css(),
        "__METIN__" to TasBeyazi.css(),
        "__IKINCIL__" to SisGrisi.css(),
        "__POPUP_ZEMIN__" to NotrGeceYuksek.css(),
        "__PIN_GOLGE__" to KaranlikLacivert.css(alfa = 0.85f),
        "__ATIF_ZEMIN__" to KaranlikLacivert.css(alfa = 0.75f),
        "__POPUP_KOSE__" to imzaKoseCss(),
    )
    var html = HTML_SABLONU
    yerTutucular.forEach { (anahtar, deger) -> html = html.replace(anahtar, deger) }
    // Veri EN SON yerleştirilir: bir mekan adı "__ZEMIN__" gibi bir yer
    // tutucu metni içerse bile değiştirilmez.
    return html.replace("__DURAKLAR_JSON__", scriptIcinGuvenliJson(duraklarJson.toString()))
}

/** Gövde fontu dosyası bir kez okunur ve oturum boyunca tutulur (~130 KB). */
private var govdeFontuOnbellek: String? = null

/**
 * [rotaHaritasiHtmlOlustur]'un gövde fontu gömülü hâli. Font dosyası
 * okunana kadar null döner (harita o sırada çizilmez; ikinci kez yüklenip
 * titremesin diye). Okuma başarısız olursa fontsuz HTML'e düşülür.
 */
@OptIn(ExperimentalEncodingApi::class)
@Composable
fun rememberRotaHaritasiHtml(duraklar: List<RotaDurak>): String? {
    val fontBase64 by produceState(govdeFontuOnbellek) {
        if (value == null) {
            val okunan = runCatching { Base64.encode(Res.readBytes("font/hanken_grotesk.ttf")) }.getOrDefault("")
            if (okunan.isNotEmpty()) govdeFontuOnbellek = okunan
            value = okunan
        }
    }
    return remember(duraklar, fontBase64) {
        fontBase64?.let { rotaHaritasiHtmlOlustur(duraklar, it.ifEmpty { null }) }
    }
}

/**
 * Tüm rotayı (kendi sırasıyla, optimize edilmeden) tek seferde Google
 * Maps'te açacak URL'i üretir. Origin yok — Maps kullanıcının canlı
 * konumundan başlatır. Son durak destination, aradakiler waypoints olur.
 */
fun tumRotaGoogleMapsUrl(duraklar: List<RotaDurak>): String {
    if (duraklar.isEmpty()) return "https://www.google.com/maps/dir/?api=1"

    val son = duraklar.last()
    val araDuraklar = duraklar.dropLast(1)

    val base = "https://www.google.com/maps/dir/?api=1" +
        "&destination=${son.enlem},${son.boylam}"
    val waypoints = if (araDuraklar.isNotEmpty()) {
        "&waypoints=" + araDuraklar.joinToString("|") { "${it.enlem},${it.boylam}" }
    } else {
        ""
    }
    return "$base$waypoints&travelmode=driving"
}

/**
 * Verilen HTML'i platforma özel bir web görünümünde ("gömme") render eder.
 * androidMain: WebView, iosMain: WKWebView, wasmJsMain: konumlandırılmış,
 * sandbox'lı <iframe>. Yükleme/desteklenmeme durumunda çökme yerine nazik
 * bir hata mesajı gösterilmelidir.
 */
@Composable
expect fun RotaHaritasiWebView(html: String, modifier: Modifier)
