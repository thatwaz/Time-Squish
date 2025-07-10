package com.thatwaz.timesquish.util



import android.app.TimePickerDialog
import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun getStartOfWeek(date: LocalDateTime): LocalDateTime {
    // DayOfWeek.SUNDAY = 7
    val daysToSubtract = if (date.dayOfWeek.value == 7) 0 else date.dayOfWeek.value
    return date.minusDays(daysToSubtract.toLong()).toLocalDate().atStartOfDay()
}

fun getEndOfWeek(startOfWeek: LocalDateTime): LocalDateTime {
    return startOfWeek.plusDays(7).minusNanos(1)
}


fun showDatePicker(
    context: Context,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).show()
}

fun showTimePicker(
    context: Context,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(LocalTime.of(hourOfDay, minute))
        },
        initialTime.hour,
        initialTime.minute,
        true
    ).show()
}
