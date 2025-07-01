package com.thatwaz.timesquish.util



import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun getStartOfWeek(date: LocalDateTime): LocalDateTime {
    // DayOfWeek.SUNDAY = 7
    val daysToSubtract = if (date.dayOfWeek.value == 7) 0 else date.dayOfWeek.value
    return date.minusDays(daysToSubtract.toLong()).toLocalDate().atStartOfDay()
}

fun getEndOfWeek(startOfWeek: LocalDateTime): LocalDateTime {
    return startOfWeek.plusDays(7).minusNanos(1)
}
