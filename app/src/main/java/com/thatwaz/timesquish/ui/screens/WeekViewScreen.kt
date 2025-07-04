package com.thatwaz.timesquish.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked


import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.Circle
import com.thatwaz.timesquish.data.local.TimeEntry
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import com.thatwaz.timesquish.util.getEndOfWeek
import com.thatwaz.timesquish.util.getStartOfWeek
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WeekViewScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(LocalDateTime.now())) }
    val thisWeekStart = getStartOfWeek(LocalDateTime.now())
    val entries by viewModel.allEntries.collectAsState()

    // Store per-day selection state
    val selectingDays = remember { mutableStateOf(setOf<DayOfWeek>()) }
    val selectedEntryIdsByDay = remember { mutableStateOf(mapOf<DayOfWeek, Set<Int>>()) }

    // Filter entries to current week
    val weekEntries = entries.filter { entry ->
        entry.startTime.isAfter(currentWeekStart.minusNanos(1)) &&
                entry.startTime.isBefore(getEndOfWeek(currentWeekStart).plusNanos(1))
    }

    val unsubmittedEntries = weekEntries.filter { !it.isSubmitted }
    val submittedEntries = weekEntries.filter { it.isSubmitted }

    val unsubmittedGrouped = unsubmittedEntries
        .groupBy { it.startTime.dayOfWeek }
        .toSortedMap()

    val submittedGrouped = submittedEntries
        .groupBy { it.startTime.dayOfWeek }
        .toSortedMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Week Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                currentWeekStart = currentWeekStart.minusWeeks(1)
            }) {
                Text("< Previous Week")
            }

            Text(
                text = "Week of ${currentWeekStart.toLocalDate()}",
                style = MaterialTheme.typography.titleMedium
            )

            if (currentWeekStart.isBefore(thisWeekStart)) {
                Button(onClick = {
                    currentWeekStart = currentWeekStart.plusWeeks(1)
                }) {
                    Text("Next Week >")
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (weekEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No entries this week.")
            }
        } else {
            LazyColumn {
                // Unsubmitted section header
                item {
                    Text(
                        text = "Unsubmitted Entries",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (unsubmittedGrouped.isEmpty()) {
                    item {
                        Text(
                            text = "No unsubmitted entries.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    unsubmittedGrouped.forEach { (dayOfWeek, entriesForDay) ->
                        val isSelecting = selectingDays.value.contains(dayOfWeek)
                        val selectedIds = selectedEntryIdsByDay.value[dayOfWeek] ?: emptySet()

                        item {
                            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
                            val dateText = entriesForDay.first().startTime.format(dateFormatter)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} - $dateText",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (entriesForDay.size > 1) {
                                    Button(
                                        onClick = {
                                            selectingDays.value = if (isSelecting) {
                                                selectingDays.value - dayOfWeek
                                            } else {
                                                selectingDays.value + dayOfWeek
                                            }
                                            // Clear selections for this day when toggling off
                                            selectedEntryIdsByDay.value = selectedEntryIdsByDay.value.toMutableMap().apply {
                                                put(dayOfWeek, emptySet())
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Text(if (isSelecting) "Cancel Selection" else "Select Entries to Squish")
                                    }
                                }
                            }
                        }

                        items(entriesForDay) { entry ->
                            TimeEntryRow(
                                entry = entry,
                                onSetSubmitted = { isSubmitted ->
                                    viewModel.setSubmitted(entry.id, isSubmitted)
                                },
                                onUnsquish = if (entry.label == "Squished Block") {
                                    { viewModel.unsquishEntryByDate(entry) }
                                } else null,
                                showSelectCheckbox = isSelecting,
                                isSelected = selectedIds.contains(entry.id),
                                onSelectChanged = { checked ->
                                    val current = selectedEntryIdsByDay.value[dayOfWeek] ?: emptySet()
                                    selectedEntryIdsByDay.value = selectedEntryIdsByDay.value.toMutableMap().apply {
                                        put(
                                            dayOfWeek,
                                            if (checked) current + entry.id else current - entry.id
                                        )
                                    }
                                }
                            )
                            Divider()
                        }

                        if (isSelecting && selectedIds.isNotEmpty()) {
                            item {
                                Button(
                                    onClick = {
                                        val entriesToSquish = entriesForDay.filter { selectedIds.contains(it.id) }
                                        viewModel.squishEntries(entriesToSquish)
                                        // Reset selection state for this day
                                        selectedEntryIdsByDay.value = selectedEntryIdsByDay.value.toMutableMap().apply {
                                            put(dayOfWeek, emptySet())
                                        }
                                        selectingDays.value = selectingDays.value - dayOfWeek
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text("Squish Selected Entries")
                                }
                            }
                        }
                    }
                }

                // Submitted section header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Submitted Entries",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (submittedGrouped.isEmpty()) {
                    item {
                        Text(
                            text = "No submitted entries.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    submittedGrouped.forEach { (dayOfWeek, entriesForDay) ->
                        item {
                            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
                            val dateText = entriesForDay.first().startTime.format(dateFormatter)
                            Text(
                                text = "${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} - $dateText",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(entriesForDay) { entry ->
                            TimeEntryRow(
                                entry = entry,
                                onSetSubmitted = { isSubmitted ->
                                    viewModel.setSubmitted(entry.id, isSubmitted)
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TimeEntryRow(
    entry: TimeEntry,
    onSetSubmitted: (Boolean) -> Unit,
    onUnsquish: (() -> Unit)? = null,
    showSelectCheckbox: Boolean = false,
    isSelected: Boolean = false,
    onSelectChanged: ((Boolean) -> Unit)? = null
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Squish selection toggle on the LEFT
        if (showSelectCheckbox && onSelectChanged != null) {
            IconToggleButton(
                checked = isSelected,
                onCheckedChange = onSelectChanged
            ) {
                Icon(
                    imageVector = if (isSelected)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Select for squish"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Entry text in the middle
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = entry.startTime.format(dateFormatter),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${entry.startTime.format(timeFormatter)} - ${entry.endTime?.format(timeFormatter) ?: "In Progress"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Duration: ${entry.durationMinutes ?: "..."} min",
                style = MaterialTheme.typography.bodySmall
            )
            if (!entry.label.isNullOrBlank()) {
                Text(
                    text = "Label: ${entry.label}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Unsquish button if applicable
            if (entry.label == "Squished Block" && onUnsquish != null) {
                Button(
                    onClick = onUnsquish,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Unsquish")
                }
            }
        }

        // Submission checkbox on the RIGHT
        if (!showSelectCheckbox) {
            Checkbox(
                checked = entry.isSubmitted,
                onCheckedChange = onSetSubmitted
            )
        }
    }
}



