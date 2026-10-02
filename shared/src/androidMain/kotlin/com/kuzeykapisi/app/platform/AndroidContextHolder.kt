package com.kuzeykapisi.app.platform

import android.content.Context

/** MainActivity.onCreate() içinde bir kez set edilir; konum gibi Context gerektiren ama Composable olmayan expect/actual fonksiyonlar için. */
object AndroidContextHolder {
    lateinit var appContext: Context
}
