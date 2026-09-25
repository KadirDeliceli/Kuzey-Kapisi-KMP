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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.model.PersonaDetay
import com.kuzeykapisi.app.data.model.RotaMekaniAdmin
import com.kuzeykapisi.app.data.remote.ApiService
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.domain.MainCardType
import com.kuzeykapisi.app.domain.SubCard
import com.kuzeykapisi.app.ui.components.ACILIS_BILGILENDIRME_METNI
import com.kuzeykapisi.app.ui.components.AdminGirisDialog
import com.kuzeykapisi.app.ui.components.BIZ_KIMIZ_METNI
import com.kuzeykapisi.app.ui.components.ChatSheet
import com.kuzeykapisi.app.ui.components.Footer
import com.kuzeykapisi.app.ui.components.InfoDialog
import com.kuzeykapisi.app.ui.components.PROJE_HAKKINDA_METNI
import com.kuzeykapisi.app.ui.components.TopBar
import com.kuzeykapisi.app.ui.kurulumYapImageLoader
import com.kuzeykapisi.app.ui.screens.AdminAnaSayfaScreen
import com.kuzeykapisi.app.ui.screens.AnlatimEkrani
import com.kuzeykapisi.app.ui.screens.BotListScreen
import com.kuzeykapisi.app.ui.screens.HomeScreen
import com.kuzeykapisi.app.ui.screens.PersonaDuzenleScreen
import com.kuzeykapisi.app.ui.screens.PersonaEkleScreen
import com.kuzeykapisi.app.ui.screens.PersonaYonetScreen
import com.kuzeykapisi.app.ui.screens.RotaScreen
import com.kuzeykapisi.app.ui.screens.RotaYerDuzenleScreen
import com.kuzeykapisi.app.ui.screens.RotaYerEkleScreen
import com.kuzeykapisi.app.ui.screens.RotaYerYonetScreen
import com.kuzeykapisi.app.ui.screens.SubMenuScreen
import com.kuzeykapisi.app.ui.screens.WipScreen
import com.kuzeykapisi.app.ui.theme.KapiGecisi
import com.kuzeykapisi.app.ui.theme.KaranlikLacivert
import com.kuzeykapisi.app.ui.theme.KuzeyKapisiTheme
import com.kuzeykapisi.app.ui.vm.AdminViewModel
import com.kuzeykapisi.app.ui.vm.AnlatimKaynagi
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.sinop_arkaplan
import org.jetbrains.compose.resources.painterResource

sealed interface Screen {
    data object Home : Screen
    data class SubMenu(val mainCard: MainCard) : Screen
    data class BotList(val kategori: String, val baslik: String) : Screen
    data class Anlatim(val kaynak: AnlatimKaynagi, val baslik: String) : Screen
    data object Wip : Screen
    data object Rota : Screen
    data object AdminAnaSayfa : Screen
    data object AdminPersonaEkle : Screen
    data object AdminRotaYerEkle : Screen
    data object AdminPersonaYonet : Screen
    data class AdminPersonaDuzenle(val detay: PersonaDetay) : Screen
    data object AdminRotaYerYonet : Screen
    data class AdminRotaYerDuzenle(val mekan: RotaMekaniAdmin, val mevcutAnlatim: String?) : Screen
}

data class BotRef(val kategori: String, val kod: String)

private enum class DialogTuru { YOK, BIZ_KIMIZ, PROJE_HAKKINDA }

/** Bu genişliğin altında sohbet tam ekran, üstünde sağdan dar panel olarak açılır. */
private val CHAT_GENIS_EKRAN_ESIGI = 600.dp

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
        println("[KuzeyKapisi] Config.BASE_URL = ${Config.BASE_URL}")
        Unit
    }
    val repo = remember { KuzeyRepository(ApiService()) }
    // Ekran geçmişi bir yığın olarak tutulur: yeni ekrana geçişte push, geri
    // gidişte pop. Böylece geri adımı her zaman SADECE bir üst seviyeye çıkar.
    val ekranYigini = remember { mutableStateListOf<Screen>(Screen.Home) }
    val screen = ekranYigini.last()
    var aktifBot by remember { mutableStateOf<BotRef?>(null) }
    var dialogTuru by remember { mutableStateOf(DialogTuru.YOK) }
    // Açılış bilgilendirme dialog'u: yalnızca uygulama bu oturumda ilk kez
    // render edildiğinde gösterilir, kart/ekran geçişlerinde tekrar açılmaz.
    var acilisBilgilendirmeAcik by remember { mutableStateOf(true) }
    // Yalnızca GÖRSEL geçişin yönü: "kapı açılma" animasyonunun hangi tarafa
    // işleyeceğini söyler. Navigasyon kararlarına HİÇBİR etkisi yoktur.
    var gecisIleri by remember { mutableStateOf(true) }

    // Admin: token yalnızca bellekte tutulur, sade AdminViewModel + StateFlow.
    val adminVm = remember(repo) { AdminViewModel(repo) }
    val adminUi by adminVm.state.collectAsState()
    var adminGirisDialoguAcik by remember { mutableStateOf(false) }

    val git: (Screen) -> Unit = { hedef ->
        gecisIleri = true
        ekranYigini.add(hedef)
    }
    val geriGit: () -> Unit = {
        if (ekranYigini.size > 1) {
            gecisIleri = false
            ekranYigini.removeAt(ekranYigini.lastIndex)
        }
    }
    // Tüm admin ekranlarında 401 alındığında ortak davranış: token sıfırlanır,
    // ekrandan çıkılır ve giriş dialogu tekrar açılır.
    val onAdminYetkisiz: () -> Unit = {
        adminVm.oturumuSifirla()
        geriGit()
        adminGirisDialoguAcik = true
    }

    KuzeyKapisiTheme {
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
                        .background(KaranlikLacivert.copy(alpha = 0.90f)),
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        onBizKimizClick = { dialogTuru = DialogTuru.BIZ_KIMIZ },
                        onProjeHakkindaClick = { dialogTuru = DialogTuru.PROJE_HAKKINDA },
                        onAdminIkonClick = {
                            if (adminUi.token != null) git(Screen.AdminAnaSayfa) else adminGirisDialoguAcik = true
                        },
                    )
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        // İMZA GEÇİŞ: ekran hiyerarşisinde derine inerken içerik
                        // ortadan açılarak gelir, geri dönerken kapı kapanır gibi
                        // ortaya doğru kapanır. Hangi ekranın çizileceği kararı
                        // aşağıdaki `when` ile, önceki hâliyle birebir aynıdır.
                        // Genişlik sınırı ekran başına uygulanır: ana sayfa tam genişlikte
                        // (kenardan kenara hero ve kartlar), diğer ekranlar 1100dp'de kalır.
                        // Sınır her ekran örneğinin kendi kutusunda olduğu için geçiş
                        // sırasında genişlik sıçramaz.
                        KapiGecisi(
                            hedef = screen,
                            ileri = gecisIleri,
                            modifier = Modifier.fillMaxSize(),
                        ) { s ->
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                Box(
                                    modifier = if (s is Screen.Home) {
                                        Modifier.fillMaxSize()
                                    } else {
                                        Modifier.fillMaxHeight().widthIn(max = 1100.dp)
                                    },
                                ) {
                                    when (s) {
                                        is Screen.Home -> HomeScreen(
                                            onKartTiklandi = { kart ->
                                                git(
                                                    when (kart.type) {
                                                        MainCardType.SUBMENU -> Screen.SubMenu(kart)
                                                        MainCardType.WIP -> Screen.Wip
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
                                                aktifBot = BotRef(kategori = s.kategori, kod = oge.kod)
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
                                        is Screen.Wip -> WipScreen(
                                            onGeri = geriGit,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.Rota -> RotaScreen(
                                            repo = repo,
                                            onGeri = geriGit,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminAnaSayfa -> AdminAnaSayfaScreen(
                                            onGeri = geriGit,
                                            onPersonaEkleTiklandi = { git(Screen.AdminPersonaEkle) },
                                            onRotaYeriEkleTiklandi = { git(Screen.AdminRotaYerEkle) },
                                            onPersonalariYonetTiklandi = { git(Screen.AdminPersonaYonet) },
                                            onRotaYerleriniYonetTiklandi = { git(Screen.AdminRotaYerYonet) },
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminPersonaEkle -> PersonaEkleScreen(
                                            repo = repo,
                                            token = adminUi.token.orEmpty(),
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminRotaYerEkle -> RotaYerEkleScreen(
                                            repo = repo,
                                            token = adminUi.token.orEmpty(),
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminPersonaYonet -> PersonaYonetScreen(
                                            repo = repo,
                                            token = adminUi.token.orEmpty(),
                                            onGeri = geriGit,
                                            onDuzenleTiklandi = { detay -> git(Screen.AdminPersonaDuzenle(detay = detay)) },
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminPersonaDuzenle -> PersonaDuzenleScreen(
                                            repo = repo,
                                            detay = s.detay,
                                            token = adminUi.token.orEmpty(),
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        is Screen.AdminRotaYerYonet -> RotaYerYonetScreen(
                                            repo = repo,
                                            token = adminUi.token.orEmpty(),
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
                                            token = adminUi.token.orEmpty(),
                                            onGeri = geriGit,
                                            onYetkisiz = onAdminYetkisiz,
                                            modifier = Modifier.fillMaxSize(),
                                        )
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
                var sonBot by remember { mutableStateOf<BotRef?>(null) }
                LaunchedEffect(aktifBot) { aktifBot?.let { sonBot = it } }
                val chatAcik = aktifBot != null

                // Sistem geri tuşu/jesti: önce açık sohbeti kapatır, sonra
                // ekran yığınında bir üst seviyeye çıkar. Home'da ve sohbet
                // kapalıyken devre dışı kalır ki normal uygulamadan çıkış
                // davranışı sisteme bırakılsın.
                BackHandler(enabled = chatAcik || ekranYigini.size > 1) {
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
                                .background(KaranlikLacivert.copy(alpha = 0.62f))
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
                            ChatSheet(
                                repo = repo,
                                kategori = b.kategori,
                                oge = b.kod,
                                onKapat = { aktifBot = null },
                                modifier = if (genisEkran) {
                                    Modifier
                                        .width(CHAT_PANEL_GENISLIGI)
                                        .fillMaxHeight()
                                        .shadow(16.dp)
                                } else {
                                    Modifier.fillMaxSize()
                                },
                            )
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
