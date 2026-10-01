package com.kuzeykapisi.app.data.remote

import com.kuzeykapisi.app.config.Config
import com.kuzeykapisi.app.log.Logger
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.Url
import io.ktor.http.encodedPath

/** Token İSTEMEYEN tek admin ucu: girişin kendisi. */
private const val ADMIN_GIRIS_YOLU = "admin/giris"

private val tabanYolu: String = Url(Config.BASE_URL).encodedPath.let { if (it.endsWith("/")) it else "$it/" }

/** BASE_URL'e göre göreli yol "admin/..." ise (giriş hariç) token gerektiren bir admin isteğidir. */
internal fun tokenGerektirenAdminYolu(encodedPath: String): Boolean {
    val goreli = encodedPath.removePrefix(tabanYolu).removePrefix("/")
    return goreli.startsWith("admin/") && goreli != ADMIN_GIRIS_YOLU
}

/**
 * Yalnızca /admin/... isteklerine (giriş hariç) "X-Admin-Token" ekler. Token
 * [AdminOturumu]'ndan okunur; oturum yoksa ya da hareketsizlikten süresi
 * dolmuşsa istek HİÇ GÖNDERİLMEZ — boş token'la admin isteği atılmaz — ve
 * 401 ile aynı şekilde ele alınan bir [AdminApiHatasi] fırlatılır.
 */
internal fun adminTokenEklentisi(oturum: AdminOturumu): ClientPlugin<Unit> =
    createClientPlugin("AdminTokenEklentisi") {
        onRequest { istek, _ ->
            if (tokenGerektirenAdminYolu(istek.url.encodedPath)) {
                val token = oturum.istekIcinToken() ?: run {
                    Logger.d { "admin isteği GÖNDERİLMEDİ (oturum yok ya da süresi doldu): ${istek.url.encodedPath}" }
                    throw AdminApiHatasi(httpKodu = 401, detay = null)
                }
                istek.headers.append("X-Admin-Token", token)
            }
        }
    }
