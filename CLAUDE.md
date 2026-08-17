# Kuzey Kapısı — Kotlin Multiplatform (Compose Multiplatform) Uygulaması

Bu dosya proje kökünde durur ve Claude Code her oturumda otomatik okur.

## 0. Ne inşa ediyoruz, kısaca

Mevcut FastAPI backend'ini (`api.py`) tüketen, **Android + iOS + Web**'de aynı
kod tabanından çalışan tek bir Compose Multiplatform uygulaması. UI, mimari,
akış ve içerik metinleri hâlihazırdaki Next.js web sitesiyle **birebir aynı**
olacak. Kod paylaşımı maksimize edilir: UI dahil neredeyse her şey
`commonMain`'de yazılır; platforma özel olan yalnızca giriş noktaları
(MainActivity, iOS App/ContentView, wasmJs main.kt) ve ağ motoru (Ktor engine)
gibi ince bir katmandır.

### Varsayımlar (yanlışsa düzelt, Claude Code adapte etsin)
- KMP projesi kmp.jetbrains.com sihirbazıyla oluşturuldu; ana modül adı
  muhtemelen `composeApp` (eskiyse `shared` olabilir) — **gerçek modül adını
  projede kontrol et**, aşağıdaki yollar örnek amaçlıdır. Kaynak setleri:
  `commonMain`, `androidMain`, `iosMain`, `wasmJsMain`.
- iOS hedefi Xcode gerektirir (`iosApp/` klasörü) — Claude Code Kotlin tarafını
  tam yazabilir, Xcode'da açıp simulator'da çalıştırmak kullanıcıya kalır.
- Web hedefi **Kotlin/Wasm (wasmJs)** ile Compose for Web'dir; ayrı bir
  Next.js projesi değildir — mevcut `kuzey-kapisi-frontend` (Next.js) projesi
  bu işten bağımsız kalır, dokunulmaz.

---

## 1. Backend API Sözleşmesi (değişmedi + yeni görsel endpoint'i)

Base URL yapılandırılabilir (bkz. §5).

| Uç | Metod | Gövde (JSON) | Yanıt (JSON) |
|---|---|---|---|
| `/` | GET | — | `{"durum":"çalışıyor"}` |
| `/katalog` | GET | — | `{ "kisiler": {"ad":String,"ogeler":[{"kod":String,"ad":String}]}, "mekanlar":{...}, "lezzetler":{...}, "doga":{...} }` |
| `/oturum/baslat` | POST | `{"kategori":String,"oge":String}` | `{"session_id":String,"baslik":String,"karsilama":String}` |
| `/sohbet` | POST | `{"session_id":String,"mesaj":String}` | `{"session_id":String,"cevap":String}` |
| `/oturum/kapat` | POST | `{"session_id":String}` | `{"durum":String,"session_id":String}` |
| `/gorseller/{kategori}/{kod}` | GET | — | **binary görsel** (jpg/png/webp) veya 404 |

- `kategori` ∈ `kisiler | mekanlar | lezzetler | doga` (+ görsellerde ayrıca `kart`)
- `oge`/`kod` = katalogtaki `kod` (aynı zamanda `.md` dosya adı ve görsel adı)
- Oturum hafızası backend'de **RAM'dedir**: API yeniden başlarsa `/sohbet` 404
  döner → istemci sessizce yeni oturum açıp mesajı tekrar dener (bkz. §6).
- **Görsel isteğinde uzantı YAZILMAZ**: `/gorseller/kisiler/alaaddin_keykubat`
  şeklinde istenir; backend hangi uzantı varsa (jpg/png/webp) onu bulup döner.
  Dosya yoksa 404 döner — istemci bunu `default_kapak.png`'e düşürür.

---

## 2. Mimari — MVVM, katmanlar `commonMain`'de paylaşılır

**Kural (zorunlu):**
- **View (Composable screen'ler):** yalnızca state gözler, olayları
  ViewModel'a iletir. İçinde ağ çağrısı YOK.
- **ViewModel:** state üretir (`StateFlow`), Repository'yi çağırır. Compose'a
  ya da platforma özel hiçbir referans içermez.
- **Repository:** tek veri kaynağıdır; Ktor `HttpClient` ile API'yi çağırır,
  `guvenliSohbet` gibi iş mantığını barındırır.
- **Model:** `kotlinx.serialization` ile işaretlenmiş sade veri sınıfları.

**ViewModel yaklaşımı — bilinçli tercih:** `androidx.lifecycle.ViewModel`'in
multiplatform desteği hâlâ olgunlaşma aşamasında (özellikle wasmJs'te). Bunun
yerine **sade Kotlin sınıfları + `StateFlow`** kullan; yaşam döngüsünü
Compose'un `remember`/`remember(key)` mekanizmasına bağla (aşağıda örnek).
Bu, üç platformda da güvenilir şekilde derlenir. Bunları mimari olarak yine
"ViewModel" diye adlandır (`CatalogViewModel`, `ChatViewModel` gibi) — sınıf
adı ve sorumluluk ayrımı MVVM'i korur, sadece platform ViewModel sınıfından
miras almıyoruz.

```kotlin
// commonMain
class ChatViewModel(
    private val repo: KuzeyRepository,
    private val kategori: String,
    private val oge: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    fun basla() { /* oturumBaslat çağır, state'i güncelle */ }
    fun gonder(mesaj: String) { /* guvenliSohbet çağır */ }
    fun temizle() { scope.launch { repo.oturumKapat(_state.value.sessionId ?: return@launch) } }
}
```
```kotlin
// Ekranda:
val vm = remember(kategori, oge) { ChatViewModel(repo, kategori, oge) }
val ui by vm.state.collectAsState()
DisposableEffect(vm) { vm.basla(); onDispose { vm.temizle() } }
```

### Navigasyon — sade state makinesi (web'deki `page.tsx` ile aynı mantık)
Voyager/Navigation-Compose-Multiplatform gibi ek bağımlılıklar **kullanma**;
web'de kanıtlanmış basit yaklaşımı tekrarla — bir üst seviye `sealed class`
ile ekran durumu:

```kotlin
sealed interface Screen {
    data object Home : Screen
    data class SubMenu(val mainId: String) : Screen
    data class BotList(val kategori: String, val baslik: String) : Screen
    data object Wip : Screen
}
```
Kök `App()` composable'ı `var screen by remember { mutableStateOf<Screen>(Screen.Home) }`
tutar, `when(screen)` ile ilgili ekranı çizer. Sohbet **ayrı bir overlay
state**'idir (`aktifBot: BotRef?`) — web'deki sağ çekmece / mobilde alttan
sheet mantığı; ana ekran akışının üstüne bindirilir, ayrı bir "route" değildir.

### Paket yapısı (`commonMain/kotlin/...`)
```
data/model/          # Katalog, KatalogKategori, KatalogOge, OturumBaslatYaniti,
                      #   SohbetYaniti, ChatUiState, Mesaj (kotlinx.serialization)
data/remote/
  ├─ ApiService.kt    # Ktor HttpClient sarmalayıcı (fonksiyon fonksiyon)
  └─ HttpClientFactory.kt  # expect/actual: platforma göre engine
data/repo/
  └─ KuzeyRepository.kt    # guvenliSohbet dahil tüm iş mantığı
domain/
  └─ Navigation.kt    # MAIN_CARDS, SubCard, MainCard (statik, web'deki navigation.ts karşılığı)
config/
  └─ Config.kt         # BASE_URL (bkz. §5)
ui/theme/              # Color.kt, Type.kt, Theme.kt
ui/components/         # CoverCard, TopBar, Footer, InfoDialog, TypingIndicator, ChatSheet
ui/screens/            # HomeScreen, SubMenuScreen, BotListScreen, WipScreen
ui/vm/                 # CatalogViewModel, ChatViewModel
App.kt                 # kök composable: Screen state machine + ChatSheet overlay
```

Platforma özel dosyalar (yalnızca giriş noktaları + Ktor engine):
```
androidMain/kotlin/.../MainActivity.kt        # setContent { App() }
androidMain/kotlin/.../HttpClientFactory.android.kt  # OkHttp engine
iosMain/kotlin/.../MainViewController.kt      # ComposeUIViewController { App() }
iosMain/kotlin/.../HttpClientFactory.ios.kt   # Darwin engine
wasmJsMain/kotlin/.../main.kt                 # ComposeViewport(document.body!!) { App() }
wasmJsMain/kotlin/.../HttpClientFactory.wasmJs.kt  # Js engine
```

---

## 3. Bağımlılıklar

`commonMain`:
- `io.ktor:ktor-client-core`, `ktor-client-content-negotiation`,
  `ktor-serialization-kotlinx-json`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `org.jetbrains.kotlinx:kotlinx-coroutines-core`
- `io.coil-kt.coil3:coil-compose`, `io.coil-kt.coil3:coil-network-ktor3`
  (Coil 3 gerçekten multiplatform'dur: Android + iOS + Wasm destekler)
- Compose Multiplatform: `compose.runtime`, `compose.foundation`,
  `compose.material3`, `compose.components.resources` (görsel/font için)

Platforma özel (yalnızca ilgili source set'e):
- `androidMain`: `io.ktor:ktor-client-okhttp`
- `iosMain`: `io.ktor:ktor-client-darwin`
- `wasmJsMain`: `io.ktor:ktor-client-js`

`expect`/`actual` ile her platformda kendi engine'ini veren tek fonksiyon:
```kotlin
// commonMain
expect fun httpEngine(): HttpClientEngineFactory<*>
// androidMain -> OkHttp, iosMain -> Darwin, wasmJsMain -> Js
```

---

## 4. Veri modelleri (`kotlinx.serialization`, snake_case eşleme)

```kotlin
@Serializable
data class KatalogOge(val kod: String, val ad: String)

@Serializable
data class KatalogKategori(val ad: String, val ogeler: List<KatalogOge>)

typealias Katalog = Map<String, KatalogKategori>

@Serializable
data class OturumBaslatIstek(val kategori: String, val oge: String)

@Serializable
data class OturumBaslatYaniti(
    @SerialName("session_id") val sessionId: String,
    val baslik: String,
    val karsilama: String,
)

@Serializable
data class SohbetIstek(@SerialName("session_id") val sessionId: String, val mesaj: String)

@Serializable
data class SohbetYaniti(@SerialName("session_id") val sessionId: String, val cevap: String)

@Serializable
data class OturumKapatIstek(@SerialName("session_id") val sessionId: String)
```

---

## 5. Yapılandırma: Base URL (tüm platformlar, tek yerden)

```kotlin
object Config {
    // GERÇEK CİHAZ (Android telefon / iOS gerçek cihaz, PC ile aynı ağda):
    // PC'nin LAN IP'si — `ipconfig` (Windows) ile bul, değişebilir.
    const val BASE_URL = "http://<PC_IP>:8000/"   // <-- güncel IP ile değiştir

    // Android EMÜLATÖRÜ:      "http://10.0.2.2:8000/"
    // iOS SIMULATOR:          "http://127.0.0.1:8000/"  (simulator host ağını
    //                          doğrudan paylaşır, NAT çevirisi GEREKMEZ — Android
    //                          emülatöründen farklı davranır, bunu unutma)
    // Web (tarayıcı, aynı makine): "http://127.0.0.1:8000/"
    // Web (tarayıcı, başka cihaz): PC'nin LAN IP'si
    // CANLI:                  "https://api.kuzeykapisi.example.com/"

    fun gorselUrl(kategori: String, kod: String) = "${BASE_URL}gorseller/$kategori/$kod"
}
```

> Tek bir sabit tüm platformlarda kullanılacağı için önerilen: geliştirme
> sırasında **gerçek cihaz/LAN IP** adresini yaz (zaten böyle test ediyoruz),
> emülatör/simulator'a geçince yorumdaki adresle değiştir.

### Android: cleartext + ağ izni (emülatör/gerçek cihaz için hâlâ gerekli)
`AndroidManifest.xml`'e `INTERNET` izni + `network_security_config.xml` (LAN
IP + `10.0.2.2` + `localhost` cleartext izinli) — önceki Android-only spec'te
olduğu gibi.

### iOS: App Transport Security
`Info.plist`'e (iosApp projesi) `NSAppTransportSecurity` altında
`NSAllowsArbitraryLoads = true` (yalnızca dev; canlıda HTTPS ile kaldırılır)
veya spesifik olarak PC'nin IP'si için istisna eklenir.

### Web (wasmJs): CORS
Tarayıcı CORS'u zorunlu uygular. Backend `api.py`'deki
`CORSMiddleware(allow_origins=[...])` listesine Compose Web dev server'ının
origin'i eklenmeli (genelde `http://localhost:8080`; gerçek portu ilk
`./gradlew wasmJsBrowserDevelopmentRun` çalıştırmasında konsoldan doğrula).
Dev'de basitlik için `allow_origins=["*"]` kullanılabilir (bkz. üstteki
endpoint eklentisi notu).

---

## 6. Repository + Güvenli Sohbet (404 → sessiz yenileme)

```kotlin
data class SohbetSonuc(val cevap: String, val sessionId: String, val yenilendi: Boolean)

class KuzeyRepository(private val api: ApiService) {
    suspend fun katalog(): Katalog = api.katalog()
    suspend fun oturumBaslat(kategori: String, oge: String) = api.oturumBaslat(kategori, oge)
    suspend fun oturumKapat(sessionId: String) = runCatching { api.oturumKapat(sessionId) }

    suspend fun guvenliSohbet(kategori: String, oge: String, sessionId: String, mesaj: String): SohbetSonuc {
        return try {
            val y = api.sohbet(sessionId, mesaj)
            SohbetSonuc(y.cevap, sessionId, yenilendi = false)
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 404) {
                val yeni = api.oturumBaslat(kategori, oge)
                val y = api.sohbet(yeni.sessionId, mesaj)
                SohbetSonuc(y.cevap, yeni.sessionId, yenilendi = true)
            } else throw e
        }
    }
}
```
`ChatViewModel` davranışı web'dekiyle birebir aynı: açılışta karşılamayı ilk
bot mesajı yap; `yenilendi == true` ise araya "Bağlantı yenilendi — sohbet
geçmişi sıfırlandı." sistem notu ekle; kapanışta `oturumKapat` çağır.

---

## 7. Görsel yükleme + fallback (Coil3, tüm platformlarda aynı kod)

```kotlin
AsyncImage(
    model = Config.gorselUrl(kategori, kod),
    contentDescription = null,
    contentScale = ContentScale.Crop,
    error = painterResource(Res.drawable.default_kapak),
    placeholder = painterResource(Res.drawable.default_kapak),
    modifier = Modifier.fillMaxSize(),
)
```

`default_kapak.png` dosyasını (daha önce sana verdiğim aynı dosya)
`composeApp/src/commonMain/composeResources/drawable/default_kapak.png`
konumuna koy (Compose Multiplatform kaynak sistemi — üç platformda da otomatik
`Res.drawable.default_kapak` olarak erişilir, ayrıca hiçbir şey yapmana gerek yok).

Kart kapakları için de aynı fonksiyon kullanılır: `Config.gorselUrl("kart", "tarih-kultur")`
gibi — `gorseller/kart/` klasörü boşsa hepsi sessizce `default_kapak`'a düşer,
uygulama kırılmaz.

---

## 8. Navigasyon Akışı (web ile birebir — değişmedi)

```
Ana Sayfa (3 ana kart)
├─ KART 1: "Tarih ve Kültür"  → Alt menü: [Tarih, Kültür]
│    ├─ Tarih   → kisiler bot listesi  → bota dokun → Sohbet
│    └─ Kültür  → mekanlar bot listesi → Sohbet
├─ KART 2: "Lezzet ve Doğa"   → Alt menü: [Lezzetler, Doğa]
│    ├─ Lezzetler → lezzetler bot listesi → Sohbet
│    └─ Doğa      → doga bot listesi      → Sohbet
└─ KART 3: "Akıllı Zaman ve Rota Düzenleyici" → "Yapım Aşamasında"
```
Üst çubukta kurumsal kimlik + Biz Kimiz / Proje Hakkında (dialog). Ana
sayfanın altında footer. Bot listeleri **statik değil**, `/katalog`'dan gelir.

### `domain/Navigation.kt` (web'deki `navigation.ts` karşılığı)
```kotlin
data class SubCard(val id: String, val ad: String, val kategori: String, val kapak: String)
enum class MainCardType { SUBMENU, WIP }
data class MainCard(
    val id: String, val ad: String, val altBaslik: String,
    val kapak: String, val type: MainCardType, val subs: List<SubCard> = emptyList()
)

val MAIN_CARDS = listOf(
    MainCard("tarih-kultur", "Tarih ve Kültür", "Tarihî Keşif", "tarih-kultur", MainCardType.SUBMENU, listOf(
        SubCard("tarih", "Tarih", "kisiler", "tarih"),
        SubCard("kultur", "Kültür", "mekanlar", "kultur"),
    )),
    MainCard("lezzet-doga", "Lezzet ve Doğa", "Tat & Tabiat", "lezzet-doga", MainCardType.SUBMENU, listOf(
        SubCard("lezzetler", "Lezzetler", "lezzetler", "lezzetler"),
        SubCard("doga", "Doğa", "doga", "doga"),
    )),
    MainCard("akilli-rota", "Akıllı Zaman ve Rota Düzenleyici", "Planlayıcı", "akilli-rota", MainCardType.WIP),
)
```

---

## 9. Tema (web paletiyle birebir aynı)

```
deniz    #0F2E42  (birincil / kurumsal lacivert)
petrol   #1C6178  (ikincil / vurgu mavi)
pirinc   #AD7A3C  (üçüncül / pirinç vurgu — az kullan)
kagit    #EDEEE9  (arka plan — taş/kağıt)
kagit2   #F6F6F2  (yüzey — kart zemini)
murekkep #14252E  (ana metin)
sur      #5C6B71  (ikincil metin)
```
Material3 `lightColorScheme` ile eşle. Açık tema yeterli.

**Tipografi:** Google Fonts downloadable provider Android'e özeldir, üç
platformda çalışmaz. Bunun yerine **font dosyalarını bundle et**:
Newsreader ve Hanken Grotesk TTF dosyalarını (varsa) `composeApp/src/commonMain/
composeResources/font/` altına koy, `FontFamily(Font(Res.font.newsreader_regular))`
ile tanımla. Font dosyaları elde yoksa (Claude Code internet erişimiyle
otomatik indiremiyorsa) **`FontFamily.Serif`** (başlıklar) ve
**`FontFamily.Default`** (gövde) ile devam et — görsel olarak yakın bir sonuç
verir, sonradan gerçek fontlarla değiştirilir. Bunu engelleyici bir hata
haline getirme.

CoverCard: kapak görseli + üstünde yarı saydam beyaz overlay
(`Color.White.copy(alpha = 0.45f)`, alta doğru koyulaşan gradient) + sol-altta
küçük etiket (opsiyonel, petrol) + koyu lacivert (`murekkep`) başlık.
Köşe yuvarlama ~16dp, hafif gölge.

---

## 10. Ekran İçerikleri (metinler web ile birebir aynı)

**Hero:** "SİNOP · KUZEY KAPISI" etiketi, başlık *"Karadeniz'in kuzey
kapısında, her başlığın bir anlatıcısı var."*, alt metin *"Bir başlık seçin;
tarihî bir şahsiyet, bir usta aşçı ya da bir doğa rehberi Sinop'u size kendi
diliyle anlatsın."*

**Biz Kimiz:** "Kuzey Kapısı, Kuzey Anadolu Kalkınma Ajansı (KUZKA) Sinop
Yatırım Destek Ofisi bünyesinde yürütülen bölgesel bir turizm ve yapay zeka
rehberlik projesidir. Amacımız; Sinop'un tarihini, kültürel mirasını, yöresel
mutfağını ve doğal güzelliklerini çağdaş bir dijital deneyimle ziyaretçilere
ulaştırmaktır."

**Proje Hakkında:** "Kuzey Kapısı, Sinop'u dört başlık altında keşfe açar:
tarihî şahsiyetler, kültürel mekânlar, yöresel lezzetler ve doğal
güzellikler. Her başlık, o konuya özel bir yapay zeka rehberiyle sohbet etme
imkânı sunar."

**WIP ekranı:** etiket "AKILLI ZAMAN VE ROTA DÜZENLEYİCİ", başlık **"Yapım
Aşamasında"**, açıklama: *"Bu araç üzerinde çalışıyoruz. Gezinizi güne ve
saate göre planlayan akıllı rota düzenleyici çok yakında burada olacak."*,
"Başlıklara dön" butonu.

**Footer:** Adres: Sinop, Gerze · Telefon: +90 530 000 00 00 · E-posta:
mail@gmail.com · "© <yıl> KUZKA Sinop YDO"

---

## 11. Kabul Kriterleri

1. `commonMain` derlenir; Android hedefi emülatör/cihazda çalışır.
2. Web hedefi (`wasmJs`) tarayıcıda açılır ve aynı UI'yi gösterir.
3. iOS Kotlin tarafı derlenir (Xcode'da çalıştırma kullanıcıya kalabilir).
4. Ana sayfa → alt menü → bot listesi (**katalogdan**, statik değil) → sohbet
   akışı üç platformda da çalışır.
5. Görsel `/gorseller/...`'dan yüklenir; yoksa/404'te `default_kapak` görünür,
   kırık ikon olmaz.
6. Backend 404 verdiğinde sohbet sessizce yeni oturumla devam eder.
7. `Config.BASE_URL` tek satır değiştirilerek emülatör/simulator/canlıya geçilir.
8. Kart 3 → "Yapım Aşamasında"; Biz Kimiz/Proje Hakkında dialogları açılır.

## 12. Yol Boyunca Notlar
- **Android emülatör** `10.0.2.2`, **iOS simulator** `127.0.0.1` (farklı!),
  **gerçek cihaz** PC'nin LAN IP'si + backend `--host 0.0.0.0` + firewall izni.
- Web hedefinde CORS zorunlu — `allow_origins` listesine dev server origin'i ekle.
- Ağ/JSON işlemleri `Dispatchers.Default`/IO'da; UI thread'i bloklama.
- MVVM katman ayrımını koru: screen ağ bilmez, ViewModel Compose bilmez,
  Repository tek veri kaynağıdır.
