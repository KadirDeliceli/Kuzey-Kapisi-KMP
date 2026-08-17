package com.kuzeykapisi.app.ui

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory

/**
 * Coil3'ün otomatik ServiceLoader tabanlı bileşen keşfi Kotlin/Wasm ve Kotlin/JS
 * hedeflerinde çalışmıyor — bu yüzden ağ fetcher'ı burada elle kaydedilmeli,
 * aksi halde AsyncImage hiçbir zaman gerçek bir HTTP isteği atmadan doğrudan
 * error/placeholder görseline düşer.
 */
private object KuzeyImageLoaderFactory : SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
}

fun kurulumYapImageLoader() {
    SingletonImageLoader.setSafe(KuzeyImageLoaderFactory)
}
