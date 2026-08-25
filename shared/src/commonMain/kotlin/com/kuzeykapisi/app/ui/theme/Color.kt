package com.kuzeykapisi.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// KUZEY KAPISI PALETİ
//
// Üç eksenli bir sistem:
//   1) MARKA — derin turkuaz / deniz mavisi. Tek birincil vurgu ailesi.
//   2) SICAK KUM — ikincil, tamamlayıcı sıcaklık. Rozet, ikincil buton,
//      "bilgi verir ama eylem değildir" nitelikli işaretler.
//   3) NÖTR — zemin ve yüzeyler. Doygun lacivert yerine, içinde çok az deniz
//      tonu taşıyan zarif nötrler; renk yükünü vurgulara bırakır.
//
// Her rol hem KOYU hem AÇIK tema için ayrı ayrı tanımlıdır (bkz. Theme.kt).
// Dosyanın sonundaki "eski adlar" bölümü, ekranlardaki mevcut çağrı yerlerini
// bozmadan yeni palete köprü kurar.
// ---------------------------------------------------------------------------

// --- 1. MARKA: turkuaz / deniz -------------------------------------------

/** Koyu tema birincil vurgusu — sığ suyun ışığı. Aktif/seçili durum, odak
 *  kenarı, birincil buton dolgusu. Geniş zeminde kullanılmaz. */
val Turkuaz = Color(0xFF3EC5BD)

/** Açık tema birincil vurgusu — aynı aile, açık zeminde okunabilir derinlikte. */
val TurkuazDerin = Color(0xFF0B6E71)

/** Marka ailesinin en koyu tonu: koyu temada birincil kapsayıcı (container),
 *  seçili satır zemini, ince marka çerçevesi. */
val TurkuazGece = Color(0xFF0A3A3E)

/** Açık temada birincil kapsayıcı — marka renginin çok açık, sakin hâli. */
val TurkuazSis = Color(0xFFCDE9E7)

// --- 2. SICAK KUM: ikincil ------------------------------------------------

/** İkincil marka tonu — dolgu olarak kullanıldığında üstüne açık metin gelir. */
val Kum = Color(0xFF9A7443)

/** [Kum]'un koyu zeminde okunabilir hâli: rozet, küçük ikon, sakin bilgi işareti. */
val KumAcik = Color(0xFFE2C08C)

/** Kum ailesinin en açık tonu — açık temada ikincil kapsayıcı zemini. */
val KumSis = Color(0xFFF2E3CC)

/** Kum ailesinin en koyu tonu — koyu temada ikincil kapsayıcı zemini. */
val KumGece = Color(0xFF3E2F1B)

// --- 3. NÖTRLER -----------------------------------------------------------

// Koyu tema: içinde bir tutam deniz tonu taşıyan, mürekkep gibi nötrler.
/** Koyu tema zemini. */
val NotrGece = Color(0xFF181F24)
/** Koyu tema temel yüzeyi (kart, panel). */
val NotrGeceYuzey = Color(0xFF101A1F)
/** Zemine yakın, hafifçe ayrışan yüzey: form alanı, liste satırı. */
val NotrGeceAlcak = Color(0xFF0D1418)
/** Yükseltilmiş yüzey: dialog, sabitlenmiş panel başlığı. */
val NotrGeceYuksek = Color(0xFF17242B)
/** En yükseltilmiş yüzey: menü, seçili kart. */
val NotrGeceEnYuksek = Color(0xFF1F2F37)
/** Koyu tema ayraç/kenarlık. */
val NotrGeceCizgi = Color(0xFF33454E)
/** Koyu tema ana metni. */
val NotrGeceMetin = Color(0xFFE8EFF1)
/** Koyu tema ikincil metni. */
val NotrGeceMetinIkincil = Color(0xFF97A9B2)

// Açık tema: kâğıt sıcaklığında, gri değil "taş" hissi veren nötrler.
/** Açık tema zemini. */
val NotrGun = Color(0xFFF6F7F5)
/** Açık tema temel yüzeyi. */
val NotrGunYuzey = Color(0xFFFFFFFF)
/** Zemine yakın ayrışan yüzey. */
val NotrGunAlcak = Color(0xFFF0F2F1)
/** Yükseltilmiş yüzey. */
val NotrGunYuksek = Color(0xFFE9EDEC)
/** En yükseltilmiş yüzey. */
val NotrGunEnYuksek = Color(0xFFE1E7E6)
/** Açık tema ayraç/kenarlık. */
val NotrGunCizgi = Color(0xFFC6D1D2)
/** Açık tema ana metni. */
val NotrGunMetin = Color(0xFF0C1417)
/** Açık tema ikincil metni. */
val NotrGunMetinIkincil = Color(0xFF53656C)

// --- 4. Sinyal renkleri ---------------------------------------------------

/**
 * DAR ROLLÜ vurgu. YALNIZCA iki bağlamda kullanılır:
 *  a) Admin panelindeki yıkıcı/geri alınamaz eylemler (sil ikonu, silme onay
 *     dialogunun birincil butonu ve vurgusu, "anlatımı kaldır" işaretliyken),
 *  b) Tescilli ürünler kategorisindeki kartların köşesindeki ince "mühür"
 *     detayı (resmi coğrafi işaret hissi).
 * Bunların DIŞINDA hiçbir yerde kullanılmaz — genel vurgu/hover rolleri
 * marka turkuazına aittir.
 */
val SinopKirmizisi = Color(0xFFD2492A)

/** [SinopKirmizisi]'nın açık temada okunabilir, biraz daha derin hâli. */
val SinopKirmizisiDerin = Color(0xFFA8371C)

/**
 * Hata/uyarı rengi. [SinopKirmizisi] DEĞİLDİR: kırmızı yalnızca yıkıcı admin
 * eylemlerine ayrılmıştır. Hatalar sakin ama fark edilir bir kehribar tonuyla
 * verilir — marka turkuazından net biçimde ayrışır.
 */
val Kehribar = Color(0xFFE9A93C)

/** [Kehribar]'ın açık zeminde okunabilir hâli. */
val KehribarDerin = Color(0xFF8F5E10)

// ---------------------------------------------------------------------------
// ESKİ ADLAR — köprü katmanı
//
// Ekranlar renkleri hâlâ bu adlarla çağırıyor. Adlar korunur, DEĞERLERİ yeni
// palete bağlanır; böylece tek satır ekran kodu değişmeden bütün uygulama yeni
// paleti alır. Yeni kod doğrudan yukarıdaki adları kullanmalıdır.
// ---------------------------------------------------------------------------

/** @see NotrGece */
val KaranlikLacivert = NotrGece

/** @see NotrGeceYuzey */
val DerinDeniz = NotrGeceYuzey

/** Birincil vurgu — artık fener alevi (kehribar) değil, marka turkuazı.
 *  @see Turkuaz */
val FenerAlevi = Turkuaz

/** @see NotrGeceMetin */
val TasBeyazi = NotrGeceMetin

/** @see NotrGeceMetinIkincil */
val SisGrisi = NotrGeceMetinIkincil

/** İkincil marka tonu — artık yosun yeşili değil, sıcak kum. @see Kum */
val Yosun = Kum

/** @see KumAcik */
val YosunAcik = KumAcik

/** @see NotrGeceYuksek */
val YuksekYuzey = NotrGeceYuksek

/** @see NotrGeceAlcak */
val AlcakYuzey = NotrGeceAlcak

/** @see Kehribar */
val HataRengi = Kehribar
