package com.thatwaz.timesquish.data.repository

import com.thatwaz.timesquish.data.local.TimeEntry
import com.thatwaz.timesquish.data.local.TimeEntryDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeEntryRepository @Inject constructor(
    private val dao: TimeEntryDao
) {

    suspend fun insertTimeEntry(entry: TimeEntry) {
        dao.insertTimeEntry(entry)
    }

    suspend fun updateTimeEntry(entry: TimeEntry) {
        dao.updateTimeEntry(entry)
    }

    suspend fun deleteTimeEntry(entry: TimeEntry) {
        dao.deleteTimeEntry(entry)
    }

    fun getAllEntries(): Flow<List<TimeEntry>> {
        return dao.getAllEntries()
    }

    fun getUnsubmittedEntries(): Flow<List<TimeEntry>> {
        return dao.getUnsubmittedEntries()
    }

    fun getEntriesForDate(date: LocalDateTime): Flow<List<TimeEntry>> {
        return dao.getEntriesForDate(date)
    }

    suspend fun setSubmitted(id: Int, isSubmitted: Boolean) {
        dao.setSubmitted(id, isSubmitted)
    }

    suspend fun getActiveSession(): TimeEntry? {
        return dao.getActiveSession()
    }

    suspend fun completeSession(id: Int, endTime: LocalDateTime, duration: Long) {
        dao.completeSession(id, endTime, duration)
    }

}
