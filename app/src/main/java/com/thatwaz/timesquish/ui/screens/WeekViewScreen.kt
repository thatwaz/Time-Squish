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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class ListMode {
    NORMAL,
    SUBMIT,
    SQUISH
}


@Composable
fun WeekViewScreen(
    viewModel: TimeEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // List Mode State
    var listMode by remember { mutableStateOf(ListMode.NORMAL) }

    // All Entries
    val allEntries by viewModel.allEntries.collectAsState()

    // Selected Week State
    val thisWeekStart = getStartOfWeek(LocalDateTime.now())
    var selectedWeekStart by remember { mutableStateOf(thisWeekStart) }

    // Dropdown for week selector
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val weeks = (0..11).map { thisWeekStart.minusWeeks(it.toLong()) }

    // Squish selection state
    val selectingDays = remember { mutableStateOf(setOf<DayOfWeek>()) }
    val selectedEntryIdsByDay = remember { mutableStateOf(mapOf<DayOfWeek, Set<Int>>()) }

    // Pending deletion
    var entryPendingDelete by remember { mutableStateOf<TimeEntry?>(null) }

    // Filter entries for this week
    val weekEntries = allEntries.filter { entry ->
        entry.startTime.isAfter(selectedWeekStart.minusNanos(1)) &&
                entry.startTime.isBefore(getEndOfWeek(selectedWeekStart).plusNanos(1))
    }

    val unsubmittedEntries = weekEntries.filter { !it.isSubmitted }
    val submittedEntries = weekEntries.filter { it.isSubmitted }

    val unsubmittedGrouped = unsubmittedEntries.groupBy { it.startTime.dayOfWeek }.toSortedMap()
    val submittedGrouped = submittedEntries.groupBy { it.startTime.dayOfWeek }.toSortedMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Week Selector Dropdown
        Box {
            Button(
                onClick = { isDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Week of ${selectedWeekStart.toLocalDate()}")
            }
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                weeks.forEach { week ->
                    DropdownMenuItem(
                        text = { Text(week.toLocalDate().toString()) },
                        onClick = {
                            selectedWeekStart = week
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = {
                    listMode = if (listMode == ListMode.SUBMIT) ListMode.NORMAL else ListMode.SUBMIT
                },
                enabled = listMode != ListMode.SQUISH
            ) {
                Text(if (listMode == ListMode.SUBMIT) "Exit Submit Mode" else "Submit Mode")
            }

            OutlinedButton(
                onClick = {
                    listMode = if (listMode == ListMode.SQUISH) ListMode.NORMAL else ListMode.SQUISH
                    if (listMode != ListMode.SQUISH) {
                        selectingDays.value = emptySet()
                        selectedEntryIdsByDay.value = emptyMap()
                    }
                },
                enabled = listMode != ListMode.SUBMIT
            ) {
                Text(if (listMode == ListMode.SQUISH) "Exit Squish Mode" else "Squish Mode")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (weekEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No entries this week.")
            }
        } else {
            LazyColumn {

                // Unsubmitted Section
                item {
                    Text(
                        "Unsubmitted Entries",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (unsubmittedGrouped.isEmpty()) {
                    item {
                        Text(
                            "No unsubmitted entries.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    unsubmittedGrouped.forEach { (dayOfWeek, entriesForDay) ->
                        val isSelecting = selectingDays.value.contains(dayOfWeek)
                        val selectedIds = selectedEntryIdsByDay.value[dayOfWeek] ?: emptySet()

                        // Day Header + Squish Toggle
                        item {
                            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
                            val dateText = entriesForDay.first().startTime.format(dateFormatter)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} - $dateText",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (entriesForDay.size > 1 && listMode == ListMode.SQUISH) {
                                    Button(
                                        onClick = {
                                            selectingDays.value = if (isSelecting) {
                                                selectingDays.value - dayOfWeek
                                            } else {
                                                selectingDays.value + dayOfWeek
                                            }
                                            selectedEntryIdsByDay.value =
                                                selectedEntryIdsByDay.value.toMutableMap().apply {
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

                        // Entries for this day
                        items(entriesForDay) { entry ->
                            TimeEntryRow(
                                entry = entry,
                                onSetSubmitted = { viewModel.setSubmitted(entry.id, it) },
                                onDelete = { entryPendingDelete = entry },
                                onUnsquish = if (entry.label == "Squished Block") {
                                    { viewModel.unsquishEntry(entry) }
                                } else null,
                                listMode = listMode,
                                showSelectCheckbox = isSelecting,
                                isSelected = selectedIds.contains(entry.id),
                                onSelectChanged = { checked ->
                                    val current = selectedIds
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
                                        val entriesToSquish =
                                            entriesForDay.filter { selectedIds.contains(it.id) }
                                        viewModel.squishEntries(entriesToSquish)
                                        selectedEntryIdsByDay.value =
                                            selectedEntryIdsByDay.value.toMutableMap().apply {
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

                // Submitted Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Submitted Entries",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (submittedGrouped.isEmpty()) {
                    item {
                        Text(
                            "No submitted entries.",
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
                                "${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} - $dateText",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(entriesForDay) { entry ->
                            TimeEntryRow(
                                entry = entry,
                                onSetSubmitted = { viewModel.setSubmitted(entry.id, it) },
                                onDelete = { entryPendingDelete = entry },
                                listMode = listMode
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    if (entryPendingDelete != null) {
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this time entry? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTimeEntry(entryPendingDelete!!)
                        entryPendingDelete = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { entryPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}




@Composable
fun TimeEntryRow(
    entry: TimeEntry,
    onSetSubmitted: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onUnsquish: (() -> Unit)? = null,
    listMode: ListMode,
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
        if (listMode == ListMode.SQUISH && showSelectCheckbox && onSelectChanged != null) {
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

        // Entry text
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
            // Unsquish button
            if (entry.label == "Squished Block" && onUnsquish != null) {
                Button(
                    onClick = onUnsquish,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Unsquish")
                }
            }
        }

        // Delete icon always visible
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }

        // Submit checkbox only in submit mode
        if (listMode == ListMode.SUBMIT) {
            Checkbox(
                checked = entry.isSubmitted,
                onCheckedChange = onSetSubmitted
            )
        }
    }
}




