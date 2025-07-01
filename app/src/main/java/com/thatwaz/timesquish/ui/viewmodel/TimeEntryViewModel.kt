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
        clockInTime = LocalDateTime.now()
        _isClockedIn.value = true
    }

    fun clockOut() {
        clockInTime?.let { start ->
            val end = LocalDateTime.now()
            insertTimeEntry(
                startTime = start,
                endTime = end,
                isManual = false
            )
        }
        clockInTime = null
        _isClockedIn.value = false
    }

}
