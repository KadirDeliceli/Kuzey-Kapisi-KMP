package com.kuzeykapisi.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.kuzeykapisi.app.domain.Screen
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Yığındaki bir ekran. [id] her eklemede artan, tekrar kullanılmayan bir
 * sayıdır: aynı ekran iki kez açılsa bile her girdinin kendi ViewModel
 * kapsamı olur (bkz. [VmKapsami]).
 */
@Serializable
data class EkranGirdisi(val id: Long, val ekran: Screen)

/**
 * Ekran geçmişi: yeni ekrana geçişte ekle, geri gidişte çıkar. Böylece geri
 * adımı her zaman yalnızca bir üst seviyeye çıkar. [Saver] ile JSON'a
 * yazılıp yapılandırma değişikliğinden (ve Android'de süreç ölümünden) sonra
 * aynı derinlikte geri kurulur.
 */
@Stable
class EkranYigini private constructor(baslangic: List<EkranGirdisi>, private var sonId: Long) {
    val girdiler = mutableStateListOf<EkranGirdisi>().apply { addAll(baslangic) }

    val ust: EkranGirdisi get() = girdiler.last()
    val boyut: Int get() = girdiler.size

    fun ekle(ekran: Screen) {
        sonId++
        girdiler.add(EkranGirdisi(sonId, ekran))
    }

    /** En üstteki ekranı çıkarır; kök ekranda (tek girdi) hiçbir şey yapmaz. */
    fun cikar(): Boolean {
        if (girdiler.size <= 1) return false
        girdiler.removeAt(girdiler.lastIndex)
        return true
    }

    fun iceriyor(id: Long): Boolean = girdiler.any { it.id == id }

    fun ustMu(id: Long): Boolean = girdiler.lastOrNull()?.id == id

    /** Koşula uyan girdileri çıkarır; kök ekran her durumda kalır. */
    fun kaldir(kosul: (Screen) -> Boolean) {
        val kok = girdiler.first()
        girdiler.removeAll { it !== kok && kosul(it.ekran) }
    }

    @Serializable
    private class Kayit(val sonId: Long, val girdiler: List<EkranGirdisi>)

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "#tur"
        }

        fun yeni(): EkranYigini = EkranYigini(listOf(EkranGirdisi(0, Screen.Home)), 0)

        val Saver: Saver<EkranYigini, String> = Saver(
            save = { json.encodeToString(Kayit.serializer(), Kayit(it.sonId, it.girdiler.toList())) },
            // Çözülemeyen eski bir kayıt (ör. uygulama güncellemesi sonrası)
            // null döner; rememberSaveable o zaman yeni bir yığınla başlar.
            restore = { kayit ->
                runCatching { json.decodeFromString(Kayit.serializer(), kayit) }
                    .getOrNull()
                    ?.takeIf { it.girdiler.isNotEmpty() }
                    ?.let { EkranYigini(it.girdiler, it.sonId) }
            },
        )
    }
}

@Composable
fun rememberEkranYigini(): EkranYigini = rememberSaveable(saver = EkranYigini.Saver) { EkranYigini.yeni() }
