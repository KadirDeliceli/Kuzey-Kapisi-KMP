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
<style>
  /* Harita, uygulamanın gece denizi paletine oturur: karo katmanı hafifçe
     karartılıp doygunluğu düşürülür, üzerindeki rota ve pinler fener
     aleviyle öne çıkar. */
  html, body, #map { height: 100%; margin: 0; padding: 0; background: #0B1E2D; }
  .leaflet-tile-pane { filter: brightness(0.72) saturate(0.55) contrast(1.05); }
  .leaflet-container { background: #0B1E2D; }
  .kk-pin {
    width: 24px; height: 24px; border-radius: 50% 50% 50% 4px;
    background: #E8A33D; color: #0B1E2D;
    font: 600 12px/24px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    text-align: center; box-shadow: 0 0 0 2px rgba(11,30,45,0.85);
  }
  .leaflet-popup-content-wrapper, .leaflet-popup-tip {
    background: #16455A; color: #F2EFE7; border-radius: 10px 10px 10px 4px;
  }
  .leaflet-control-attribution { background: rgba(11,30,45,0.75) !important; color: #7C8B93 !important; }
  .leaflet-control-attribution a { color: #7C8B93 !important; }
</style>
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
    var ikon = L.divIcon({
      className: '',
      html: '<div class="kk-pin">' + d.sira + '</div>',
      iconSize: [24, 24],
      iconAnchor: [12, 24],
      popupAnchor: [0, -22]
    });
    var marker = L.marker([d.lat, d.lng], {icon: ikon}).addTo(map);
    marker.bindPopup('<b>' + d.sira + '. ' + d.ad + '</b>');
    noktalar.push([d.lat, d.lng]);
  });
  if (noktalar.length > 1) {
    L.polyline(noktalar, {color: '#E8A33D', weight: 3, opacity: 0.9}).addTo(map);
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
