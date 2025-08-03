package com.thatwaz.timesquish.ui.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thatwaz.timesquish.data.local.TimeEntry
import com.thatwaz.timesquish.data.repository.TimeEntryRepository
import com.thatwaz.timesquish.util.ReminderScheduler
import com.thatwaz.timesquish.util.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class TimeEntryViewModel @Inject constructor(
    private val repository: TimeEntryRepository,
    val userPreferences: UserPreferences,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _activeSession = MutableStateFlow<TimeEntry?>(null)
    val activeSession = _activeSession.asStateFlow()

    val reminderHoursFlow = userPreferences.reminderHoursFlow

    // All entries, sorted newest first
    val allEntries = repository.getAllEntries()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Group and sort entries by day, with the newest day first
    val groupedEntriesByDate = allEntries
        .map { entries ->
            entries
                .filter { true }
                .groupBy { it.startTime.toLocalDate() }
                .toSortedMap(compareByDescending { it }) // Sort by date descending
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())



    // Hourly pay flow
    val hourlyPayFlow: Flow<Double> = userPreferences.hourlyPayFlow



    // Save hourly pay
    suspend fun saveHourlyPay(rate: Double) {
        userPreferences.setHourlyPay(rate)
    }



    init {
        viewModelScope.launch {
            _activeSession.value = repository.getActiveSession()
        }
    }

    // Function to save reminder hours
    suspend fun saveReminderHours(hours: Long) {
        userPreferences.setReminderHours(hours)
    }





    // Unsubmitted entries
    val unsubmittedEntries = repository.getUnsubmittedEntries()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Total earnings for the current week
    val weeklyEarnings = allEntries
        .map { entries ->
            val startOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY)
            val endOfWeek = startOfWeek.plusDays(6)

            entries
                .filter {
                    val date = it.startTime.toLocalDate()
                    date in startOfWeek..endOfWeek
                }
                .sumOf { entry ->
                    val rate = entry.hourlyPay ?: 0.0 // fallback to 0.0 if not stored
                    val hours = (entry.durationMinutes ?: 0) / 60.0
                    hours * rate
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

//    val weeklyEarnings = allEntries
//        .map { entries ->
//            val startOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY)
//            val endOfWeek = startOfWeek.plusDays(6)
//            entries
//                .filter {
//                    val date = it.startTime.toLocalDate()
//                    date in startOfWeek..endOfWeek
//                }
//                .sumOf { (it.durationMinutes ?: 0) / 60.0 * 20.0 } // Replace 20.0 with a dynamic rate later
//        }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    // Insert a new entry
    fun insertTimeEntry(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        isManual: Boolean = true,
        label: String? = null
    ) {
        viewModelScope.launch {
            val duration = Duration.between(startTime, endTime).toMinutes()
            val payRate = hourlyPayFlow.first()
            val entry = TimeEntry(
                startTime = startTime,
                endTime = endTime,
                durationMinutes = duration,
                isManual = isManual,
                label = label,
                hourlyPay = payRate
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

    fun updateTimeEntry(entry: TimeEntry) {
        viewModelScope.launch {
            repository.updateTimeEntry(entry)
        }
    }


    private val _isClockedIn = MutableStateFlow(false)
    val isClockedIn = _isClockedIn.asStateFlow()

    private var clockInTime: LocalDateTime? = null

    fun clockIn(hourlyPay: Double) {
        viewModelScope.launch {
            val start = LocalDateTime.now()
            val entry = TimeEntry(
                startTime = start,
                endTime = null,
                durationMinutes = null,
                hourlyPay = hourlyPay // << save pay per session
            )
            repository.insertTimeEntry(entry)
            _activeSession.value = repository.getActiveSession()

            val reminderHours = userPreferences.reminderHoursFlow.first()
            scheduleClockInReminder(reminderHours)
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

                // Cancel reminder
                cancelClockInReminder()
            }
        }
    }

    fun squishEntries(entriesToSquish: List<TimeEntry>) {
        viewModelScope.launch {
            if (entriesToSquish.isEmpty()) return@launch

            val groupId = UUID.randomUUID().toString()

            // 1️⃣ Earliest start time
            val startTime = entriesToSquish.minBy { it.startTime }.startTime

            // 2️⃣ Total duration
            val totalWorkedMinutes = entriesToSquish.sumOf { it.durationMinutes ?: 0 }

            // 3️⃣ Shifted end time
            val adjustedEndTime = startTime.plusMinutes(totalWorkedMinutes)

            val averagePayRate = entriesToSquish.map { it.hourlyPay }.average()

            val squishedEntry = TimeEntry(
                startTime = startTime,
                endTime = adjustedEndTime,
                durationMinutes = totalWorkedMinutes,
                isManual = true,
                isSubmitted = false,
                label = "Squished Block",
                squishGroupId = groupId,
                hourlyPay = averagePayRate // 💵 This fixes the display issue
            )


            repository.insertTimeEntry(squishedEntry)

            entriesToSquish.forEach {
                repository.updateTimeEntry(it.copy(isHidden = true, squishGroupId = groupId))
            }
        }
    }




    fun unsquishEntry(squishedEntry: TimeEntry) {
        viewModelScope.launch {
            // Get all entries one time
            val allEntries = repository.getAllEntriesOnce()

            // Find only the ones belonging to this squish group
            val originals = allEntries.filter {
                it.isHidden && it.squishGroupId == squishedEntry.squishGroupId
            }

            // Delete squished summary
            repository.deleteTimeEntry(squishedEntry)

            // Restore originals
            originals.forEach {
                repository.updateTimeEntry(it.copy(isHidden = false, squishGroupId = null))
            }
        }
    }



    fun unsquishEntryByDate(squishedEntry: TimeEntry) {
        viewModelScope.launch {
            // Get all entries including hidden ones
            val allEntries = repository.getAllEntriesIncludingHidden()
            // Find the originals
            val originals = allEntries.filter {
                it.isHidden && it.startTime.toLocalDate() == squishedEntry.startTime.toLocalDate()
            }

            // Log what you found
            originals.forEach {
                Log.d("UNSQUISH", "Unhiding ID=${it.id}")
            }

            // Remove the squished record
            repository.deleteTimeEntry(squishedEntry)
            // Mark originals as visible
            originals.forEach {
                repository.updateTimeEntry(it.copy(isHidden = false))
            }
        }
    }

    fun scheduleClockInReminder(hours: Long) {
        val millis = 30 * 1000

        ReminderScheduler.scheduleReminder(appContext, millis)
    }

    fun cancelClockInReminder() {
        ReminderScheduler.cancelReminder(appContext)
    }


    fun calculateWeeklyDurationMinutes(): Int {
        val startOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY)
        val endOfWeek = startOfWeek.plusDays(6)
        return allEntries.value
            .filter {
                val date = it.startTime.toLocalDate()
                date in startOfWeek..endOfWeek
            }
            .sumOf { (it.durationMinutes ?: 0).toLong() }
            .toInt()
    }






}
