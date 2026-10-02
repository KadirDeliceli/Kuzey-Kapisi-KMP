package com.kuzeykapisi.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.remote.OturumSonlanmaNedeni
import com.kuzeykapisi.app.domain.AnlatimKaynagi
import com.kuzeykapisi.app.domain.BotRef
import com.kuzeykapisi.app.domain.MainCardType
import com.kuzeykapisi.app.domain.Screen
import com.kuzeykapisi.app.domain.SubCard
import com.kuzeykapisi.app.domain.adminEkrani
import com.kuzeykapisi.app.log.Logger
import com.kuzeykapisi.app.ui.components.ACILIS_BILGILENDIRME_METNI
import com.kuzeykapisi.app.ui.components.AdminGirisDialog
import com.kuzeykapisi.app.ui.components.BIZ_KIMIZ_METNI
import com.kuzeykapisi.app.ui.components.ChatSheet
import com.kuzeykapisi.app.ui.components.Footer
import com.kuzeykapisi.app.ui.components.InfoDialog
import com.kuzeykapisi.app.ui.components.PROJE_HAKKINDA_METNI
import com.kuzeykapisi.app.ui.components.TopBar
import com.kuzeykapisi.app.ui.kurulumYapImageLoader
import com.kuzeykapisi.app.ui.nav.LocalVmDeposu
import com.kuzeykapisi.app.ui.nav.VmDeposu
import com.kuzeykapisi.app.ui.nav.VmKapsami
import com.kuzeykapisi.app.ui.nav.rememberEkranYigini
import com.kuzeykapisi.app.ui.screens.AdminAnaSayfaScreen
import com.kuzeykapisi.app.ui.screens.AnlatimEkrani
import com.kuzeykapisi.app.ui.screens.BotListScreen
import com.kuzeykapisi.app.ui.screens.HomeScreen
import com.kuzeykapisi.app.ui.screens.PersonaDuzenleScreen
import com.kuzeykapisi.app.ui.screens.PersonaEkleScreen
import com.kuzeykapisi.app.ui.screens.PersonaOnizlemeEkrani
import com.kuzeykapisi.app.ui.screens.PersonaYonetScreen
import com.kuzeykapisi.app.ui.screens.RotaScreen
import com.kuzeykapisi.app.ui.screens.RotaYerDuzenleScreen
import com.kuzeykapisi.app.ui.screens.RotaYerEkleScreen
import com.kuzeykapisi.app.ui.screens.RotaYerYonetScreen
import com.kuzeykapisi.app.ui.screens.SubMenuScreen
import com.kuzeykapisi.app.ui.theme.KapiGecisi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KuzeyKapisiTheme
import com.kuzeykapisi.app.ui.theme.Opaklik
import com.kuzeykapisi.app.ui.theme.TELEFON_KIRILIMI
import com.kuzeykapisi.app.ui.theme.Yukseklik
import com.kuzeykapisi.app.ui.vm.AdminViewModel
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.sinop_arkaplan
import org.jetbrains.compose.resources.painterResource

/** Açık sohbetin hangi bot için olduğu, döndürmede korunmak üzere "kategori\nkod" olarak saklanır. */
private val BotRefSaver = Saver<BotRef?, String>(
    save = { it?.let { b -> "${b.kategori}\n${b.kod}" } },
    restore = { kayit -> kayit.split('\n').takeIf { it.size == 2 }?.let { BotRef(it[0], it[1]) } },
)

private enum class DialogTuru { YOK, BIZ_KIMIZ, PROJE_HAKKINDA }

/** Bu genişliğin altında sohbet tam ekran, üstünde sağdan dar panel olarak açılır. */
private val CHAT_GENIS_EKRAN_ESIGI = TELEFON_KIRILIMI

/** Geniş ekranda sohbet panelinin sabit genişliği. */
private val CHAT_PANEL_GENISLIGI = 400.dp

// BackHandler, CMP 1.11'de @ExperimentalComposeUiApi ve "NavigationEventHandler
// kullanın" diye @Deprecated işaretli. Yerine gelen navigationevent-compose
// paketinin bu sürümde yalnızca -android varyantı çözülüyor (wasm/iOS yok), bu
// yüzden şimdilik CMP'nin kendi sağladığı BackHandler kullanılıyor.
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
fun App() {
    remember {
        kurulumYapImageLoader()
        Logger.d { "Config.BASE_URL = ${Config.BASE_URL}" }
        Unit
    }
    // Uygulama düzeyi ViewModel'ler (Activity'nin store'unda, döndürmeden sağ
    // çıkar): repository + ekran kapsamlarının store'ları, ve admin oturumu.
    val vmDeposu = viewModel { VmDeposu() }
    val repo = vmDeposu.repo
    // Admin: token yalnızca bellekte, tek kaynakta (AdminOturumu) tutulur;
    // HttpClient onu /admin/... isteklerine kendisi ekler. Ekranlar token görmez.
    val adminOturumu = vmDeposu.adminOturumu
    val adminVm = viewModel { AdminViewModel(repo, adminOturumu) }
    val adminAcik by adminOturumu.acik.collectAsState()

    // Ekran geçmişi bir yığın olarak tutulur: yeni ekrana geçişte push, geri
    // gidişte pop. Böylece geri adımı her zaman SADECE bir üst seviyeye çıkar.
    // rememberSaveable: döndürmede aynı derinlikte geri kurulur.
    val ekranYigini = rememberEkranYigini()
    // Yığın geri kurulduğunda token yoksa (Android'de süreç öldürülüp yeniden
    // açıldıysa; token kasıtlı olarak kalıcı değil) admin ekranları atılır.
    // Döndürmede token korunduğu için burası hiçbir şey yapmaz.
    remember {
        if (!adminOturumu.acik.value) ekranYigini.kaldir { it.adminEkrani }
        Unit
    }
    var aktifBot by rememberSaveable(stateSaver = BotRefSaver) { mutableStateOf<BotRef?>(null) }
    var dialogTuru by remember { mutableStateOf(DialogTuru.YOK) }
    // Açılış bilgilendirme dialog'u: yalnızca uygulama bu oturumda ilk kez
    // render edildiğinde gösterilir; kart/ekran geçişlerinde ve döndürmede
    // tekrar açılmaz.
    var acilisBilgilendirmeAcik by rememberSaveable { mutableStateOf(true) }
    // Yalnızca GÖRSEL geçişin yönü: "kapı açılma" animasyonunun hangi tarafa
    // işleyeceğini söyler. Navigasyon kararlarına HİÇBİR etkisi yoktur.
    var gecisIleri by remember { mutableStateOf(true) }

    var adminGirisDialoguAcik by remember { mutableStateOf(false) }

    val git: (Screen) -> Unit = { hedef ->
        // Oturum yokken hiçbir admin ekranı yığına girmez; yerine giriş istenir.
        if (hedef.adminEkrani && !adminOturumu.acik.value) {
            adminVm.hataTemizle()
            adminGirisDialoguAcik = true
        } else {
            gecisIleri = true
            ekranYigini.ekle(hedef)
        }
    }
    val geriGit: () -> Unit = {
        if (ekranYigini.boyut > 1) {
            gecisIleri = false
            ekranYigini.cikar()
        }
    }
    // Admin ekranları 401 görünce bunu çağırır. HttpClient oturumu zaten
    // kapatmış olur; asıl temizlik aşağıdaki sonlanma dinleyicisinde.
    val onAdminYetkisiz: () -> Unit = { adminVm.yetkisizBildir() }

    // Oturum NASIL biterse bitsin (401, "Çıkış yap", hareketsizlik) tek yol:
    // yığındaki TÜM admin ekranları TEK SEFERDE atılır ve Ana Sayfa'ya dönülür;
    // kullanıcı token'sız bir ara admin ekranında kalamaz. Çıkış dışındaki
    // nedenlerde giriş dialogu, nedeni söyleyen bir mesajla açılır.
    LaunchedEffect(adminOturumu) {
        adminOturumu.sonlanma.collect { neden ->
            if (ekranYigini.boyut > 1) gecisIleri = false
            ekranYigini.kaldir { true }
            if (neden != OturumSonlanmaNedeni.CIKIS) adminGirisDialoguAcik = true
        }
    }

    KuzeyKapisiTheme {
        CompositionLocalProvider(LocalVmDeposu provides vmDeposu) {
        Surface(modifier = Modifier.fillMaxSize(), color = KaranlikLacivert) {
            Box(modifier = Modifier.fillMaxSize()) {
                // En alt katman: sabit arka plan fotoğrafı. Üstündeki yüksek
                // opaklıklı gece denizi katmanıyla birlikte, göz yormayan hafif
                // buğulu bir doku olarak hissedilir. Not: Modifier.blur()
                // Android API 31 altında sessizce devre dışı kalır (minSdk=24);
                // o cihazlarda yalnızca opaklık katmanı devreye girer.
                Image(
                    painter = painterResource(Res.drawable.sinop_arkaplan),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(7.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KaranlikLacivert.copy(alpha = Opaklik.YUZDE90)),
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        onBizKimizClick = { dialogTuru = DialogTuru.BIZ_KIMIZ },
                        onProjeHakkindaClick = { dialogTuru = DialogTuru.PROJE_HAKKINDA },
                        onAdminIkonClick = { git(Screen.AdminAnaSayfa) },
                    )
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        // İMZA GEÇİŞ: ekran hiyerarşisinde derine inerken içerik
                        // ortadan açılarak gelir, geri dönerken kapı kapanır gibi
                        // ortaya doğru kapanır. Hangi ekranın çizileceği kararı
                        // aşağıdaki `when` ile, önceki hâliyle birebir aynıdır.
                        // Genişlik sınırı ekran başına uygulanır: ana sayfa ve kart listesi
                        // tam genişlikte (kenardan kenara hero/ızgara), diğer ekranlar 1100dp'de kalır.
                        // Sınır her ekran örneğinin kendi kutusunda olduğu için geçiş
                        // sırasında genişlik sıçramaz.
                        //
                        // Her yığın girdisi kendi ViewModel kapsamında çizilir:
                        // girdi yığından çıkıp çıkış animasyonu bitince kapsam
                        // temizlenir (ViewModel'ler onCleared, istekler iptal);
                        // döndürmede girdi yığında kaldığı için korunur.
                        KapiGecisi(
                            hedef = ekranYigini.ust,
                            ileri = gecisIleri,
                            modifier = Modifier.fillMaxSize(),
                        ) { girdi ->
                            VmKapsami(
                                anahtar = "ekran-${girdi.id}",
                                halaGerekli = { ekranYigini.iceriyor(girdi.id) },
                            ) {
                            val s = girdi.ekran
                            // İleri navigasyon yalnızca EN ÜSTTEKİ ekrandan kabul
                            // edilir. Geri basıldıktan sonra ekran çıkış
                            // animasyonu boyunca (~380ms) hâlâ çizilir ve
                            // ViewModel'i o süre boyunca yaşar; o pencerede biten
                            // bir istek (ör. "Düzenle") artık görünmeyen ekrandan
                            // yeni ekran açmasın.
                            val git: (Screen) -> Unit = { hedef -> if (ekranYigini.ustMu(girdi.id)) git(hedef) }
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                Box(
                                    modifier = if (s is Screen.Home || s is Screen.BotList) {
                                        Modifier.fillMaxSize()
                                    } else {
                                        Modifier.fillMaxHeight().widthIn(max = 1100.dp)
                                    },
                                ) {
                                    // Guard: oturum kapalıyken hiçbir admin ekranı (ve
                                    // ViewModel'i) oluşturulmaz, dolayısıyla token'sız
                                    // tek bir admin isteği bile başlatılamaz. Yığın
                                    // aynı karede sonlanma dinleyicisi tarafından
                                    // temizlenir.
                                    if (s.adminEkrani && !adminAcik) return@Box
                                    when (s) {
                                        is Screen.Home -> HomeScreen(
                                            onKartTiklandi = { kart ->
                                                git(
                                                    when (kart.type) {
                                                        MainCardType.SUBMENU -> Screen.SubMenu(kart)
                                                        MainCardType.ROTA_PLANLAYICI -> Screen.Rota
                                                        MainCardType.DIRECT -> Screen.BotList(
                                                            kategori = "tescil",
                                                            baslik = kart.ad,
                                                        )
                                                    }
                                                )
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.SubMenu -> SubMenuScreen(
                                            mainCard = s.mainCard,
                                            onGeri = geriGit,
                                            onSubTiklandi = { sub: SubCard ->
                                                git(Screen.BotList(kategori = sub.kategori, baslik = sub.ad))
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.BotList -> BotListScreen(
                                            repo = repo,
                                            kategori = s.kategori,
                                            baslik = s.baslik,
                                            onGeri = geriGit,
                                            onBotTiklandi = { oge: KatalogOge ->
                                                git(
                                                    Screen.PersonaOnizleme(
                                                        kategori = s.kategori,
                                                        kod = oge.kod,
                                                        ad = oge.ad,
                                                        anlatimVar = oge.anlatimVar,
                                                    ),
                                                )
                                            },
                                            onSesTiklandi = { oge: KatalogOge ->
                                                git(
                                                    Screen.Anlatim(
                                                        kaynak = AnlatimKaynagi.Persona(kategori = s.kategori, kod = oge.kod),
                                                        baslik = oge.ad,
                                                    ),
                                                )
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.Anlatim -> AnlatimEkrani(
                                            repo = repo,
                                            kaynak = s.kaynak,
                                            baslik = s.baslik,
                                            onGeri = geriGit,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.PersonaOnizleme -> PersonaOnizlemeEkrani(
                                            repo = repo,
                                            kategori = s.kategori,
                                            kod = s.kod,
                                            ad = s.ad,
                                            anlatimVar = s.anlatimVar,
                                            onGeri = geriGit,
                                            onSohbetAc = { aktifBot = BotRef(kategori = s.kategori, kod = s.kod) },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.Rota -> RotaScreen(
                                            repo = repo,
                                            onGeri = geriGit,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminAnaSayfa -> AdminAnaSayfaScreen(
                                            repo = repo,
                                            onGeri = geriGit,
                                            onCikisYap = { adminVm.cikisYap() },
                                            onPersonaEkleTiklandi = { git(Screen.AdminPersonaEkle) },
                                            onRotaYeriEkleTiklandi = { git(Screen.AdminRotaYerEkle) },
                                            onPersonalariYonetTiklandi = { git(Screen.AdminPersonaYonet) },
                                            onRotaYerleriniYonetTiklandi = { git(Screen.AdminRotaYerYonet) },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminPersonaEkle -> PersonaEkleScreen(
                                            repo = repo,
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminRotaYerEkle -> RotaYerEkleScreen(
                                            repo = repo,
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminPersonaYonet -> PersonaYonetScreen(
                                            repo = repo,
                                            onGeri = geriGit,
                                            onDuzenleTiklandi = { detay -> git(Screen.AdminPersonaDuzenle(detay = detay)) },
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminPersonaDuzenle -> PersonaDuzenleScreen(
                                            repo = repo,
                                            detay = s.detay,
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminRotaYerYonet -> RotaYerYonetScreen(
                                            repo = repo,
                                            onGeri = geriGit,
                                            onDuzenleTiklandi = { mekan, mevcutAnlatim ->
                                                git(Screen.AdminRotaYerDuzenle(mekan = mekan, mevcutAnlatim = mevcutAnlatim))
                                            },
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminRotaYerDuzenle -> RotaYerDuzenleScreen(
                                            repo = repo,
                                            mekan = s.mekan,
                                            mevcutAnlatim = s.mevcutAnlatim,
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }
                    Footer(modifier = Modifier.fillMaxWidth())
                }

                // Sohbet paneli: arkadaki içeriği yerinde bırakan bir overlay
                // katmanı (ekranlar layout'tan kaldırılmaz, scroll pozisyonu
                // korunur). Geniş ekranda sağdan kayan dar bir panel, dar
                // ekranda tam ekran. Kapı geçişi BURAYA UYGULANMAZ — overlay
                // akışları kendi sheet/slide-in davranışını korur.
                //
                // aktifBot null olduğunda çıkış animasyonu boyunca paneli
                // çizmeye devam edebilmek için son geçerli bot hatırlanır —
                // yalnızca animasyon amaçlı; oturum/state mantığı değişmez.
                // Başlangıç değeri aktifBot: döndürmeden sonra panel ilk karede çizilsin.
                var sonBot by remember { mutableStateOf(aktifBot) }
                LaunchedEffect(aktifBot) { aktifBot?.let { sonBot = it } }
                val chatAcik = aktifBot != null

                // Sistem geri tuşu/jesti: önce açık sohbeti kapatır, sonra
                // ekran yığınında bir üst seviyeye çıkar. Home'da ve sohbet
                // kapalıyken devre dışı kalır ki normal uygulamadan çıkış
                // davranışı sisteme bırakılsın.
                BackHandler(enabled = chatAcik || ekranYigini.boyut > 1) {
                    if (chatAcik) aktifBot = null else geriGit()
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val genisEkran = maxWidth >= CHAT_GENIS_EKRAN_ESIGI

                    // Panelin arkasındaki karartma — tıklanınca sohbet kapanır.
                    // Yalnızca geniş ekranda, çünkü dar ekranda panel tam ekrandır.
                    val scrimInteraction = remember { MutableInteractionSource() }
                    AnimatedVisibility(
                        visible = chatAcik && genisEkran,
                        enter = fadeIn(animationSpec = tween(240)),
                        exit = fadeOut(animationSpec = tween(240)),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(KaranlikLacivert.copy(alpha = Opaklik.YUZDE62))
                                .clickable(
                                    interactionSource = scrimInteraction,
                                    indication = null,
                                    onClick = { aktifBot = null },
                                ),
                        )
                    }

                    AnimatedVisibility(
                        visible = chatAcik,
                        enter = slideInHorizontally(animationSpec = tween(240)) { it },
                        exit = slideOutHorizontally(animationSpec = tween(240)) { it },
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) {
                        sonBot?.let { b ->
                            // Sohbetin kendi ViewModel kapsamı: panel kapanıp
                            // çıkış animasyonu bitince ChatViewModel temizlenir
                            // (oturum kapanır); döndürmede aktifBot korunduğu
                            // için aynı bot ve aynı ViewModel ile geri açılır.
                            VmKapsami(
                                anahtar = "sohbet-${b.kategori}-${b.kod}",
                                halaGerekli = { aktifBot == b },
                            ) {
                                ChatSheet(
                                    repo = repo,
                                    kategori = b.kategori,
                                    oge = b.kod,
                                    onKapat = { aktifBot = null },
                                    modifier = if (genisEkran) {
                                        Modifier
                                            .width(CHAT_PANEL_GENISLIGI)
                                            .fillMaxHeight()
                                            .shadow(Yukseklik.DP16)
                                    } else {
                                        Modifier.fillMaxSize()
                                    },
                                )
                            }
                        }
                    }
                }

                when (dialogTuru) {
                    DialogTuru.BIZ_KIMIZ -> InfoDialog(
                        baslik = "Biz Kimiz",
                        metin = BIZ_KIMIZ_METNI,
                        onDismiss = { dialogTuru = DialogTuru.YOK },
                    )
                    DialogTuru.PROJE_HAKKINDA -> InfoDialog(
                        baslik = "Proje Hakkında",
                        metin = PROJE_HAKKINDA_METNI,
                        onDismiss = { dialogTuru = DialogTuru.YOK },
                    )
                    DialogTuru.YOK -> {}
                }

                if (acilisBilgilendirmeAcik) {
                    InfoDialog(
                        baslik = "Bilgilendirme",
                        metin = ACILIS_BILGILENDIRME_METNI,
                        onaylaMetni = "Anladım",
                        dismissOnClickOutside = false,
                        onDismiss = { acilisBilgilendirmeAcik = false },
                    )
                }

                if (adminGirisDialoguAcik) {
                    AdminGirisDialog(
                        vm = adminVm,
                        onDismiss = { adminGirisDialoguAcik = false },
                        onBasarili = {
                            adminGirisDialoguAcik = false
                            git(Screen.AdminAnaSayfa)
                        },
                    )
                }
            }
        }
        }
    }
}
