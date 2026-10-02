@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.kuzeykapisi.app.platform

actual fun guncelYil(): Int = js("new Date().getFullYear()")
