@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.ui.theme

actual val fenerHalesiDestekli: Boolean = true

actual val hareketAzaltilsin: Boolean
    get() = runCatching { jsHareketAzaltilsin() }.getOrDefault(false)

private fun jsHareketAzaltilsin(): Boolean = js(
    """
    (function() {
        if (!window.matchMedia) return false;
        return window.matchMedia('(prefers-reduced-motion: reduce)').matches === true;
    })()
    """,
)
