package com.kuzeykapisi.app.platform

actual fun guncelYil(): Int = kotlin.js.Date().getFullYear()
