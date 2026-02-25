package com.avialu.pawplan.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun petTypeIcon(type: String): String = when (type.lowercase()) {
    "dog" -> "🐶"
    "cat" -> "🐱"
    "rabbit" -> "🐰"
    else -> "🐾"
}

fun startOfDay(ts: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = ts
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun formatDayRelative(ts: Long, now: Long = System.currentTimeMillis()): String {
    val today = startOfDay(now)
    val thatDay = startOfDay(ts)
    return when (thatDay) {
        today -> "today"
        today - 24L * 60 * 60 * 1000 -> "yesterday"
        else -> {
            val fmt = SimpleDateFormat("dd/MM", Locale.getDefault())
            fmt.format(Date(ts))
        }
    }
}