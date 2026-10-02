package com.kuzeykapisi.app.platform

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun guncelYil(): Int =
    NSCalendar.currentCalendar.component(NSCalendarUnitYear, fromDate = NSDate()).toInt()
