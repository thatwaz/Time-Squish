package com.thatwaz.timesquish.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "time_entries")
data class TimeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val durationMinutes: Long? = null,
    val isManual: Boolean = false,
    val isSubmitted: Boolean = false,
    val label: String? = null,
    val isHidden: Boolean = false,
    val squishGroupId: String? = null



)
