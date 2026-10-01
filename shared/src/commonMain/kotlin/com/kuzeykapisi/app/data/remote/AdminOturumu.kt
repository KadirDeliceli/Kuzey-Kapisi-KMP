package com.kuzeykapisi.app.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.concurrent.Volatile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

/** Son admin isteğinden bu kadar süre sonra oturum kendiliğinden kapanır. */
val ADMIN_HAREKETSIZLIK_SURESI: Duration = 15.minutes

enum class OturumSonlanmaNedeni {
    /** Kullanıcı "Çıkış yap"a bastı. */
    CIKIS,

    /** Backend bir admin isteğine 401 döndü (token geçersiz/süresi dolmuş, backend yeniden başladı...). */
    YETKISIZ,

    /** [ADMIN_HAREKETSIZLIK_SURESI] boyunca hiç admin isteği yapılmadı. */
    ZAMAN_ASIMI,
}

/**
 * Admin token'ının TEK kaynağı. Token yalnızca bellekte tutulur (kalıcı
 * depolama yok). HttpClient'taki [adminTokenEklentisi] her /admin/... isteğine
 * token'ı buradan ekler; ekranlar ve ViewModel'ler token'ı hiç görmez.
 *
 * Oturum hangi yoldan biterse bitsin (çıkış, 401, zaman aşımı) [sonlanma]
 * tek bir olay yayar; uygulama kabuğu (App.kt) buna göre admin ekranlarını
 * yığından tek seferde temizler.
 */
class AdminOturumu {
    private val _token = MutableStateFlow<String?>(null)

    private val _acik = MutableStateFlow(false)

    /** Arayüzün yalnızca "oturum açık mı" bilgisi için okuduğu durum. */
    val acik: StateFlow<Boolean> = _acik.asStateFlow()

    private val _sonlanma = MutableSharedFlow<OturumSonlanmaNedeni>(extraBufferCapacity = 4)
    val sonlanma: SharedFlow<OturumSonlanmaNedeni> = _sonlanma.asSharedFlow()

    @Volatile
    private var sonEtkinlik: TimeSource.Monotonic.ValueTimeMark? = null

    fun baslat(token: String) {
        sonEtkinlik = TimeSource.Monotonic.markNow()
        _token.value = token
        _acik.value = true
    }

    /** Oturumu kapatır ve olayı yayar. Zaten kapalıysa hiçbir şey yapmaz (tek olay). */
    fun sonlandir(neden: OturumSonlanmaNedeni) {
        val onceki = _token.value ?: return
        if (!_token.compareAndSet(onceki, null)) return
        sonEtkinlik = null
        _acik.value = false
        _sonlanma.tryEmit(neden)
    }

    /**
     * Bir admin isteği için kullanılacak token. Oturum yoksa ya da hareketsizlik
     * süresi dolmuşsa null döner (süre dolmuşsa oturumu da kapatır) — çağıran
     * istek o zaman HİÇ gönderilmez. Geçerliyse etkinlik zamanı yenilenir.
     */
    fun istekIcinToken(): String? {
        val token = _token.value ?: return null
        val kalan = kalanSure() ?: return null
        if (kalan <= Duration.ZERO) {
            sonlandir(OturumSonlanmaNedeni.ZAMAN_ASIMI)
            return null
        }
        sonEtkinlik = TimeSource.Monotonic.markNow()
        return token
    }

    /** Hareketsizlik süresinin dolmasına kalan süre; oturum yoksa null. */
    fun kalanSure(): Duration? {
        if (_token.value == null) return null
        val son = sonEtkinlik ?: return null
        return ADMIN_HAREKETSIZLIK_SURESI - son.elapsedNow()
    }
}
