package com.thatwaz.timesquish.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TimeEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntry(entry: TimeEntry)

    @Update
    suspend fun updateTimeEntry(entry: TimeEntry)

    @Delete
    suspend fun deleteTimeEntry(entry: TimeEntry)


    @Query("SELECT * FROM time_entries WHERE isHidden = 0 ORDER BY startTime DESC")
    fun getAllEntries(): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries ORDER BY startTime DESC")
    suspend fun getAllEntriesIncludingHidden(): List<TimeEntry>


    @Query("SELECT * FROM time_entries WHERE isSubmitted = 0 ORDER BY startTime DESC")
    fun getUnsubmittedEntries(): Flow<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE date(startTime) = date(:date) ORDER BY startTime ASC")
    fun getEntriesForDate(date: LocalDateTime): Flow<List<TimeEntry>>

    @Query("UPDATE time_entries SET isSubmitted = :isSubmitted WHERE id = :id")
    suspend fun setSubmitted(id: Int, isSubmitted: Boolean)

    @Query("SELECT * FROM time_entries WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): TimeEntry?

    @Query("UPDATE time_entries SET endTime = :endTime, durationMinutes = :duration WHERE id = :id")
    suspend fun completeSession(id: Int, endTime: LocalDateTime, duration: Long)

    @Query("SELECT * FROM time_entries")
    suspend fun getAllEntriesOnce(): List<TimeEntry>


}
