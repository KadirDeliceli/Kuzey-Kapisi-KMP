# Kuzey Kapısı

**Kuzey Kapısı**, Sinop'u tanıtan bağımsız, yapay zekâ destekli bir turizm
ve rehberlik uygulamasıdır. Uygulama; Sinop'un tarihini, kültürel mirasını,
yöresel mutfağını, doğal güzelliklerini ve tescilli değerlerini, konuya özel
yapay zekâ rehberleriyle birebir sohbet edebileceğiniz etkileşimli bir
deneyimle ziyaretçilere sunar.

## 1. Proje Özeti (Hakkında)

Ziyaretçi, ana ekrandaki başlıklardan birini seçer — **Tarih ve Kültür**,
**Lezzet ve Doğa**, **Akıllı Zaman ve Rota Düzenleyici** ya da **Tescilli
Ürünler** — ve o başlığa özgü bir karakterle (tarihî bir şahsiyet, bir usta
aşçı, bir doğa rehberi vb.) metin tabanlı bir sohbet başlatır. Sohbetler
FastAPI tabanlı bir backend (`api.py`) üzerinden yürütülen yapay zekâ
oturumlarıyla desteklenir; konum izni verildiğinde **Akıllı Rota
Düzenleyici**, kullanıcının anlık konumunu ve isteğini temel alarak kişisel
bir gezi rotası önerir.

Uygulama **Kotlin Multiplatform (KMP)** ve **Compose Multiplatform**
mimarisiyle geliştirilmiştir: arayüz (UI), gezinme (navigasyon), durum
yönetimi (ViewModel), ağ katmanı (Ktor) ve iş mantığının tamamı **tek bir
ortak Kotlin kod tabanında** (`shared` modülü, `commonMain` kaynak seti)
yazılır. Bu sayede Android, iOS ve Web (Kotlin/Wasm & Kotlin/JS) hedefleri
aynı ekranları, aynı davranışı ve aynı iş mantığını paylaşır; platforma özel
kod yalnızca uygulama giriş noktaları (Activity, iOS App, `main.kt`) ve ağ
motoru (Ktor engine: OkHttp / Darwin / Js) gibi ince bir katmanla sınırlı
kalır. Sonuç olarak tek bir mühendislik ekibi, tek bir kod değişikliğiyle üç
platformda da tutarlı bir kullanıcı deneyimi sağlar.

## 2. Temel Özellikler

- **Kategori bazlı yapay zekâ sohbetleri:** Tarih (kişiler), Kültür
  (mekânlar), Lezzetler ve Doğa kategorilerinde, her biri kendi karakterine
  sahip yapay zekâ rehberleriyle gerçek zamanlı sohbet.
- **Dinamik katalog:** Bot/karakter listeleri statik olarak gömülü değildir;
  uygulama açılışında backend'deki `/katalog` uç noktasından çekilir.
- **Akıllı Zaman ve Rota Düzenleyici:** Kullanıcının cihaz konumunu (izin
  verildiğinde) ve serbest metin isteğini alarak kişiselleştirilmiş bir gezi
  rotası öneren yapay zekâ destekli planlayıcı.
- **Tescilli Ürünler:** Sinop'a özgü coğrafi işaretli/tescilli değerleri
  tanıtan ayrı bir bölüm.
- **Dayanıklı oturum yönetimi:** Backend'in bellek içi (RAM) tuttuğu sohbet
  oturumu, sunucu yeniden başladığında 404 dönerse istemci tarafında
  sessizce yeni bir oturumla otomatik olarak yenilenir; kullanıcı hata
  görmez.
- **Görsel yükleme ve zarif geri düşüş (fallback):** Karakter/kart görselleri
  backend'den (`/gorseller/{kategori}/{kod}`) yüklenir; görsel yoksa veya
  404 dönerse arayüz kırılmadan varsayılan bir kapak görseline düşer.
- **Platformlar arası konum erişimi:** Android, iOS ve Web'de platforma özel
  `expect`/`actual` uygulamalarıyla konum izni isteme ve konum okuma.
- **Bilgilendirme diyalogları:** "Biz Kimiz" ve "Proje Hakkında"
  bilgilendirme pencereleri, üst gezinme çubuğundan erişilebilir.
- **Tek kod tabanından çoklu platform:** Aynı ekranlar, aynı gezinme akışı ve
  aynı iş mantığı Android, iOS, Kotlin/Wasm ve Kotlin/JS web hedeflerinde
  aynı şekilde çalışır.

## 3. Kullanılan Teknolojiler (Tech Stack)

| Katman | Teknoloji |
|---|---|
| Dil | Kotlin 2.4.10 |
| UI Çatısı | Compose Multiplatform 1.11.1 (Material 3) |
| Mimari | MVVM — sade Kotlin sınıfları + `StateFlow` (platform `androidx.lifecycle.ViewModel`'den bağımsız, `remember` ile Compose yaşam döngüsüne bağlanır) |
| Gezinme (Navigasyon) | Harici kütüphanesiz, `sealed interface Screen` tabanlı elle yazılmış durum makinesi |
| Ağ İstekleri | Ktor Client 3.1.0 (`ktor-client-core`, `content-negotiation`, `kotlinx-serialization-json`) — platforma göre `expect/actual` engine: Android → OkHttp, iOS → Darwin, Web (JS/Wasm) → Js |
| Serileştirme | `kotlinx.serialization` 1.8.0 |
| Eşzamanlılık | `kotlinx.coroutines` 1.10.1 |
| Görsel Yükleme | Coil 3.1.0 (`coil-compose`, `coil-network-ktor3`) — tam multiplatform (Android, iOS, Wasm) |
| Konum Servisleri | Android: Google Play Services Location 21.3.0; iOS/Web: platforma özel `expect/actual` konum uygulamaları |
| Bağımlılık Enjeksiyonu | Kullanılmıyor — nesneler doğrudan constructor ile (manuel/elle) oluşturulup birbirine geçiriliyor |
| Yerel Veritabanı | Kullanılmıyor — kalıcı yerel depolama yok, oturum durumu backend'de (RAM) ve uygulama içi `StateFlow`'da tutuluyor |
| Backend | Ayrı bir FastAPI servisi (`api.py`, bu repo dışında) — REST/JSON uç noktaları üzerinden tüketiliyor |
| Build Sistemi | Gradle (Kotlin DSL), Android Gradle Plugin 9.0.1, sürüm kataloğu (`gradle/libs.versions.toml`) |

## 4. Kurulum ve Çalıştırma (Getting Started)

### Ön Koşullar

- **Android Studio** (güncel, KMP eklentileriyle) veya **JetBrains Fleet**
- JDK 11+ (proje `JVM_11` hedefliyor)
- iOS hedefi için: **macOS + Xcode**
- Backend servisinin (`api.py`, FastAPI) ayrı olarak çalışır durumda olması
  ve `shared/src/commonMain/kotlin/com/kuzeykapisi/app/config/Config.kt`
  içindeki `BASE_URL` değerinin ortamınıza göre ayarlanmış olması
  (emülatör/simülatör/gerçek cihaz/canlı — dosya içindeki yorumlarda tüm
  seçenekler açıklanmıştır).

### Projeyi Açma

1. Depoyu klonlayın ve kök dizini Android Studio veya Fleet ile açın.
2. IDE'nin Gradle senkronizasyonunu otomatik başlatmasını bekleyin ya da
   manuel tetikleyin (Android Studio: *File → Sync Project with Gradle
   Files*).
3. `Config.kt` içindeki `BASE_URL`'i backend adresinize göre güncelleyin.

### Android'de Çalıştırma

- IDE'nin çalıştır (run) widget'ından `androidApp` yapılandırmasını seçip
  çalıştırın, veya:
  ```
  ./gradlew :androidApp:assembleDebug
  ```

### Web'de Çalıştırma (Kotlin/Wasm veya Kotlin/JS)

- Wasm hedefi (daha hızlı, modern tarayıcılar):
  ```
  ./gradlew :webApp:wasmJsBrowserDevelopmentRun
  ```
- JS hedefi (daha yavaş, eski tarayıcı desteği):
  ```
  ./gradlew :webApp:jsBrowserDevelopmentRun
  ```

### iOS'te Çalıştırma

- `iosApp/` dizinini Xcode ile açıp simülatör veya cihazda çalıştırın.
  Kotlin tarafı (`shared` modülü) `Shared.framework` olarak derlenip
  otomatik bağlanır.

### Testleri Çalıştırma

- Android testleri: `./gradlew :shared:testAndroidHostTest`
- Web testleri:
  - Wasm hedefi: `./gradlew :shared:wasmJsTest`
  - JS hedefi: `./gradlew :shared:jsTest`
- iOS testleri: `./gradlew :shared:iosSimulatorArm64Test`

## 5. Proje Yapısı (Directory Structure)

```
KuzeyKapisiApp/
├─ shared/                         # KMP çekirdek modülü — UI, iş mantığı, ağ katmanı (paylaşımlı kod)
│  └─ src/
│     ├─ commonMain/                # Tüm platformlarda ortak kod
│     │  ├─ kotlin/com/kuzeykapisi/app/
│     │  │  ├─ App.kt                 # Kök composable — Screen durum makinesi + sohbet overlay
│     │  │  ├─ config/Config.kt       # Backend BASE_URL yapılandırması
│     │  │  ├─ data/
│     │  │  │  ├─ model/               # kotlinx.serialization veri modelleri (Katalog, Oturum, Rota, ChatUiState, Konum...)
│     │  │  │  ├─ remote/              # ApiService (Ktor HttpClient sarmalayıcı) + HttpClientFactory (expect)
│     │  │  │  └─ repo/                # KuzeyRepository — tek veri kaynağı, güvenli sohbet mantığı
│     │  │  ├─ domain/Navigation.kt    # MAIN_CARDS — statik gezinme/kart tanımları
│     │  │  └─ ui/
│     │  │     ├─ components/          # CoverCard, TopBar, Footer, ChatSheet, InfoDialog, TypingIndicator, konum izin efekti (expect) ...
│     │  │     ├─ screens/              # HomeScreen, SubMenuScreen, BotListScreen, RotaScreen, WipScreen
│     │  │     ├─ theme/                # Color, Type, Theme (Material 3 renk paleti ve tipografi)
│     │  │     └─ vm/                   # CatalogViewModel, ChatViewModel, RotaViewModel (sade Kotlin + StateFlow)
│     │  └─ composeResources/         # Paylaşımlı görseller (logo, arka plan)
│     ├─ androidMain/                # Android'e özel: HttpClientFactory (OkHttp), konum/izin uygulamaları
│     ├─ iosMain/                    # iOS'e özel: HttpClientFactory (Darwin), MainViewController, konum uygulaması
│     ├─ jsMain/ , wasmJsMain/       # Web'e özel: HttpClientFactory (Js), konum uygulamaları
│     └─ commonTest/, androidHostTest/, iosTest/, webTest/   # Platform bazlı test kaynak setleri
│
├─ androidApp/                     # Android uygulama kabuğu (yalnızca giriş noktası)
│  └─ src/main/kotlin/.../MainActivity.kt   # setContent { App() }
│
├─ iosApp/                         # iOS uygulama kabuğu (Xcode projesi)
│  └─ iosApp/iOSApp.swift, ContentView.swift # ComposeUIViewController { App() } çağıran SwiftUI giriş noktası
│
├─ webApp/                         # Web uygulama kabuğu (Kotlin/Wasm & Kotlin/JS)
│  └─ src/webMain/kotlin/.../main.kt        # ComposeViewport(document.body!!) { App() }
│
├─ gradle/libs.versions.toml       # Merkezî bağımlılık/sürüm kataloğu
├─ settings.gradle.kts             # Gradle modül tanımları (:shared, :androidApp, :webApp)
└─ build.gradle.kts                # Kök proje derleme yapılandırması
```

**Modüllerin kısa işlevi:**

- **`shared`** — Projenin kalbi. UI (Compose), MVVM katmanları (View → sade
  ViewModel sınıfları → Repository → ApiService), veri modelleri ve
  platforma özel `expect/actual` uygulamaları (ağ engine'i, konum erişimi)
  burada yaşar. Android, iOS ve Web hedefleri bu modülü derleyip kullanır.
- **`androidApp`** — Yalnızca Android giriş noktasını (`MainActivity`) ve
  Android'e özgü manifest/kaynak dosyalarını barındıran ince bir kabuk;
  tüm ekran ve mantık `shared`'dan gelir.
- **`iosApp`** — Xcode projesi; `shared` modülünün ürettiği
  `Shared.framework`'ü SwiftUI üzerinden `ComposeUIViewController` ile
  gösteren ince bir kabuk.
- **`webApp`** — Kotlin/Wasm ve Kotlin/JS hedefleri için tarayıcı giriş
  noktası; `ComposeViewport` ile `shared` modülündeki `App()` composable'ını
  DOM'a bağlar.
