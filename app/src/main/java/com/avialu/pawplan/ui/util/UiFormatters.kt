package com.avialu.pawplan.ui.util

import com.avialu.pawplan.data.models.ActivityType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatWhen(ts: Long): String {
    val fmt = SimpleDateFormat("dd/MM", Locale.getDefault())
    return fmt.format(Date(ts))
}

fun formatWhenHour(ts: Long): String {
    val fmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return fmt.format(Date(ts))
}


fun formatActivityType(type: String): String {
    val t = ActivityType.entries.firstOrNull { it.name == type }
    return t?.label ?: type
}

fun petAgeText(birthYear: Int?): String? {
    if (birthYear == null) return null
    val yearNow = Calendar.getInstance().get(Calendar.YEAR)
    val age = yearNow - birthYear
    return if (age >= 0) "$age years" else null
}

fun addMonths(ts: Long, months: Int): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = ts
    cal.add(Calendar.MONTH, months)
    return cal.timeInMillis
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

fun petTypeIcon(type: String): String = when (type.lowercase()) {
    "dog" -> "🐶"
    "cat" -> "🐱"
    "rabbit" -> "🐰"
    else -> "🐾"
}

