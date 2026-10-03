package com.kuzeykapisi.app.platform

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberUygulamadanCikis(): () -> Unit {
    val activity = LocalActivity.current
    return remember(activity) { { activity?.finish() } }
}
