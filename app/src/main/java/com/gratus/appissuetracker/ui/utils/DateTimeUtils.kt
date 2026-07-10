/*
 * MustDO
 * Copyright (C) 2026 spewedprojects <rkharat98@live.com>
 *
 * This file is part of MustDo Application.
 *
 * MustDo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See the LICENSE file for details.
 */

/*
 * Issue Tracker
 * Copyright (C) 2026 spewedprojects <rkharat98@live.com>
 *
 * This file is part of Issue Tracker Application.
 *
 * Issue Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See the LICENSE file for details.
 */

package com.gratus.appissuetracker.ui.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Thread-safe utility helper for handling all date and time formatting patterns used in the app.
 */
object DateTimeUtils {
    
    private val dbFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    private val mainHeaderFormatter = ThreadLocal.withInitial { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    private val homeDateLabelFormatter = ThreadLocal.withInitial { SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()) }
    private val shortDateFormatter = ThreadLocal.withInitial { SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()) }
    private val alarmTimeFormatter = ThreadLocal.withInitial { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    private val db get() = dbFormatter.get()!!
    private val mainHeader get() = mainHeaderFormatter.get()!!
    private val homeDateLabel get() = homeDateLabelFormatter.get()!!
    private val shortDate get() = shortDateFormatter.get()!!

    fun formatDbDate(calendar: Calendar): String = db.format(calendar.time)
    fun formatDbDate(timeMillis: Long): String = db.format(Date(timeMillis))
    fun parseDbDate(dateStr: String): Date? = try { db.parse(dateStr) } catch (e: Exception) { null }

    fun formatMainHeader(calendar: Calendar): String = mainHeader.format(calendar.time)
    fun formatMainHeader(date: Date): String = mainHeader.format(date)
    
    fun formatHomeDateLabel(calendar: Calendar): String = homeDateLabel.format(calendar.time)
    fun formatHomeDateLabel(date: Date): String = homeDateLabel.format(date)
    
    fun formatShortDate(calendar: Calendar): String = shortDate.format(calendar.time)
    fun formatShortDate(date: Date): String = shortDate.format(date)

    fun formatAlarmTime(context: android.content.Context?, timeMillis: Long): String {
        val is24 = try {
            context?.let { android.text.format.DateFormat.is24HourFormat(it) } ?: false
        } catch (e: Exception) {
            false
        }
        val pattern = if (is24) "HH:mm" else "hh:mm a"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun formatAlarmTime(timeMillis: Long): String = formatAlarmTime(null, timeMillis)

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(calendar: Calendar): Boolean {
        return isSameDay(calendar, Calendar.getInstance())
    }
}
