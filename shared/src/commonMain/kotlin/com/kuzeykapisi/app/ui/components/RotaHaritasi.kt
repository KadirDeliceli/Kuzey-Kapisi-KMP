package com.kuzeykapisi.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kuzeykapisi.app.data.model.RotaDurak
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val HTML_SABLONU = """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
<style>html, body, #map { height: 100%; margin: 0; padding: 0; }</style>
</head>
<body>
<div id="map"></div>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script>
  var duraklar = __DURAKLAR_JSON__;
  var map = L.map('map');
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap katkıda bulunanlar',
    maxZoom: 19
  }).addTo(map);
  var noktalar = [];
  duraklar.forEach(function(d) {
    var marker = L.marker([d.lat, d.lng]).addTo(map);
    marker.bindPopup('<b>' + d.sira + '. ' + d.ad + '</b>');
    noktalar.push([d.lat, d.lng]);
  });
  if (noktalar.length > 1) {
    L.polyline(noktalar, {color: '#1C6178', weight: 4, opacity: 0.8}).addTo(map);
  }
  if (noktalar.length > 0) {
    map.fitBounds(noktalar, {padding: [30, 30]});
  } else {
    map.setView([41.0, 35.0], 6);
  }
</script>
</body>
</html>"""

/**
 * Durak listesinden Leaflet tabanlı, sıralı pinli ve rota çizgili bir harita
 * HTML'i üretir. `ad` alanındaki tırnak/özel karakterlerin JS içine güvenli
 * kaçışlanması için manuel string birleştirme yerine kotlinx.serialization.json
 * kullanılır.
 */
fun rotaHaritasiHtmlOlustur(duraklar: List<RotaDurak>): String {
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
    return HTML_SABLONU.replace("__DURAKLAR_JSON__", duraklarJson.toString())
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
 * androidMain: WebView, iosMain: WKWebView, wasmJsMain: konumlandırılmış
 * <iframe>. Yükleme/desteklenmeme durumunda çökme yerine nazik bir hata
 * mesajı gösterilmelidir.
 */
@Composable
expect fun RotaHaritasiWebView(html: String, modifier: Modifier)
