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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.data.model.KatalogOge
import com.kuzeykapisi.app.data.remote.ApiService
import com.kuzeykapisi.app.data.repo.KuzeyRepository
import com.kuzeykapisi.app.domain.MainCard
import com.kuzeykapisi.app.domain.MainCardType
import com.kuzeykapisi.app.domain.SubCard
import com.kuzeykapisi.app.ui.components.BIZ_KIMIZ_METNI
import com.kuzeykapisi.app.ui.components.ChatSheet
import com.kuzeykapisi.app.ui.components.InfoDialog
import com.kuzeykapisi.app.ui.components.PROJE_HAKKINDA_METNI
import com.kuzeykapisi.app.ui.components.TopBar
import com.kuzeykapisi.app.ui.kurulumYapImageLoader
import com.kuzeykapisi.app.ui.screens.BotListScreen
import com.kuzeykapisi.app.ui.screens.HomeScreen
import com.kuzeykapisi.app.ui.screens.SubMenuScreen
import com.kuzeykapisi.app.ui.screens.WipScreen
import com.kuzeykapisi.app.ui.theme.Deniz
import com.kuzeykapisi.app.ui.theme.Kagit
import com.kuzeykapisi.app.ui.theme.KuzeyKapisiTheme
import kuzeykapisiapp.shared.generated.resources.Res
import kuzeykapisiapp.shared.generated.resources.sinop_arkaplan
import org.jetbrains.compose.resources.painterResource

sealed interface Screen {
    data object Home : Screen
    data class SubMenu(val mainCard: MainCard) : Screen
    data class BotList(val kategori: String, val baslik: String) : Screen
    data object Wip : Screen
}

data class BotRef(val kategori: String, val kod: String)

private enum class DialogTuru { YOK, BIZ_KIMIZ, PROJE_HAKKINDA }

/** Bu genişliğin altında sohbet tam ekran, üstünde sağdan dar panel olarak açılır. */
private val CHAT_GENIS_EKRAN_ESIGI = 600.dp

/** Geniş ekranda sohbet panelinin sabit genişliği. */
private val CHAT_PANEL_GENISLIGI = 400.dp

@Composable
fun App() {
    remember {
        kurulumYapImageLoader()
        println("[KuzeyKapisi] Config.BASE_URL = ${Config.BASE_URL}")
        Unit
    }
    val repo = remember { KuzeyRepository(ApiService()) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var aktifBot by remember { mutableStateOf<BotRef?>(null) }
    var dialogTuru by remember { mutableStateOf(DialogTuru.YOK) }

    KuzeyKapisiTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // En alt katman: sabit arka plan fotoğrafı. Üstündeki yüksek
                // opaklıklı "kagit" overlay ile birlikte, göz yormayan hafif
                // buğulu bir doku olarak hissedilir. Not: Modifier.blur()
                // Android API 31 altında sessizce devre dışı kalır (minSdk=24);
                // o cihazlarda yalnızca opaklık katmanı devreye girer.
                Image(
                    painter = painterResource(Res.drawable.sinop_arkaplan),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(8.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Kagit.copy(alpha = 0.90f)),
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        onBizKimizClick = { dialogTuru = DialogTuru.BIZ_KIMIZ },
                        onProjeHakkindaClick = { dialogTuru = DialogTuru.PROJE_HAKKINDA },
                    )
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        Box(modifier = Modifier.fillMaxHeight().widthIn(max = 1100.dp)) {
                            when (val s = screen) {
                                is Screen.Home -> HomeScreen(
                                    onKartTiklandi = { kart ->
                                        screen = when (kart.type) {
                                            MainCardType.SUBMENU -> Screen.SubMenu(kart)
                                            MainCardType.WIP -> Screen.Wip
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                is Screen.SubMenu -> SubMenuScreen(
                                    mainCard = s.mainCard,
                                    onGeri = { screen = Screen.Home },
                                    onSubTiklandi = { sub: SubCard ->
                                        screen = Screen.BotList(kategori = sub.kategori, baslik = sub.ad)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                is Screen.BotList -> BotListScreen(
                                    repo = repo,
                                    kategori = s.kategori,
                                    baslik = s.baslik,
                                    onGeri = { screen = Screen.Home },
                                    onBotTiklandi = { oge: KatalogOge ->
                                        aktifBot = BotRef(kategori = s.kategori, kod = oge.kod)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                                is Screen.Wip -> WipScreen(
                                    onGeri = { screen = Screen.Home },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }

                // Sohbet paneli: arkadaki içeriği yerinde bırakan bir overlay
                // katmanı (ekranlar layout'tan kaldırılmaz, scroll pozisyonu
                // korunur). Geniş ekranda sağdan kayan dar bir panel, dar
                // ekranda tam ekran.
                //
                // aktifBot null olduğunda çıkış animasyonu boyunca paneli
                // çizmeye devam edebilmek için son geçerli bot hatırlanır —
                // yalnızca animasyon amaçlı; oturum/state mantığı değişmez.
                var sonBot by remember { mutableStateOf<BotRef?>(null) }
                LaunchedEffect(aktifBot) { aktifBot?.let { sonBot = it } }
                val chatAcik = aktifBot != null

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
                                .background(Deniz.copy(alpha = 0.35f))
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
            }
        }
    }
}
