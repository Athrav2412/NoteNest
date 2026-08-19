package com.notenest.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatFullDateTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return fullDateFormat.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return shortDateFormat.format(Date(timestamp))
    }

    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0) return ""

        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp

        val calendarNow = Calendar.getInstance()
        val calendarTarget = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = calendarNow.get(Calendar.YEAR) == calendarTarget.get(Calendar.YEAR) &&
                calendarNow.get(Calendar.DAY_OF_YEAR) == calendarTarget.get(Calendar.DAY_OF_YEAR)

        val isYesterday = calendarNow.get(Calendar.YEAR) == calendarTarget.get(Calendar.YEAR) &&
                calendarNow.get(Calendar.DAY_OF_YEAR) - calendarTarget.get(Calendar.DAY_OF_YEAR) == 1

        return when {
            diffMillis < 60_000 -> "Just now"
            diffMillis < 3_600_000 -> "${diffMillis / 60_000}m ago"
            isToday -> "Today, ${timeFormat.format(Date(timestamp))}"
            isYesterday -> "Yesterday, ${timeFormat.format(Date(timestamp))}"
            else -> shortDateFormat.format(Date(timestamp))
        }
    }

    fun formatRelativeDate(timestamp: Long): String = formatRelativeTime(timestamp)
    fun formatDateTime(timestamp: Long): String = formatFullDateTime(timestamp)
}
