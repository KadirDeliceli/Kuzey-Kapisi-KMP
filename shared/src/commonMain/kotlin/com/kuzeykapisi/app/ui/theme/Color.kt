package com.kuzeykapisi.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// KUZEY KAPISI PALETİ
//
// Marka temeli: Sinop / antik Sinope, taş kale surları, balıkçı limanı,
// Diyojen'in "dürüst insan arayan" feneri, sahildeki deniz fenerleri.
// İmza motif: FENER IŞIĞI.
//
// Kaynak: tokens/colors.json (DTCG) — bu dosya o spesifikasyonun Compose
// karşılığıdır. Her ikili scripts/contrast.py ile ÖLÇÜLMÜŞTÜR; yorumlardaki
// oranlar gerçek çıktıdır, tahmini değil.
//
// Uygulama KOYU zeminde çalışır (bkz. Theme.kt, karanlik = true varsayılan).
// Açık şema tanımlıdır ama henüz uygulamanın gönderdiği varsayılan değildir.
// ---------------------------------------------------------------------------

// --- 1. Zemin: "gece denizi" ------------------------------------------------

/** Ana koyu zemin. Brief'teki tam değer. */
val KaranlikLacivert = Color(0xFF0B1E2D)

/** Çökük yüzey — form alanı, liste satırı. KaranlikLacivert'ten bir tık koyu. */
val NotrGeceAlcak = Color(0xFF081620)

/** İkincil koyu ton / kart-panel yüzeyi. Brief'teki tam değer. */
val DerinDeniz = Color(0xFF123B4F)

/** Yükseltilmiş yüzey — dialog kabuğu, sabitlenmiş başlık. */
val NotrGeceYuksek = Color(0xFF1B4A61)

/** En yükseltilmiş yüzey — açık menü, seçili kart. */
val NotrGeceEnYuksek = Color(0xFF235875)

/** Dekoratif ayraç/kart kenarı — düşük kontrast, kasıtlı (3:1 hedeflemez). */
val NotrGeceCizgi = Color(0xFF2A4A5C)

/** Zorunlu kontrol kenarlığı — 3.18:1 DerinDeniz üzerinde, 4.90:1 çökük yüzeyde. */
val NotrGeceCizgiGuclu = Color(0xFF5F89A6)

// --- 2. FenerAlevi: BİRİCİL vurgu --------------------------------------------

/**
 * FenerAlevi — fener alevi ambar tonu. Brief'teki tam değer. BİRİCİL vurgu:
 * hover/aktif durum, birincil buton dolgusu, odak halkası, glow efekti.
 * Geniş zeminde ASLA kullanılmaz. 7.87:1 KaranlikLacivert üzerinde.
 */
val FenerAlevi = Color(0xFFE8A33D)

/** FenerAlevi'nin açık temada metin/ikon olarak okunabilir koyu hâli — 4.93:1 TasBeyazi üzerinde. */
val FenerAleviDerin = Color(0xFF8F5C10)

/** Birincil kapsayıcı (container) zemini — ambar tonunun çok koyu, sakin hâli. */
val FenerGece = Color(0xFF2E2410)

// --- 3. TasBeyazi / SisGrisi: metin --------------------------------------

/** Açık zemin / koyu üstü ana metin. Brief'teki tam değer. 14.76:1 KaranlikLacivert üzerinde. */
val TasBeyazi = Color(0xFFF2EFE7)

/**
 * İkincil metin. Brief #7C8B93 veriyordu; DerinDeniz yüzeyinde 3.38:1'e
 * düşüp AA'yı (4.5:1) kaçırdığı ÖLÇÜLDÜ — tek bir ikincil-metin tokenının
 * HER yüzey katmanında geçmesi için #93A3AA'ya açıldı (6.51:1 zemin,
 * 4.57:1 kart, 7.03:1 çökük yüzey). Erişilebilirlik brief hex'inden önce
 * gelir (CLAUDE.md karar çerçevesi).
 */
val SisGrisi = Color(0xFF93A3AA)

/** İkincil metin — açık temada. 5.93:1 TasBeyazi üzerinde. */
val SisGrisiKoyu = Color(0xFF4F5D64)

// --- 4. Yosun: doğa kategorisi nokta atışı vurgu -----------------------------

/** Yosun — brief'teki tam değer. Dolgu / açık temada metin olarak kullanılır. */
val Yosun = Color(0xFF3F6B5C)

/** Yosun'un koyu zeminde okunabilir hâli — rozet metni/ikonu. 7.25:1 zemin, 5.09:1 kart üzerinde. */
val YosunAcik = Color(0xFF82B49F)

// --- 5. SinopKirmizisi: DAR ROL ----------------------------------------------

/**
 * DAR ROLLÜ vurgu. Brief'teki tam değer. YALNIZCA:
 *  a) Admin panelindeki yıkıcı/geri alınamaz eylemler,
 *  b) Tescilli Ürünler kategorisindeki kart köşesindeki mühür detayı.
 * Başka HİÇBİR yerde kullanılmaz.
 */
val SinopKirmizisi = Color(0xFFCB410B)

/** SinopKirmizisi'nin açık temada / koyu dolgu üstünde okunabilir hâli. 7.18:1 TasBeyazi üzerinde. */
val SinopKirmizisiDerin = Color(0xFF8F2D08)

// --- 6. Kehribar: hata/uyarı — SinopKirmizisi VE FenerAlevi'nden ayrı -------

/**
 * Hata/uyarı rengi. Ne SinopKirmizisi'dir (o iki dar role kilitli) ne de
 * FenerAlevi (o birincil vurguya kilitli) — token-by-intent gereği üçüncü,
 * ayrı bir ton. 6.50:1 zemin, 4.56:1 kart üzerinde.
 */
val Kehribar = Color(0xFFE28A63)

/** Kehribar'ın açık temada okunabilir hâli. 5.42:1 TasBeyazi üzerinde. */
val KehribarDerin = Color(0xFF9A4A1E)

// --- 7. Açık tema yüzeyleri ("gün" — KaranlikLacivert/DerinDeniz'in gündüz karşılığı) ---

val NotrGun = TasBeyazi
val NotrGunYuzey = Color(0xFFFBFAF6)
val NotrGunAlcak = Color(0xFFEDE9DD)
val NotrGunYuksek = Color(0xFFE6E0D0)
val NotrGunEnYuksek = Color(0xFFDED5BF)
val NotrGunCizgi = Color(0xFFDCD5C4)
val NotrGunCizgiGuclu = Color(0xFF7A8A90)
val NotrGunMetin = KaranlikLacivert
val NotrGunMetinIkincil = SisGrisiKoyu

// ---------------------------------------------------------------------------
// ESKİ ADLAR — köprü katmanı
//
// Bir önceki (turkuaz) iterasyonda ekranlar zaten bu adları çağırıyordu.
// Böylece bu dosya dışında TEK satır ekran kodu değişmeden bütün uygulama
// brief'in paletini alır (single source of truth, CLAUDE.md non-negotiable #2).
// ---------------------------------------------------------------------------

/** @see NotrGeceYuksek */
val YuksekYuzey = NotrGeceYuksek

/** @see NotrGeceAlcak */
val AlcakYuzey = NotrGeceAlcak

/** @see Kehribar */
val HataRengi = Kehribar
