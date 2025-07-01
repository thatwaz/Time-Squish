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



    // Filter entries to the current week
    val weekEntries = entries.filter { entry ->
        entry.startTime.isAfter(currentWeekStart.minusNanos(1)) &&
                entry.startTime.isBefore(getEndOfWeek(currentWeekStart).plusNanos(1))
    }

    // Group by day of week
    val groupedEntries = weekEntries
        .groupBy { it.startTime.dayOfWeek }
        .toSortedMap() // Sorts Sunday -> Saturday

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
            // Always show Previous button
            Button(onClick = {
                currentWeekStart = currentWeekStart.minusWeeks(1)
            }) {
                Text("< Previous Week")
            }

            Text(
                text = "Week of ${currentWeekStart.toLocalDate()}",
                style = MaterialTheme.typography.titleMedium
            )

            // Only show Next button if NOT on the current week
            if (currentWeekStart.isBefore(thisWeekStart)) {
                Button(onClick = {
                    currentWeekStart = currentWeekStart.plusWeeks(1)
                }) {
                    Text("Next Week >")
                }
            } else {
                // Spacer to balance layout
                Spacer(modifier = Modifier.width(8.dp))
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        if (groupedEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No entries this week.")
            }
        } else {
            LazyColumn {
                groupedEntries.forEach { (dayOfWeek, entriesForDay) ->
                    // Day header
                    item {
                        Text(
                            text = dayOfWeek.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    // Entries for this day
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

@Composable
fun TimeEntryRow(
    entry: TimeEntry,
    onSetSubmitted: (Boolean) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${entry.startTime.format(dateFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${entry.startTime.format(timeFormatter)} - ${entry.endTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Duration: ${entry.durationMinutes} min",
                style = MaterialTheme.typography.bodySmall
            )
            if (!entry.label.isNullOrBlank()) {
                Text(
                    text = "Label: ${entry.label}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Checkbox(
            checked = entry.isSubmitted,
            onCheckedChange = onSetSubmitted
        )
    }
}
