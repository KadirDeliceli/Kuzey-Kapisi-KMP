package com.kuzeykapisi.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Kuzey Kapısı paleti — "gece denizi" zemin + sıcak fener ışığı vurgusu.
// Eski deniz/petrol/pirinç/kağıt paletinin TAMAMEN yerine geçer.
// ---------------------------------------------------------------------------

/** Ana koyu zemin (gece denizi). */
val KaranlikLacivert = Color(0xFF0B1E2D)

/** İkincil koyu ton / kart ve panel yüzeyi. */
val DerinDeniz = Color(0xFF123B4F)

/**
 * BİRİNCİL vurgu — fener alevi. KISITLI kullanılır: aktif/seçili durum,
 * hover/basılı kenar çizgisi, imza glow efekti, birincil buton dolgusu.
 * Geniş alanlarda (arka plan, kart zemini) ASLA kullanılmaz; nadirliği
 * etkisini korur.
 */
val FenerAlevi = Color(0xFFE8A33D)

/** Açık zemin / koyu üstü ana metin. */
val TasBeyazi = Color(0xFFF2EFE7)

/** İkincil metin. */
val SisGrisi = Color(0xFF7C8B93)

/** Nokta atışı vurgu (doğa kategorisi, küçük rozetler). */
val Yosun = Color(0xFF3F6B5C)

/**
 * [Yosun]'un koyu zemin üzerinde okunabilir kalması için açılmış hâli — aynı
 * renk ailesi, yeni bir palet rengi değil. Küçük ikon/rozet gibi ince
 * işaretlerde kullanılır.
 */
val YosunAcik = Color(0xFF6FA48F)

/**
 * DAR ROLLÜ vurgu. YALNIZCA iki bağlamda kullanılır:
 *  a) Admin panelindeki yıkıcı/geri alınamaz eylemler (sil ikonu, silme onay
 *     dialogunun birincil butonu ve vurgusu, "anlatımı kaldır" işaretliyken),
 *  b) Tescilli ürünler kategorisindeki kartların köşesindeki ince "mühür"
 *     detayı (resmi coğrafi işaret hissi).
 * Bunların DIŞINDA hiçbir yerde kullanılmaz — genel vurgu/hover rolleri
 * [FenerAlevi]'ne aittir.
 */
val SinopKirmizisi = Color(0xFFCB410B)

// --- Paletten türetilen yardımcı tonlar (yeni renk değil, aynı ailenin
// --- yükseltilmiş/alçaltılmış yüzeyleri) ---------------------------------

/** Yükseltilmiş yüzey: dialog, sabitlenmiş panel başlığı. */
val YuksekYuzey = Color(0xFF16455A)

/** Zemine yakın, hafifçe ayrışan yüzey: form alanı, liste satırı zemini. */
val AlcakYuzey = Color(0xFF0F2C3D)

/**
 * Hata/uyarı metinlerinin rengi. Kırmızı DEĞİLDİR: [SinopKirmizisi] yalnızca
 * yıkıcı admin eylemlerine ayrılmıştır. Hatalar sakin ama fark edilir bir
 * "dikkat" tonuyla, fener aleviyle verilir.
 */
val HataRengi = FenerAlevi
