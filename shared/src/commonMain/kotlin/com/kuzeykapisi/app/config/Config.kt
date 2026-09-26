package com.kuzeykapisi.app.config

object Config {
    // GERÇEK CİHAZ (Android telefon / iOS gerçek cihaz, PC ile aynı ağda):
    // PC'nin LAN IP'si — `ipconfig` (Windows) ile bul, değişebilir.
    const val BASE_URL = "https://kuzey-kapisi-sinop.onrender.com/"
    //const val BASE_URL = "http://192.168.1.xxx:8000/"

    // Android EMÜLATÖRÜ:      "http://10.0.2.2:8000/"
    // iOS SIMULATOR:          "http://127.0.0.1:8000/"  (simulator host ağını
    //                          doğrudan paylaşır, NAT çevirisi GEREKMEZ — Android
    //                          emülatöründen farklı davranır, bunu unutma)
    // Web (tarayıcı, aynı makine): "http://127.0.0.1:8000/"
    // Web (tarayıcı, başka cihaz): PC'nin LAN IP'si
    // CANLI:                  "https://api.kuzeykapisi.example.com/"

    fun gorselUrl(kategori: String, kod: String) = "${BASE_URL}gorseller/$kategori/$kod"
}
