@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.kuzeykapisi.app.data.media

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume

/** Apple'ın "herhangi bir görsel" için tanımladığı sabit UTI (Uniform Type Identifier). */
private const val UTI_GORSEL = "public.image"

/**
 * PHPickerViewController (iOS 14+) ile gerçek sistem görsel seçici — fotoğraf
 * kütüphanesi izni GEREKTİRMEZ (yalnızca kullanıcının AÇIKÇA seçtiği öğeyi
 * uygulamaya döner, bu yüzden Info.plist'te bir kullanım açıklaması da yok).
 * Kullanıcı iptal ederse ya da öğe görsel değilse null döner — Android/web ile
 * aynı sözleşme.
 */
actual suspend fun resimSec(): SecilenResim? {
    val kokGorunum = enUstGorunumDenetleyicisi() ?: return null

    return suspendCancellableCoroutine { cont ->
        val yapilandirma = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter()
            selectionLimit = 1L
        }
        val secici = PHPickerViewController(configuration = yapilandirma)
        val delege = ResimSeciciDelegesi(cont)
        secici.delegate = delege
        // delegate ObjC tarafında WEAK'tir: bu güçlü referans olmazsa delege
        // sonuç gelmeden GC'lenebilir.
        aktifDelege = delege
        cont.invokeOnCancellation {
            secici.delegate = null
            aktifDelege = null
        }
        kokGorunum.presentViewController(secici, animated = true, completion = null)
    }
}

/** [resimSec] süresince delegeyi canlı tutan tek slot — aynı anda yalnızca bir seçim akışı olabilir. */
private var aktifDelege: ResimSeciciDelegesi? = null

private class ResimSeciciDelegesi(
    private val devam: CancellableContinuation<SecilenResim?>,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        aktifDelege = null

        val saglayici = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider
        if (saglayici == null || !saglayici.hasItemConformingToTypeIdentifier(UTI_GORSEL)) {
            if (devam.isActive) devam.resume(null)
            return
        }
        saglayici.loadDataRepresentationForTypeIdentifier(UTI_GORSEL) { veri, _ ->
            val sonuc = veri?.let {
                SecilenResim(bytes = it.tumBaytlariAl(), dosyaAdi = "secilen_gorsel.jpg", uzanti = ".jpg")
            }
            if (devam.isActive) devam.resume(sonuc)
        }
    }
}

/** Uygulamanın aktif penceresindeki EN ÜSTTEKİ (varsa modal üstüne modal açılmış) view controller. */
private fun enUstGorunumDenetleyicisi(): UIViewController? {
    @Suppress("DEPRECATION")
    val pencere = UIApplication.sharedApplication.keyWindow
        ?: (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)
    var kok = pencere?.rootViewController
    while (kok?.presentedViewController != null) {
        kok = kok.presentedViewController
    }
    return kok
}

private fun NSData.tumBaytlariAl(): ByteArray {
    val boy = length.toInt()
    if (boy == 0) return ByteArray(0)
    val veri = ByteArray(boy)
    veri.usePinned { pin ->
        memcpy(pin.addressOf(0), bytes, length)
    }
    return veri
}
