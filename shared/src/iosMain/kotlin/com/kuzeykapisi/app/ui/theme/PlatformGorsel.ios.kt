package com.kuzeykapisi.app.ui.theme

import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

/** Mobilde glow yok — yalnızca basma geri bildirimi. */
actual val fenerHalesiDestekli: Boolean = false

actual val hareketAzaltilsin: Boolean
    get() = runCatching { UIAccessibilityIsReduceMotionEnabled() }.getOrDefault(false)
