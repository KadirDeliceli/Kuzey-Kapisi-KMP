package com.kuzeykapisi.app.platform

import java.util.Calendar

actual fun guncelYil(): Int = Calendar.getInstance().get(Calendar.YEAR)
