package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeFormatter {

    fun formatMessageTime(timestamp: Long): String {
        val date = Date(timestamp)
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(date)
    }

    fun formatConversationDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, messageDate) -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Today, ${timeFormat.format(Date(timestamp))}"
            }
            isYesterday(now, messageDate) -> {
                "Yesterday"
            }
            isWithinDays(now, messageDate, 7) -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                dayFormat.format(Date(timestamp))
            }
            else -> {
                val fullFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                fullFormat.format(Date(timestamp))
            }
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, date)
    }

    private fun isWithinDays(now: Calendar, date: Calendar, days: Int): Boolean {
        val diffMillis = now.timeInMillis - date.timeInMillis
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)
        return diffDays in 0..days
    }
}
