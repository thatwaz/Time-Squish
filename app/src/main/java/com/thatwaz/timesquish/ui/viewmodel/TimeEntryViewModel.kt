package com.thatwaz.timesquish.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.timesquish.data.local.TimeEntry
import com.thatwaz.timesquish.data.repository.TimeEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TimeEntryViewModel @Inject constructor(
    private val repository: TimeEntryRepository
) : ViewModel() {

    private val _activeSession = MutableStateFlow<TimeEntry?>(null)
    val activeSession = _activeSession.asStateFlow()

    init {
        viewModelScope.launch {
            _activeSession.value = repository.getActiveSession()
        }
    }


    // All entries, sorted newest first
    val allEntries = repository.getAllEntries()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Unsubmitted entries
    val unsubmittedEntries = repository.getUnsubmittedEntries()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Insert a new entry
    fun insertTimeEntry(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        isManual: Boolean = false,
        label: String? = null
    ) {
        viewModelScope.launch {
            val duration = Duration.between(startTime, endTime).toMinutes()
            val entry = TimeEntry(
                startTime = startTime,
                endTime = endTime,
                durationMinutes = duration,
                isManual = isManual,
                label = label
            )
            repository.insertTimeEntry(entry)
        }
    }

    // Mark an entry as submitted
    fun setSubmitted(id: Int, isSubmitted: Boolean) {
        viewModelScope.launch {
            repository.setSubmitted(id, isSubmitted)
        }
    }

    // Delete an entry
    fun deleteTimeEntry(entry: TimeEntry) {
        viewModelScope.launch {
            repository.deleteTimeEntry(entry)
        }
    }

    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn = _isClockedIn.asStateFlow()

    private var clockInTime: LocalDateTime? = null

    fun clockIn() {
        viewModelScope.launch {
            val start = LocalDateTime.now()
            val entry = TimeEntry(
                startTime = start,
                endTime = null,
                durationMinutes = null
            )
            repository.insertTimeEntry(entry)
            _activeSession.value = repository.getActiveSession()
        }
    }

    fun clockOut() {
        viewModelScope.launch {
            val session = _activeSession.value
            if (session != null) {
                val end = LocalDateTime.now()
                val duration = java.time.Duration.between(session.startTime, end).toMinutes()
                repository.completeSession(session.id, end, duration)
                _activeSession.value = null
            }
        }
    }


}
