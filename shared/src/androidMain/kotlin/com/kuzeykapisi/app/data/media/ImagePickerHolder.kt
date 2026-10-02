package com.kuzeykapisi.app.data.media

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * MainActivity.onCreate() içinde registerForActivityResult ile oluşturulan
 * launcher burada saklanır; commonMain'deki `resimSec()` (Composable
 * olmayan bir suspend fonksiyon) bu launcher'ı Activity Result API'sinin
 * gerektirdiği callback/launcher modeliyle köprüler.
 */
object ImagePickerHolder {
    private var launcher: ActivityResultLauncher<String>? = null
    private var bekleyenDevam: CancellableContinuation<Uri?>? = null

    fun launcherAyarla(l: ActivityResultLauncher<String>) {
        launcher = l
    }

    fun sonucGeldi(uri: Uri?) {
        bekleyenDevam?.resume(uri)
        bekleyenDevam = null
    }

    suspend fun sec(): Uri? = suspendCancellableCoroutine { cont ->
        val l = launcher
        if (l == null) {
            cont.resume(null)
        } else {
            bekleyenDevam = cont
            // Çağıran coroutine iptal edilirse (ör. ekran kapanıp ViewModel
            // temizlenirse) bekleyen referans silinir — yoksa kullanıcı daha
            // sonra bir görsel seçtiğinde sonucGeldi() iptal edilmiş bu
            // continuation'ı resume etmeye çalışıp çöker.
            cont.invokeOnCancellation { bekleyenDevam = null }
            l.launch("image/*")
        }
    }
}
