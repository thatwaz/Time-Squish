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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thatwaz.timesquish.data.local.TimeEntry
import com.thatwaz.timesquish.ui.viewmodel.TimeEntryViewModel
import com.thatwaz.timesquish.util.getEndOfWeek
import com.thatwaz.timesquish.util.getStartOfWeek
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

    // NEW STATE
    var isSelectingForSquish by remember { mutableStateOf(false) }
    val selectedEntryIds = remember { mutableStateListOf<Int>() }

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
                                            isSelectingForSquish = !isSelectingForSquish
                                            selectedEntryIds.clear()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Text(if (isSelectingForSquish) "Cancel Selection" else "Select Entries to Squish")
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
                                    {
                                        viewModel.unsquishEntryByDate(entry)
                                    }
                                } else {
                                    null
                                },
                                showSelectCheckbox = isSelectingForSquish,
                                isSelected = selectedEntryIds.contains(entry.id),
                                onSelectChanged = { checked ->
                                    if (checked) {
                                        selectedEntryIds.add(entry.id)
                                    } else {
                                        selectedEntryIds.remove(entry.id)
                                    }
                                }
                            )
                            Divider()
                        }
                    }

                    // Squish Selected button
                    if (isSelectingForSquish && selectedEntryIds.isNotEmpty()) {
                        item {
                            Button(
                                onClick = {
                                    val entriesToSquish = entries.filter { selectedEntryIds.contains(it.id) }
                                    viewModel.squishEntries(entriesToSquish)
                                    isSelectingForSquish = false
                                    selectedEntryIds.clear()
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
            // If this is a squished block, show Unsquish button
            if (entry.label == "Squished Block" && onUnsquish != null) {
                Button(
                    onClick = onUnsquish,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Unsquish")
                }
            }
        }
        if (showSelectCheckbox && onSelectChanged != null) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectChanged
            )
        } else {
            Checkbox(
                checked = entry.isSubmitted,
                onCheckedChange = onSetSubmitted
            )
        }
    }
}


